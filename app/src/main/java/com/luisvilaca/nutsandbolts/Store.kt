package com.luisvilaca.nutsandbolts

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

// The saved state lives in one JSON file; each diary page in its own file, since handwriting outgrows the rest within weeks.
class Store private constructor(private val dir: File) {
    // bumped on every change. Reading s reads it too, so anything drawn from the data redraws when the data changes,
    // even a part of a screen the rest of the screen would let Compose skip.
    var tick by mutableIntStateOf(0)
        private set
    private var data: Data = load()
    val s: Data get() { tick; return data }

    fun change(block: Data.() -> Unit) { data.block(); tick++; save(); onChange?.invoke() }

    // Sync's hooks: every change made here, and every page written here, goes to the cloud copy as it happens.
    var onChange: (() -> Unit)? = null
    var onPage: ((LocalDate, Page) -> Unit)? = null
    // the other device's changes, already merged by the cloud; nothing to send back
    fun replace(d: Data) { data = d; tick++; save() }

    // the robot's current reaction; not saved
    var react by mutableStateOf<Reaction?>(null)
    fun react(kind: String) { react = Reaction(kind, now()) }
    // the habit whose newest electron is travelling home on the atom; not saved
    var fresh by mutableStateOf<Pair<String, Long>?>(null)

    private val stateFile = File(dir, "state.json")
    private val pagesDir = File(dir, "pages").apply { mkdirs() }
    init { File(dir, "thumbs").deleteRecursively() } // page pictures from a build that showed them in the calendar

    private fun load(): Data {
        val f = File(dir, "state.json")
        if (!f.exists()) return Data()
        return try { dataFrom(JSONObject(f.readText())) } catch (e: Exception) {
            f.copyTo(File(dir, "state-unreadable-${System.currentTimeMillis()}.json")) // keep the bad copy instead of overwriting it
            Data()
        }
    }

    // Saving: the state is turned into text here (cheap, and a true snapshot), then written on one background thread.
    // Saves that pile up while a write is under way collapse into the latest one. flush() finishes any write waiting.
    private val disk = Dispatchers.IO.limitedParallelism(1)
    private val writer = CoroutineScope(SupervisorJob() + disk)
    private val waiting = AtomicReference<String?>(null)
    fun save() {
        waiting.set(json(data).toString())
        writer.launch { waiting.getAndSet(null)?.let { writeAtomic(stateFile, it) } }
    }
    fun flush() = runBlocking(disk) {
        waiting.getAndSet(null)?.let { writeAtomic(stateFile, it) }
        pagesWaiting.keys.toList().forEach { d -> pagesWaiting.remove(d)?.let { putPage(d, it) } }
    }

    fun page(d: LocalDate): Page? = File(pagesDir, "$d.json").takeIf { it.exists() }?.let { pageFrom(JSONObject(it.readText())) }
    // A page is saved after every stroke. Like the state, it goes to the one writer thread (two writes of the same page
    // at once would share its .tmp file), and strokes that come faster than the disk collapse into the newest copy.
    // The caller marks the day in diary.
    private val pagesWaiting = java.util.concurrent.ConcurrentHashMap<LocalDate, Page>()
    // mine: written here, so it goes to the cloud too; a page that came from the cloud does not go back
    fun writePage(d: LocalDate, p: Page, mine: Boolean = true) {
        pagesWaiting[d] = p
        writer.launch { pagesWaiting.remove(d)?.let { putPage(d, it) } }
        if (mine) onPage?.invoke(d, p)
    }
    fun pageDays(): List<LocalDate> = pagesDir.listFiles()?.mapNotNull { runCatching { LocalDate.parse(it.nameWithoutExtension) }.getOrNull() }.orEmpty()
    private fun putPage(d: LocalDate, p: Page) {
        val f = File(pagesDir, "$d.json")
        if (p.strokes.isEmpty()) f.delete() else writeAtomic(f, pageJson(p).toString())
    }

    fun backup(): String = JSONObject()
        .put("app", BACKUP).put("v", 1).put("saved", LocalDate.now().toString())
        .put("state", json(s))
        .put("pages", JSONObject().apply { pagesDir.listFiles()?.forEach { put(it.nameWithoutExtension, JSONObject(it.readText())) } })
        .toString()

    // A backup file is untrusted until it checks out; nothing on the device changes before then.
    fun checkBackup(text: String): Pair<Data, Map<String, Page>>? = try {
        val b = JSONObject(text)
        require(b.getString("app") == BACKUP)
        val pages = b.getJSONObject("pages").let { o -> o.keys().asSequence().associateWith { k -> LocalDate.parse(k); pageFrom(o.getJSONObject(k)) } }
        dataFrom(b.getJSONObject("state")) to pages
    } catch (e: Exception) { null }

    // The new pages are written aside first; the old ones are only replaced once every page from the file is down.
    fun restore(b: Pair<Data, Map<String, Page>>) {
        val fresh = File(dir, "pages-new").apply { deleteRecursively(); mkdirs() }
        b.second.forEach { (k, p) -> File(fresh, "$k.json").writeText(pageJson(p).toString()) }
        val old = File(dir, "pages-old").apply { deleteRecursively() }
        check(pagesDir.renameTo(old))
        if (!fresh.renameTo(pagesDir)) { old.renameTo(pagesDir); error("pages could not be swapped") }
        old.deleteRecursively()
        data = b.first
        tick++
        save()
    }

    private fun writeAtomic(f: File, text: String) {
        val tmp = File(f.parentFile, f.name + ".tmp")
        tmp.writeText(text)
        if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f) }
    }

    companion object {
        const val BACKUP = "nuts-and-bolts"
        @Volatile private var one: Store? = null
        fun get(c: Context) = one ?: synchronized(this) { one ?: Store(c.applicationContext.filesDir).also { one = it } }
    }
}

// the animation clock: frame time in ms, as withFrameMillis reports it
fun now() = System.nanoTime() / 1_000_000

class InkStroke(val c: String, val w: Float, val e: Boolean, val p: FloatArray) // p: x, y, nib opening triples in page units
class Page(var paper: String, val strokes: MutableList<InkStroke>)

// ---------- JSON ----------

private fun JSONObject.str(k: String) = if (has(k) && !isNull(k)) getString(k) else null
private fun JSONObject.date(k: String) = str(k)?.let(LocalDate::parse)

// The saved state's shape; files without "v" are version 1. 3: marks name their attribute, and the wallet keeps adj.
const val SCHEMA = 3

// Brings a saved state of any earlier shape up to this one, so dataFrom reads only the current shape. Each step looks
// for what it fixes rather than trusting the version, so a partly old file is mended too.
internal fun migrate(o: JSONObject): JSONObject {
    // before levels there was no peak: an attribute has at least reached the level it is at
    val stats = o.optJSONObject("stats")
    val peak = o.optJSONObject("peak") ?: JSONObject()
    Attr.entries.forEach { a -> peak.put(a.id, maxOf(level(stats?.optInt(a.id, 30) ?: 30), peak.optInt(a.id))) }
    o.put("peak", peak)
    // before the sliders the pen kept three-step choices (size, stab, eraser); each maps to its nearest new value
    o.optJSONObject("pen")?.let { j ->
        if (!j.has("nib")) j.put("nib", listOf(0.35, 0.6, 1.0)[j.optInt("size", 1).coerceIn(0, 2)])
        if (!j.has("stabPct")) j.put("stabPct", listOf(0, 60, 90)[j.optInt("stab", 1).coerceIn(0, 2)])
        if (!j.has("rub")) j.put("rub", listOf(2.0, 4.0, 7.0)[j.optInt("eraser", 1).coerceIn(0, 2)])
    }
    return o.put("v", SCHEMA)
}

internal fun json(s: Data): JSONObject = JSONObject()
    .put("v", SCHEMA)
    .put("name", s.name).put("bolts", s.bolts).put("nuts", s.nuts)
    .put("stats", JSONObject().apply { s.stats.forEach { (a, v) -> put(a.id, v) } })
    .put("peak", JSONObject().apply { s.peak.forEach { (a, v) -> put(a.id, v) } })
    .put("owned", JSONArray(s.owned))
    .put("equip", JSONObject().apply { s.equip.forEach { (k, v) -> put(k, v ?: JSONObject.NULL) } })
    .put("habits", JSONArray(s.habits.map { h ->
        JSONObject().put("id", h.id).put("name", h.name).put("kind", h.kind.id).put("per", h.per.id).put("target", h.target)
            .put("stat", h.stat.id).put("weight", h.weight.id).put("start", h.start?.toString()).put("end", h.end?.toString())
            .put("days", h.days?.let { JSONArray(it.sorted()) }).put("remind", h.remind)
    }))
    .put("marks", JSONObject().apply {
        s.marks.forEach { (d, hs) -> put(d.toString(), JSONObject().apply { hs.forEach { (id, ms) -> put(id, JSONArray(ms.map { JSONArray(listOf(it.s.id, it.b, it.p, it.n, it.l, it.a?.id ?: "")) })) } }) }
    })
    .put("diary", JSONArray(s.diary.map { it.toString() }))
    .put("papers", JSONArray(s.papers))
    .put("pen", JSONObject().put("color", s.pen.color).put("nib", s.pen.nib.toDouble()).put("stabPct", s.pen.stab).put("rub", s.pen.rub.toDouble()).put("paper", s.pen.paper).put("seen", s.pen.seen).put("palm", s.pen.palm).put("b1", s.pen.b1).put("b2", s.pen.b2).put("move", s.pen.move))
    .put("remind", JSONObject().put("on", s.remind.on).put("time", s.remind.time))
    .put("sound", s.sound)
    .put("backedUp", s.backedUp?.toString())
    .put("adj", walletJson(s.adj))

private fun walletJson(w: Wallet) = JSONObject().put("bolts", w.bolts).put("nuts", w.nuts)
    .put("stats", JSONObject().apply { w.stats.forEach { (a, v) -> put(a.id, v) } }).put("peak", JSONObject().apply { w.peak.forEach { (a, v) -> put(a.id, v) } })
private fun walletFrom(o: JSONObject) = Wallet(o.getInt("bolts"), o.getInt("nuts"),
    Attr.entries.associateWith { o.getJSONObject("stats").getInt(it.id) }.toMutableMap(), Attr.entries.associateWith { o.getJSONObject("peak").getInt(it.id) }.toMutableMap())

internal fun dataFrom(raw: JSONObject) = Data().apply {
    val o = migrate(raw)
    name = o.optString("name", name)
    bolts = o.optInt("bolts").coerceAtLeast(0)
    nuts = o.optInt("nuts").coerceAtLeast(0)
    o.optJSONObject("stats")?.let { j -> Attr.entries.forEach { stats[it] = j.optInt(it.id, 30).coerceAtLeast(0) } }
    Attr.entries.forEach { peak[it] = o.getJSONObject("peak").optInt(it.id, level(stats.getValue(it))) }
    o.optJSONArray("owned")?.let { a -> owned.clear(); for (i in 0 until a.length()) owned += a.getString(i) }
    o.optJSONObject("equip")?.let { j -> equip.keys.toList().forEach { k -> equip[k] = j.str(k)?.takeIf { it in owned } ?: equip[k] } }
    o.getJSONArray("habits").let { a ->
        for (i in 0 until a.length()) a.getJSONObject(i).let { j ->
            // an unknown code fails the load (byId throws) rather than inventing a value
            val per = byId<Per>(j.getString("per"))
            habits += Habit(j.getString("id"), j.getString("name"), byId<Kind>(j.getString("kind")), per, j.getInt("target").coerceIn(1, maxTarget(per)),
                byId<Attr>(j.getString("stat")), byId<Heft>(j.getString("weight")),
                j.date("start"), j.date("end"), j.optJSONArray("days")?.let { d -> (0 until d.length()).map(d::getInt).toSet() }, j.str("remind"))
        }
    }
    o.optJSONObject("marks")?.let { m ->
        for (d in m.keys()) m.getJSONObject(d).let { hs ->
            marks[LocalDate.parse(d)] = hs.keys().asSequence().associateWith { id ->
                hs.getJSONArray(id).let { a -> (0 until a.length()).map { a.getJSONArray(it).let { x -> Mark(byId<Outcome>(x.getString(0)), x.getInt(1), x.getInt(2), x.getInt(3), x.optInt(4),
                    // marks from before a carry their habit's attribute; adj below absorbs any that moved since
                    x.optString(5).takeIf { it.isNotEmpty() }?.let { byId<Attr>(it) } ?: habits.find { it.id == id }?.stat) } }.toMutableList() }
            }.toMutableMap()
        }
    }
    o.optJSONArray("diary")?.let { a -> for (i in 0 until a.length()) diary += LocalDate.parse(a.getString(i)) }
    o.optJSONArray("papers")?.let { a -> if (a.length() == 3) for (i in 0..2) papers[i] = a.getString(i) }
    o.optJSONObject("pen")?.let { j ->
        val nib = j.optDouble("nib").toFloat(); val stab = j.optInt("stabPct"); val rub = j.optDouble("rub").toFloat()
        pen = Pen(j.optString("color", pen.color), nib.coerceIn(NIB_MM.start, NIB_MM.endInclusive), (stab / 10 * 10).coerceIn(0, 100),
            rub.coerceIn(RUB_MM.start, RUB_MM.endInclusive), j.optInt("paper", 0), j.optBoolean("seen"), j.optBoolean("palm", true), j.optString("b1", "eraser"), j.optString("b2", "ink"),
            j.optBoolean("move", j.optBoolean("seen") && j.optBoolean("palm", true))) // before move, palm rejection made a finger move the page once a pen had written
    }
    o.optJSONObject("remind")?.let { j -> remind = Remind(j.optBoolean("on"), j.optString("time", "21:00")) }
    sound = o.optBoolean("sound", true)
    backedUp = o.date("backedUp")
    adj = o.optJSONObject("adj")?.let(::walletFrom) ?: adjFor(this)
}

private fun pageJson(p: Page) = JSONObject().put("paper", p.paper).put("strokes", JSONArray(p.strokes.map { st ->
    JSONObject().put("c", st.c).put("w", st.w.toDouble()).put("e", st.e).put("p", JSONArray(st.p.map { it.toDouble() }))
}))

private fun pageFrom(o: JSONObject) = Page(o.getString("paper"), o.getJSONArray("strokes").let { a ->
    (0 until a.length()).map { i ->
        a.getJSONObject(i).let { j -> j.getJSONArray("p").let { p -> InkStroke(j.getString("c"), j.getDouble("w").toFloat(), j.getBoolean("e"), FloatArray(p.length()) { p.getDouble(it).toFloat() }) } }
    }.toMutableList()
})

// ---------- the cloud copy (Sync.kt) ----------

// The state as the cloud keeps it: one field per thing two devices could change apart (a habit, a day's bolts for
// a habit, a part owned, a slot's part), so their changes merge field by field instead of one overwriting the other.
// Keys are dotted paths into the document. Left out: the wallet (each device counts it from the marks, see recount),
// the diary days (they follow the pages), and what belongs to this device (pen, sounds, last backup).
internal fun flat(s: Data): Map<String, Any?> = buildMap {
    put("name", s.name); put("papers", s.papers.toList())
    put("remind", mapOf("on" to s.remind.on, "time" to s.remind.time))
    put("adj", mapOf("bolts" to s.adj.bolts, "nuts" to s.adj.nuts, "stats" to s.adj.stats.mapKeys { it.key.id }, "peak" to s.adj.peak.mapKeys { it.key.id }))
    s.equip.forEach { (k, v) -> put("equip.$k", v) }
    s.owned.forEach { put("owned.$it", true) }
    s.habits.forEachIndexed { i, h ->
        put("habits.${h.id}", mapOf("name" to h.name, "kind" to h.kind.id, "per" to h.per.id, "target" to h.target, "stat" to h.stat.id, "weight" to h.weight.id,
            "start" to h.start?.toString(), "end" to h.end?.toString(), "days" to h.days?.sorted(), "remind" to h.remind, "o" to i))
    }
    s.marks.forEach { (d, hs) -> hs.forEach { (id, ms) -> if (ms.isNotEmpty()) put("marks.$d.$id", ms.map { listOf(it.s.id, it.b, it.p, it.n, it.l, it.a?.id ?: "").joinToString(",") }) } }
}.mapValues { norm(it.value) }

// Numbers as the cloud hands them back (Long), so a field read back compares equal to the one sent.
private fun norm(v: Any?): Any? = when (v) {
    is Number -> v.toLong(); is Map<*, *> -> v.entries.associate { it.key as String to norm(it.value) }; is List<*> -> v.map(::norm); else -> v
}

// A cloud document flattened to the same paths; a day left with no bolts for a habit is no field at all.
internal fun flatOf(o: Map<String, Any?>): Map<String, Any?> = buildMap {
    for ((k, v) in o) when (k) {
        "equip", "owned", "habits" -> (v as? Map<*, *>)?.forEach { (id, x) -> put("$k.$id", norm(x)) }
        "marks" -> (v as? Map<*, *>)?.forEach { (d, hs) -> (hs as? Map<*, *>)?.forEach { (id, x) -> if ((x as? List<*>)?.isNotEmpty() == true) put("marks.$d.$id", norm(x)) } }
        else -> put(k, norm(v))
    }
}

// What to send for the cloud to go from old to new: changed and new fields, and gone (the delete marker) for the rest.
internal fun diff(old: Map<String, Any?>, new: Map<String, Any?>, gone: Any): Map<String, Any?> =
    new.filter { (k, v) -> !old.containsKey(k) || old[k] != v } + (old.keys - new.keys).associateWith { gone }

// Dotted paths back into nested maps, the shape a merging write takes.
@Suppress("UNCHECKED_CAST")
internal fun nest(m: Map<String, Any?>): Map<String, Any?> = mutableMapOf<String, Any?>().also { root ->
    for ((k, v) in m) {
        val ps = k.split(".")
        var cur = root
        for (p in ps.dropLast(1)) cur = cur.getOrPut(p) { mutableMapOf<String, Any?>() } as MutableMap<String, Any?>
        cur[ps.last()] = v
    }
}

// The state from its cloud fields, keeping what belongs to this device; the wallet is counted from the marks.
// An unknown code (a newer app on the other device) throws, and the copy here stays as it was.
internal fun fromFlat(m: Map<String, Any?>, here: Data) = Data().apply {
    name = m["name"] as? String ?: name
    (m["papers"] as? List<*>)?.takeIf { it.size == 3 }?.forEachIndexed { i, c -> papers[i] = c as String }
    (m["remind"] as? Map<*, *>)?.let { remind = Remind(it["on"] == true, it["time"] as? String ?: "21:00") }
    (m["adj"] as? Map<*, *>)?.let { w ->
        fun ints(k: String) = Attr.entries.associateWith { ((w[k] as Map<*, *>)[it.id] as Number).toInt() }.toMutableMap()
        adj = Wallet((w["bolts"] as Number).toInt(), (w["nuts"] as Number).toInt(), ints("stats"), ints("peak"))
    }
    if (m.keys.any { it.startsWith("owned.") }) owned.clear()
    val hs = mutableListOf<Pair<Long, Habit>>()
    for ((k, v) in m) {
        val p = k.split(".")
        when (p[0]) {
            "owned" -> owned += p[1]
            "habits" -> (v as Map<*, *>).let { j ->
                fun str(x: String) = j[x] as String?
                val per = byId<Per>(str("per")!!)
                hs += (j["o"] as Number).toLong() to Habit(p[1], str("name")!!, byId(str("kind")!!), per, (j["target"] as Number).toInt().coerceIn(1, maxTarget(per)),
                    byId(str("stat")!!), byId(str("weight")!!), str("start")?.let(LocalDate::parse), str("end")?.let(LocalDate::parse),
                    (j["days"] as List<*>?)?.map { (it as Number).toInt() }?.toSet(), str("remind"))
            }
            "marks" -> marks.getOrPut(LocalDate.parse(p[1])) { mutableMapOf() }[p[2]] = (v as List<*>).map { x ->
                (x as String).split(",").let { f -> Mark(byId(f[0]), f[1].toInt(), f[2].toInt(), f[3].toInt(), f[4].toInt(), f.getOrNull(5)?.takeIf { it.isNotEmpty() }?.let { byId<Attr>(it) }) }
            }.toMutableList()
        }
    }
    habits += hs.sortedWith(compareBy({ it.first }, { it.second.id })).map { it.second }
    for ((k, v) in m) if (k.startsWith("equip.")) k.removePrefix("equip.").let { slot -> if (slot in equip && (v == null || v in owned)) equip[slot] = v as String? }
    diary += here.diary; pen = here.pen; sound = here.sound; backedUp = here.backedUp
    recount(this)
}

// A page for the cloud: a compact binary of its strokes, gzipped. Points are kept to a tenth of a page unit and
// pressure to a hundredth already (see put in Diary.kt), so short integers hold them exactly.
internal fun pageBytes(p: Page): ByteArray = ByteArrayOutputStream().also { bo ->
    DataOutputStream(GZIPOutputStream(bo)).use { o ->
        o.writeUTF(p.paper); o.writeInt(p.strokes.size)
        p.strokes.forEach { st ->
            o.writeUTF(st.c); o.writeFloat(st.w); o.writeBoolean(st.e); o.writeInt(st.p.size)
            st.p.forEachIndexed { i, v -> o.writeShort(Math.round(v * if (i % 3 == 2) 100 else 10).coerceIn(-32768, 32767)) }
        }
    }
}.toByteArray()

internal fun pageOf(b: ByteArray): Page = DataInputStream(GZIPInputStream(b.inputStream())).use { i ->
    val paper = i.readUTF()
    Page(paper, MutableList(i.readInt()) {
        val c = i.readUTF(); val w = i.readFloat(); val e = i.readBoolean()
        InkStroke(c, w, e, FloatArray(i.readInt()) { k -> i.readShort() / if (k % 3 == 2) 100f else 10f })
    })
}
