package com.luisvilaca.nutsandbolts

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject

// The tablet and the phone share one copy of everything in Firebase, under the owner's Google account:
// users/{uid} holds the state, one field per habit, per day's bolts of a habit, per part (see flat in Store.kt), and
// users/{uid}/pages/{date} each diary page. Firestore is the merge: every change made here is written to it at once
// (queued on the device while offline), only the fields that changed, so two devices changing different things never
// undo each other; whatever arrives from the other device replaces the copy here, and the wallet is counted again
// from the marks. last (kept in sync-{uid}.json) is the cloud copy as this device last saw it, so a change made while
// sync was not running (a notification's Done, say) still goes up at the next start.
object Sync {
    var who by mutableStateOf<String?>(null) // the account signed in, for Settings
    var state by mutableStateOf("")
    private var store: Store? = null
    private lateinit var dir: File
    private lateinit var app: Context
    private var doc: DocumentReference? = null
    private var uid = ""
    private var regs = listOf<ListenerRegistration>()
    private var last: Map<String, Any?> = emptyMap()
    private var pagesAt = 0L // the newest page time seen; later starts ask only for pages written since
    private val dirty = mutableSetOf<String>() // pages written here and not yet in the cloud
    private val waiting = mutableMapOf<LocalDate, Page>()
    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()

    // Firebase starts on its own when the build carries google-services.json; without it there is no sync
    fun ready(c: Context) = FirebaseApp.getApps(c).isNotEmpty()

    fun start(c: Context, s: Store) {
        if (store != null || !ready(c)) return
        store = s; dir = c.filesDir; app = c.applicationContext
        s.onChange = ::push
        s.onPage = ::queuePage
        FirebaseAuth.getInstance().currentUser?.let(::attach)
    }

    // null when signed in, or what went wrong
    suspend fun signIn(c: Context): String? = try {
        val client = c.resources.getIdentifier("default_web_client_id", "string", c.packageName).takeIf { it != 0 }?.let(c::getString)
            ?: return t("This build has no Google sign-in set up (see SYNC.md).")
        val cred = CredentialManager.create(c).getCredential(c, GetCredentialRequest.Builder().addCredentialOption(GetSignInWithGoogleOption.Builder(client).build()).build()).credential
        if (cred !is CustomCredential || cred.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) t("That sign-in is not a Google account.")
        else {
            val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
            val u = FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(token, null)).done().user!!
            attach(u)
            null
        }
    } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) { "" } catch (e: Exception) { e.message ?: t("Sign-in failed.") }

    // Email and password, for an account without Google (it still needs Google's servers: a VPN where they are
    // blocked). create makes the account first. null when signed in, or what went wrong.
    suspend fun signInEmail(email: String, password: String, create: Boolean): String? = try {
        val a = FirebaseAuth.getInstance()
        val r = if (create) a.createUserWithEmailAndPassword(email.trim(), password).done() else a.signInWithEmailAndPassword(email.trim(), password).done()
        attach(r.user!!)
        null
    } catch (e: Exception) { why(e) }

    // Sends a link to choose a new password, written in the app's language. null when sent, or what went wrong.
    suspend fun resetPassword(email: String): String? = try {
        FirebaseAuth.getInstance().apply { setLanguageCode(Lang.code) }.sendPasswordResetEmail(email.trim()).done()
        null
    } catch (e: Exception) { why(e) }

    private fun why(e: Exception): String = when {
        e is FirebaseNetworkException -> t("No connection: check the internet (or the VPN) and try again.")
        e !is FirebaseAuthException -> e.message ?: t("Sign-in failed.")
        else -> when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> t("That is not an email address.")
            "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL" -> t("Wrong email or password.")
            "ERROR_EMAIL_ALREADY_IN_USE" -> t("There is already an account with that email: sign in instead.")
            "ERROR_WEAK_PASSWORD" -> t("The password needs at least 6 characters.")
            "ERROR_OPERATION_NOT_ALLOWED" -> t("Email sign-in is not turned on for this app yet (see SYNC.md).")
            else -> e.message ?: t("Sign-in failed.")
        }
    }

    // This device keeps everything it has; it only stops following the cloud.
    suspend fun signOut(c: Context) {
        regs.forEach { it.remove() }; regs = emptyList()
        doc = null; who = null; state = ""
        Buddy.detach()
        FirebaseAuth.getInstance().signOut()
        runCatching { CredentialManager.create(c).clearCredentialState(ClearCredentialStateRequest()) }
    }

    // A restored backup replaces the cloud copy too: its state goes up as changes, and every page goes up again.
    fun restored() {
        val s = store ?: return
        push()
        s.pageDays().forEach { d -> s.page(d)?.let { queuePage(d, it) } }
    }

    private fun attach(u: FirebaseUser) {
        val s = store ?: return
        who = u.email ?: u.displayName ?: t("your account")
        uid = u.uid
        val d = FirebaseFirestore.getInstance().collection("users").document(u.uid)
        doc = d
        Buddy.attach(app, u.uid, u.email.orEmpty(), u.displayName, s)
        val saved = File(dir, "sync-$uid.json").takeIf { it.exists() }?.let { f -> runCatching { JSONObject(f.readText()) }.getOrNull() }
        if (saved != null) {
            @Suppress("UNCHECKED_CAST")
            last = plain(saved.getJSONObject("last")) as Map<String, Any?>
            pagesAt = saved.optLong("pagesAt")
            saved.optJSONArray("dirty")?.let { a -> for (i in 0 until a.length()) dirty += a.getString(i) }
            push() // what changed here while sync was not running
            dirty.toList().forEach { k -> s.page(LocalDate.parse(k))?.let { queuePage(LocalDate.parse(k), it) } }
            listen(d, first = false)
        } else {
            // the first time on this device: an existing cloud copy wins, and what this device had is kept in a backup
            // file in Downloads first (Settings > Restore can bring it back); with no cloud copy yet, this device's goes up
            state = t("Joining…")
            d.get().addOnSuccessListener { snap ->
                if (doc !== d) return@addOnSuccessListener
                if (snap.exists()) {
                    if (s.s.habits.isNotEmpty() || s.s.diary.isNotEmpty()) keepCopy(s.backup())
                    val remote = flatOf(snap.data.orEmpty())
                    runCatching { fromFlat(remote, s.s) }.onSuccess { last = remote; s.replace(it) }
                } else push()
                pagesAt = 0; keep()
                listen(d, first = true)
            }.addOnFailureListener { state = t("Sync could not start: {0}", it.message) }
        }
    }

    private fun listen(d: DocumentReference, first: Boolean) {
        var join = first
        regs = listOf(
            d.addSnapshotListener(MetadataChanges.INCLUDE) { snap, e -> if (e != null) state = t("Sync stopped: {0}", e.message) else if (snap != null) arrived(snap) },
            d.collection("pages").whereGreaterThan("at", pagesAt).addSnapshotListener { q, e ->
                if (e != null || q == null) return@addSnapshotListener
                if (join) {
                    // joining: this device's pages that the cloud does not have go up
                    join = false
                    val there = q.documents.map { it.id }.toSet()
                    store?.let { s -> s.pageDays().filter { it.toString() !in there }.forEach { day -> s.page(day)?.let { queuePage(day, it) } } }
                }
                for (ch in q.documentChanges) {
                    val x = ch.document
                    if (x.metadata.hasPendingWrites()) continue // our own write, on its way
                    val day = runCatching { LocalDate.parse(x.id) }.getOrNull() ?: continue
                    pagesAt = maxOf(pagesAt, x.getLong("at") ?: 0)
                    val z = x.getBlob("z")?.toBytes()
                    io.execute {
                        val page = z?.let { runCatching { pageOf(it) }.getOrNull() }
                        main.post { pageArrived(day, page) }
                    }
                }
                keep()
            },
        )
    }

    private fun arrived(snap: DocumentSnapshot) {
        val s = store ?: return
        state = when {
            snap.metadata.hasPendingWrites() -> t("Sending…")
            snap.metadata.isFromCache -> t("Offline: changes wait on this device")
            else -> t("Up to date")
        }
        if (snap.metadata.hasPendingWrites() || !snap.exists()) return
        val remote = flatOf(snap.data.orEmpty())
        if (remote == last) return
        runCatching { fromFlat(remote, s.s) }
            .onSuccess { last = remote; keep(); s.replace(it) }
            .onFailure { state = t("The other device has a newer app: update this one to keep syncing") }
    }

    // shortcut: a page open in the editor while the other device changes it keeps showing (and later saves) this device's copy; reopen it to see the other
    private fun pageArrived(day: LocalDate, page: Page?) {
        val s = store ?: return
        if (day in waiting || day.toString() in dirty) return // an edit here is on its way and wins
        val has = page != null && page.strokes.isNotEmpty()
        s.writePage(day, page ?: Page(PAPERS[0], mutableListOf()), mine = false)
        if (has != (day in s.s.diary)) s.change { if (has) diary += day else diary -= day }
    }

    private fun push() {
        val d = doc ?: return
        val s = store ?: return
        Buddy.publish(s)
        val cur = flat(s.s)
        val ch = diff(last, cur, FieldValue.delete())
        if (ch.isEmpty()) return
        d.set(nest(ch), SetOptions.merge()).addOnFailureListener { state = t("Not sent: {0}", it.message) }
        last = cur
        keep()
    }

    // While the pen writes, a page goes up at most once every few seconds, with its newest strokes, not on every stroke.
    private fun queuePage(day: LocalDate, p: Page) {
        if (store == null) return
        if (dirty.add(day.toString())) keep()
        if (waiting.put(day, p) == null) main.postDelayed({ sendPage(day) }, 4000)
    }

    private fun sendPage(day: LocalDate) {
        val p = waiting.remove(day) ?: return
        val ref = doc?.collection("pages")?.document(day.toString()) ?: return // signed out: it stays dirty for the next sign-in
        val at = System.currentTimeMillis()
        io.execute {
            // a blank page stays in the cloud as an empty one, so a device that asks for pages since a time sees it go
            val b = if (p.strokes.isEmpty()) null else pageBytes(p)
            // shortcut: a Firestore document holds 1 MiB, about 150,000 points of ink; a page past that stays on this device only, split it into parts if that ever happens
            if (b != null && b.size > 1_000_000) { main.post { state = t("The page of {0} is too full to sync", day) }; return@execute }
            ref.set(mapOf("z" to b?.let { Blob.fromBytes(it) }, "at" to at))
                .addOnSuccessListener { if (day !in waiting && dirty.remove(day.toString())) keep() }
                .addOnFailureListener { state = t("Page not sent: {0}", it.message) }
        }
    }

    private fun keepCopy(text: String) {
        val name = "nuts-and-bolts-before-sync-${LocalDate.now()}-${System.currentTimeMillis() % 100000}.json"
        if (Build.VERSION.SDK_INT >= 29) runCatching {
            val r = app.contentResolver
            val uri = r.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name); put(MediaStore.Downloads.MIME_TYPE, "application/json")
            })!!
            r.openOutputStream(uri)!!.use { it.write(text.toByteArray()) }
            return
        }
        File(dir, name).writeText(text) // older Android: in the app's own folder
    }

    private fun keep() {
        if (uid.isEmpty()) return
        val text = JSONObject().put("last", JSONObject(last)).put("pagesAt", pagesAt).put("dirty", JSONArray(dirty.toList())).toString()
        val f = File(dir, "sync-$uid.json")
        io.execute { File(dir, f.name + ".tmp").apply { writeText(text); renameTo(f) } }
    }

    // org.json back to the plain values the cloud uses
    private fun plain(v: Any?): Any? = when (v) {
        JSONObject.NULL, null -> null
        is JSONObject -> v.keys().asSequence().associateWith { plain(v.get(it)) }
        is JSONArray -> (0 until v.length()).map { plain(v.get(it)) }
        is Number -> v.toLong()
        else -> v
    }

    private suspend fun <T> Task<T>.done(): T = suspendCancellableCoroutine { c ->
        addOnCompleteListener { t -> if (t.isSuccessful) c.resume(t.result) else c.resumeWithException(t.exception ?: Exception("failed")) }
    }
}
