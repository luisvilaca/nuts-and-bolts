package com.luisvilaca.nutsandbolts

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

// The saved state lives in one JSON file; each diary page in its own file, since handwriting outgrows the rest within weeks.
class Store private constructor(private val dir: File) {
    var s: Data = load()
        private set
    // bumped on every change; screens read it so they recompose
    var tick by mutableIntStateOf(0)
        private set

    fun change(block: Data.() -> Unit) { s.block(); tick++; save() }

    // the robot's current reaction; not saved
    var react by mutableStateOf<Reaction?>(null)
    fun react(kind: String) { react = Reaction(kind, now()) }
    // the habit whose newest electron is travelling home on the atom; not saved
    var fresh by mutableStateOf<Pair<String, Long>?>(null)

    private val stateFile = File(dir, "state.json")
    private val pagesDir = File(dir, "pages").apply { mkdirs() }

    private fun load(): Data {
        val f = File(dir, "state.json")
        if (!f.exists()) return Data()
        return try { dataFrom(JSONObject(f.readText())) } catch (e: Exception) {
            f.copyTo(File(dir, "state-unreadable-${System.currentTimeMillis()}.json")) // keep the bad copy instead of overwriting it
            Data()
        }
    }

    fun save() = writeAtomic(stateFile, json(s).toString())

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
        s = b.first
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

private fun json(s: Data): JSONObject = JSONObject()
    .put("name", s.name).put("bolts", s.bolts).put("nuts", s.nuts)
    .put("stats", JSONObject(s.stats as Map<*, *>))
    .put("owned", JSONArray(s.owned))
    .put("equip", JSONObject().apply { s.equip.forEach { (k, v) -> put(k, v ?: JSONObject.NULL) } })
    .put("habits", JSONArray(s.habits.map { h ->
        JSONObject().put("id", h.id).put("name", h.name).put("kind", h.kind).put("per", h.per).put("target", h.target)
            .put("stat", h.stat).put("weight", h.weight).put("start", h.start?.toString()).put("end", h.end?.toString())
            .put("days", h.days?.let { JSONArray(it.sorted()) })
    }))
    .put("marks", JSONObject().apply {
        s.marks.forEach { (d, hs) -> put(d.toString(), JSONObject().apply { hs.forEach { (id, ms) -> put(id, JSONArray(ms.map { JSONArray(listOf(it.s, it.b, it.p, it.n)) })) } }) }
    })
    .put("diary", JSONArray(s.diary.map { it.toString() }))
    .put("papers", JSONArray(s.papers))
    .put("pen", JSONObject().put("color", s.pen.color).put("size", s.pen.size).put("stab", s.pen.stab).put("eraser", s.pen.eraser).put("paper", s.pen.paper).put("seen", s.pen.seen))
    .put("remind", JSONObject().put("on", s.remind.on).put("time", s.remind.time))
    .put("backedUp", s.backedUp?.toString())

private fun dataFrom(o: JSONObject) = Data().apply {
    name = o.optString("name", name)
    bolts = o.optInt("bolts").coerceAtLeast(0)
    nuts = o.optInt("nuts").coerceAtLeast(0)
    o.optJSONObject("stats")?.let { j -> stats.keys.forEach { stats[it] = j.optInt(it, 30).coerceIn(0, 100) } }
    o.optJSONArray("owned")?.let { a -> owned.clear(); for (i in 0 until a.length()) owned += a.getString(i) }
    o.optJSONObject("equip")?.let { j -> equip.keys.toList().forEach { k -> equip[k] = j.str(k)?.takeIf { it in owned } ?: equip[k] } }
    o.getJSONArray("habits").let { a ->
        for (i in 0 until a.length()) a.getJSONObject(i).let { j ->
            habits += Habit(j.getString("id"), j.getString("name"), j.getString("kind"), j.getString("per"), j.getInt("target").coerceIn(1, 7),
                j.getString("stat").also { require(it in STATS) }, j.getString("weight").also { require(it in WEIGHTS) },
                j.date("start"), j.date("end"), j.optJSONArray("days")?.let { d -> (0 until d.length()).map(d::getInt).toSet() })
        }
    }
    o.optJSONObject("marks")?.let { m ->
        for (d in m.keys()) m.getJSONObject(d).let { hs ->
            marks[LocalDate.parse(d)] = hs.keys().asSequence().associateWith { id ->
                hs.getJSONArray(id).let { a -> (0 until a.length()).map { a.getJSONArray(it).let { x -> Mark(x.getString(0), x.getInt(1), x.getInt(2), x.getInt(3)) } }.toMutableList() }
            }.toMutableMap()
        }
    }
    o.optJSONArray("diary")?.let { a -> for (i in 0 until a.length()) diary += LocalDate.parse(a.getString(i)) }
    o.optJSONArray("papers")?.let { a -> if (a.length() == 3) for (i in 0..2) papers[i] = a.getString(i) }
    o.optJSONObject("pen")?.let { j -> pen = Pen(j.optString("color", pen.color), j.optInt("size", 1), j.optInt("stab", 1), j.optInt("eraser", 1), j.optInt("paper", 0), j.optBoolean("seen")) }
    o.optJSONObject("remind")?.let { j -> remind = Remind(j.optBoolean("on"), j.optString("time", "21:00")) }
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
