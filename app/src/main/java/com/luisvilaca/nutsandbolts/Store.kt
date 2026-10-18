package com.luisvilaca.nutsandbolts

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.time.LocalDate
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
    private val thumbsDir = File(dir, "thumbs").apply { mkdirs() }

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
        File(thumbsDir, "$d.png").delete()
    }

    // When a page last changed (0 when there is none), so a picture of it can tell it is out of date.
    fun pageStamp(d: LocalDate) = File(pagesDir, "$d.json").lastModified()

    // A page as a small picture for the diary calendar: its paper and ink, no template. Kept as a PNG beside the pages
    // and drawn again when the page is newer. Runs off the main thread.
    fun thumb(d: LocalDate): Bitmap? {
        val f = File(pagesDir, "$d.json")
        if (!f.exists()) return null
        val t = File(thumbsDir, "$d.png")
        if (t.exists() && t.lastModified() >= f.lastModified()) BitmapFactory.decodeFile(t.path)?.let { return it }
        val b = drawThumb(page(d) ?: return null)
        runCatching { t.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        return b
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

internal fun json(s: Data): JSONObject = JSONObject()
    .put("name", s.name).put("bolts", s.bolts).put("nuts", s.nuts)
    .put("stats", JSONObject(s.stats as Map<*, *>))
    .put("peak", JSONObject(s.peak as Map<*, *>))
    .put("owned", JSONArray(s.owned))
    .put("equip", JSONObject().apply { s.equip.forEach { (k, v) -> put(k, v ?: JSONObject.NULL) } })
    .put("habits", JSONArray(s.habits.map { h ->
        JSONObject().put("id", h.id).put("name", h.name).put("kind", h.kind).put("per", h.per).put("target", h.target)
            .put("stat", h.stat).put("weight", h.weight).put("start", h.start?.toString()).put("end", h.end?.toString())
            .put("days", h.days?.let { JSONArray(it.sorted()) })
    }))
    .put("marks", JSONObject().apply {
        s.marks.forEach { (d, hs) -> put(d.toString(), JSONObject().apply { hs.forEach { (id, ms) -> put(id, JSONArray(ms.map { JSONArray(listOf(it.s, it.b, it.p, it.n, it.l)) })) } }) }
    })
    .put("diary", JSONArray(s.diary.map { it.toString() }))
    .put("papers", JSONArray(s.papers))
    .put("pen", JSONObject().put("color", s.pen.color).put("nib", s.pen.nib.toDouble()).put("stabPct", s.pen.stab).put("rub", s.pen.rub.toDouble()).put("paper", s.pen.paper).put("seen", s.pen.seen).put("palm", s.pen.palm).put("b1", s.pen.b1).put("b2", s.pen.b2))
    .put("remind", JSONObject().put("on", s.remind.on).put("time", s.remind.time))
    .put("sound", s.sound)
    .put("backedUp", s.backedUp?.toString())

internal fun dataFrom(o: JSONObject) = Data().apply {
    name = o.optString("name", name)
    bolts = o.optInt("bolts").coerceAtLeast(0)
    nuts = o.optInt("nuts").coerceAtLeast(0)
    o.optJSONObject("stats")?.let { j -> stats.keys.forEach { stats[it] = j.optInt(it, 30).coerceAtLeast(0) } }
    // files from before levels have no peak: an attribute has at least reached the level it is at
    stats.keys.forEach { peak[it] = maxOf(level(stats.getValue(it)), o.optJSONObject("peak")?.optInt(it) ?: 0) }
    o.optJSONArray("owned")?.let { a -> owned.clear(); for (i in 0 until a.length()) owned += a.getString(i) }
    o.optJSONObject("equip")?.let { j -> equip.keys.toList().forEach { k -> equip[k] = j.str(k)?.takeIf { it in owned } ?: equip[k] } }
    o.getJSONArray("habits").let { a ->
        for (i in 0 until a.length()) a.getJSONObject(i).let { j ->
            habits += Habit(j.getString("id"), j.getString("name"), j.getString("kind"), j.getString("per"), j.getInt("target").coerceIn(1, maxTarget(j.getString("per"))),
                j.getString("stat").also { require(it in STATS) }, j.getString("weight").also { require(it in WEIGHTS) },
                j.date("start"), j.date("end"), j.optJSONArray("days")?.let { d -> (0 until d.length()).map(d::getInt).toSet() })
        }
    }
    o.optJSONObject("marks")?.let { m ->
        for (d in m.keys()) m.getJSONObject(d).let { hs ->
            marks[LocalDate.parse(d)] = hs.keys().asSequence().associateWith { id ->
                hs.getJSONArray(id).let { a -> (0 until a.length()).map { a.getJSONArray(it).let { x -> Mark(x.getString(0), x.getInt(1), x.getInt(2), x.getInt(3), x.optInt(4)) } }.toMutableList() }
            }.toMutableMap()
        }
    }
    o.optJSONArray("diary")?.let { a -> for (i in 0 until a.length()) diary += LocalDate.parse(a.getString(i)) }
    o.optJSONArray("papers")?.let { a -> if (a.length() == 3) for (i in 0..2) papers[i] = a.getString(i) }
    o.optJSONObject("pen")?.let { j ->
        // files from before the sliders kept three-step choices (size, stab, eraser); each maps to its nearest new value
        val nib = if (j.has("nib")) j.optDouble("nib").toFloat() else listOf(0.35f, 0.6f, 1f)[j.optInt("size", 1).coerceIn(0, 2)]
        val stab = if (j.has("stabPct")) j.optInt("stabPct") else listOf(0, 60, 90)[j.optInt("stab", 1).coerceIn(0, 2)]
        val rub = if (j.has("rub")) j.optDouble("rub").toFloat() else listOf(2f, 4f, 7f)[j.optInt("eraser", 1).coerceIn(0, 2)]
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

// Ink on its own layer (so the eraser's strokes clear ink, not paper), laid on the paper at a tenth of page size. The
// nib's width is taken at a middling pressure; at this size the difference does not show.
private fun drawThumb(p: Page): Bitmap {
    val k = 0.1f
    val w = (PAGE_W * k).toInt(); val h = (PAGE_H * k).toInt()
    val ink = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = android.graphics.Canvas(ink)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    for (st in p.strokes) {
        paint.color = android.graphics.Color.parseColor(st.c)
        paint.xfermode = if (st.e) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
        paint.strokeWidth = maxOf(0.8f, st.w * (if (st.e) 5f else 1.1f) * k)
        val path = android.graphics.Path()
        for (i in 0 until st.p.size / 3) if (i == 0) path.moveTo(st.p[0] * k, st.p[1] * k) else path.lineTo(st.p[i * 3] * k, st.p[i * 3 + 1] * k)
        if (st.p.size == 3) path.lineTo(st.p[0] * k + 0.01f, st.p[1] * k)
        c.drawPath(path, paint)
    }
    val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(out).apply { drawColor(android.graphics.Color.parseColor(p.paper)); drawBitmap(ink, 0f, 0f, null) }
    ink.recycle()
    return out
}
