package com.luisvilaca.nutsandbolts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate

// The buddy link: two people, each with their own app, look and Google account, see each other's character and how
// today is going, and can send each other a cheer. Each app keeps a small card about itself at peek/{uid}: its name,
// look, a picture of the character, its levels, today's done and slots, and the buddy's email. Firestore lets a card
// be read only by the account whose email it names (the rules in SYNC.md). Naming someone is the invitation: their app
// sees a card naming them from someone they have not named, notifies them, and one tap on Accept names the sender back,
// which is the link. A cheer is a time written on the sender's card; the buddy's app, listening, shows a notification.
// shortcut: a cheer or an invitation arrives while the other app is running or next time it opens (no push server on
// the free plan);
// Cloud Messaging with a Cloud Function would deliver it to a closed app.
object Buddy {
    var buddy by mutableStateOf("") // the buddy's email, as named on this account's card
    var them by mutableStateOf<Peek?>(null)
    var invites by mutableStateOf<List<Peek>>(emptyList()) // cards naming this account from people not named back
    private var me = "" // this account's email
    private var who = "" // this account's first name, for the buddy's notifications
    private var uid = ""
    private var app: Context? = null
    private var own: ListenerRegistration? = null // this account's own card, for the buddy it names
    private var query: ListenerRegistration? = null // the cards naming this account
    private var naming = listOf<DocumentSnapshot>()
    private var loaded = false // the own card has been read: until then nothing is published
    private var last: Map<String, Any?> = emptyMap()
    private var picKey = "" // the equipment and mood the last picture was drawn with

    class Peek(val who: String, val name: String, val skin: String, val day: String, val done: Int, val slots: Int, val levels: List<Int>, val pic: ImageBitmap?, val cheer: Long, val email: String = "")

    private fun card() = FirebaseFirestore.getInstance().collection("peek")

    fun attach(c: Context, id: String, email: String, name: String?, s: Store) {
        app = c.applicationContext; uid = id; me = email.lowercase(); who = name?.substringBefore(' ').orEmpty()
        // the card is followed, not read once, so the buddy named on another device of this account shows here too
        own?.remove()
        own = card().document(uid).addSnapshotListener { snap, e ->
            if (e != null || snap == null || uid != id) return@addSnapshotListener
            val b = snap.getString("buddy").orEmpty()
            val first = !loaded
            loaded = true
            if (b != buddy) { buddy = b; sort() }
            if (first) { listen(); publish(s) }
        }
    }

    fun detach() {
        own?.remove(); own = null; query?.remove(); query = null; loaded = false
        uid = ""; buddy = ""; them = null; invites = emptyList(); naming = emptyList(); last = emptyMap(); picKey = ""
    }

    // Names (or, with "", forgets) the buddy: an invitation to them, or the answer to theirs. null when done, or what
    // is wrong with it.
    fun link(email: String, s: Store): String? {
        val e = email.trim().lowercase()
        if (uid.isEmpty()) return t("Sign in under Sync first.")
        if (e.isNotEmpty() && !Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+").matches(e)) return t("That is not an email address.")
        buddy = e
        card().document(uid).set(mapOf("buddy" to e, "email" to me), SetOptions.merge())
        sort()
        publish(s)
        return null
    }

    fun accept(p: Peek, s: Store) = link(p.email, s)

    // Not now: this device stops showing the invitation; naming them in Settings still links.
    fun decline(p: Peek) {
        val c = app ?: return
        File(c.filesDir, "buddy-declined-$uid").appendText(p.email + "\n")
        sort()
    }

    // The card's facts as they stand; written only when something on it changed. The picture is drawn again only when
    // the character's parts or mood change. The buddy is not among them: only link() writes it, so a card published
    // before the app has read its own (at start, say) cannot clear the link.
    fun publish(store: Store) {
        if (uid.isEmpty() || !loaded) return
        val s = store.s
        val today = LocalDate.now()
        val p = slotProgress(s, today)
        val cur = mapOf<String, Any?>("email" to me, "who" to who, "name" to s.robot, "skin" to SKIN, "day" to today.toString(),
            "done" to p.done.toLong(), "slots" to p.slots.toLong(), "levels" to Attr.entries.map { level(s.stats.getValue(it)).toLong() })
        val key = s.equip.toString() + mood(s)
        val ch = cur.filter { (k, v) -> last[k] != v }.toMutableMap()
        if (key != picKey) { ch["pic"] = Blob.fromBytes(picture(s)); picKey = key }
        if (ch.isEmpty()) return
        card().document(uid).set(ch, SetOptions.merge())
        last = cur
    }

    fun cheer() {
        if (uid.isEmpty()) return
        card().document(uid).set(mapOf("cheer" to System.currentTimeMillis()), SetOptions.merge())
    }

    // the character standing still, a pixel per robot unit (400 x 520), so it stays sharp in the buddy's bay
    private fun picture(s: Data): ByteArray {
        val img = ImageBitmap(400, 520)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(img), Size(400f, 520f)) {
            fit(400f, 520f) { robot(s.equip, 0L, mood(s)) }
        }
        return ByteArrayOutputStream().also { img.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
    }

    // Cards that name this account, as they change.
    private fun listen() {
        query?.remove()
        if (me.isEmpty()) return
        query = card().whereEqualTo("buddy", me).addSnapshotListener { q, e ->
            if (e != null || q == null) return@addSnapshotListener
            naming = q.documents
            sort()
        }
    }

    // The buddy's card is the one with the email named here; every other card naming this account is an invitation.
    private fun sort() {
        val c = app
        val declined = c?.let { File(it.filesDir, "buddy-declined-$uid").takeIf { f -> f.exists() }?.readLines()?.toSet() }.orEmpty()
        them = if (buddy.isEmpty()) null else naming.firstOrNull { it.getString("email") == buddy }?.let(::peek)
        invites = naming.filter { val m = it.getString("email").orEmpty(); m.isNotEmpty() && m != buddy && m != me && m !in declined }.map(::peek)
        them?.let(::cheered)
        invites.forEach(::invited)
    }

    private fun peek(d: DocumentSnapshot): Peek {
        val pic = d.getBlob("pic")?.toBytes()?.let { b -> BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap() }
        @Suppress("UNCHECKED_CAST")
        return Peek(d.getString("who").orEmpty(), d.getString("name").orEmpty(), d.getString("skin").orEmpty(), d.getString("day").orEmpty(), (d.getLong("done") ?: 0).toInt(),
            (d.getLong("slots") ?: 0).toInt(), (d.get("levels") as? List<Long>).orEmpty().map { l -> l.toInt() }, pic, d.getLong("cheer") ?: 0, d.getString("email").orEmpty())
    }

    // An invitation not yet announced on this device becomes a notification, once.
    private fun invited(p: Peek) {
        val c = app ?: return
        val f = File(c.filesDir, "buddy-invited-$uid")
        if (f.exists() && p.email in f.readLines()) return
        f.appendText(p.email + "\n")
        Reminder.notify(c, 8, Reminder.BUDDY, t("{0} wants to be your buddy", p.who.ifEmpty { p.name }), t("Tap to answer."), tab = Tab.Lab)
    }

    // A cheer newer than the last one seen on this device becomes a notification.
    private fun cheered(p: Peek) {
        val c = app ?: return
        val f = File(c.filesDir, "buddy-cheer-$uid")
        val seen = runCatching { f.readText().toLong() }.getOrDefault(0L)
        if (p.cheer <= seen) return
        f.writeText(p.cheer.toString())
        Sfx.play("level")
        Reminder.notify(c, 7, Reminder.BUDDY, t("{0} cheered you on!", p.who.ifEmpty { p.name }), t("Tap to cheer back."), tab = Tab.Lab)
    }
}
