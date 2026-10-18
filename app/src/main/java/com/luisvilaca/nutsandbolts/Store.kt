package com.luisvilaca.nutsandbolts

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
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

    fun change(block: Data.() -> Unit) { data.block(); tick++; save() }

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
    fun flush() = runBlocking(disk) { waiting.getAndSet(null)?.let { writeAtomic(stateFile, it) } }

    fun page(d: LocalDate): Page? = File(pagesDir, "$d.json").takeIf { it.exists() }?.let { pageFrom(JSONObject(it.readText())) }
    // runs off the main thread; the caller marks the day in diary afterwards
    fun writePage(d: LocalDate, p: Page) {
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

// The saved state's shape; files without "v" are version 1.
const val SCHEMA = 2

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
            .put("days", h.days?.let { JSONArray(it.sorted()) })
    }))
    .put("marks", JSONObject().apply {
        s.marks.forEach { (d, hs) -> put(d.toString(), JSONObject().apply { hs.forEach { (id, ms) -> put(id, JSONArray(ms.map { JSONArray(listOf(it.s.id, it.b, it.p, it.n, it.l)) })) } }) }
    })
    .put("diary", JSONArray(s.diary.map { it.toString() }))
    .put("papers", JSONArray(s.papers))
    .put("pen", JSONObject().put("color", s.pen.color).put("nib", s.pen.nib.toDouble()).put("stabPct", s.pen.stab).put("rub", s.pen.rub.toDouble()).put("paper", s.pen.paper).put("seen", s.pen.seen).put("palm", s.pen.palm).put("b1", s.pen.b1).put("b2", s.pen.b2))
    .put("remind", JSONObject().put("on", s.remind.on).put("time", s.remind.time))
    .put("sound", s.sound)
    .put("backedUp", s.backedUp?.toString())

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
                j.date("start"), j.date("end"), j.optJSONArray("days")?.let { d -> (0 until d.length()).map(d::getInt).toSet() })
        }
    }
    o.optJSONObject("marks")?.let { m ->
        for (d in m.keys()) m.getJSONObject(d).let { hs ->
            marks[LocalDate.parse(d)] = hs.keys().asSequence().associateWith { id ->
                hs.getJSONArray(id).let { a -> (0 until a.length()).map { a.getJSONArray(it).let { x -> Mark(byId<Outcome>(x.getString(0)), x.getInt(1), x.getInt(2), x.getInt(3), x.optInt(4)) } }.toMutableList() }
            }.toMutableMap()
        }
    }
    o.optJSONArray("diary")?.let { a -> for (i in 0 until a.length()) diary += LocalDate.parse(a.getString(i)) }
    o.optJSONArray("papers")?.let { a -> if (a.length() == 3) for (i in 0..2) papers[i] = a.getString(i) }
    o.optJSONObject("pen")?.let { j ->
        val nib = j.optDouble("nib").toFloat(); val stab = j.optInt("stabPct"); val rub = j.optDouble("rub").toFloat()
        pen = Pen(j.optString("color", pen.color), nib.coerceIn(NIB_MM.start, NIB_MM.endInclusive), (stab / 10 * 10).coerceIn(0, 100),
            rub.coerceIn(RUB_MM.start, RUB_MM.endInclusive), j.optInt("paper", 0), j.optBoolean("seen"), j.optBoolean("palm", true), j.optString("b1", "eraser"), j.optString("b2", "ink"))
    }
    o.optJSONObject("remind")?.let { j -> remind = Remind(j.optBoolean("on"), j.optString("time", "21:00")) }
    sound = o.optBoolean("sound", true)
    backedUp = o.date("backedUp")
}

private fun pageJson(p: Page) = JSONObject().put("paper", p.paper).put("strokes", JSONArray(p.strokes.map { st ->
    JSONObject().put("c", st.c).put("w", st.w.toDouble()).put("e", st.e).put("p", JSONArray(st.p.map { it.toDouble() }))
}))

private fun pageFrom(o: JSONObject) = Page(o.getString("paper"), o.getJSONArray("strokes").let { a ->
    (0 until a.length()).map { i ->
        a.getJSONObject(i).let { j -> j.getJSONArray("p").let { p -> InkStroke(j.getString("c"), j.getDouble("w").toFloat(), j.getBoolean("e"), FloatArray(p.length()) { p.getDouble(it).toFloat() }) } }
    }.toMutableList()
})
