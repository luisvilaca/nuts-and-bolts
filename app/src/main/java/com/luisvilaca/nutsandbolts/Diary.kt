package com.luisvilaca.nutsandbolts

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.translate
import android.view.MotionEvent
import android.view.MotionPredictor
import android.os.Build
import android.os.SystemClock
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// The owner's StarNote palette. Matte Black is also the first paper, so it is the ink for light paper.
val PEN_COLORS = listOf("Ice Blue" to "#E0F7FA", "Matrix Green" to "#00FF88", "Electric Cyan" to "#00E5FF", "Neon Fuchsia" to "#FF007F", "Matte Black" to "#393939")
val PAPERS = listOf("#393939", "#F4ECD8", "#0B1020") // three slots the owner can replace with any hex
val NIB_MM = 0.2f..2f // in steps of 0.1
val RUB_MM = 1f..15f // in steps of 1
// the stabilizer's share of the gap to the pen closed per sample: 0% follows the pen exactly, 100% trails it most
private fun follow(pct: Int) = 1f - 0.88f * pct / 100f
// Fountain pen: the nib opens with pressure and closes with speed, easing between the two so the line swells and tapers.
// shortcut: tuned by eye in a desktop browser, retune min/max/speed on the tablet with the real pen
private const val NIB_MIN = 0.2f
private const val NIB_MAX = 1.7f
private const val NIB_SPEED = 0.45f
private const val NIB_EASE = 0.35f
const val PAGE_W = 1600f
const val PAGE_H = 2260f

fun hex(s: String) = Color(android.graphics.Color.parseColor(s))
fun lightPaper(s: String) = hex(s).let { 0.299f * it.red + 0.587f * it.green + 0.114f * it.blue > 0.55f }

@Composable
fun DiaryScreen(store: Store, ui: Ui, lay: Lay) {
    ui.page?.let { d ->
        BackHandler { ui.page = null }
        Editor(store, ui, d)
        return
    }
    val s = store.s
    val t = LocalDate.now()
    var month by remember { mutableStateOf(YearMonth.now()) }
    val n = s.diary.count { YearMonth.from(it) == month }
    val sum = "${if (n == 0) "No pages" else plural(n, "page")} written in ${month.format(DateTimeFormatter.ofPattern("MMMM", Locale.UK))}"
    // like Habits: the calendar fills the screen below the header without scrolling, at any size; today's page is a
    // quiet action beside the month it belongs to
    Column(Modifier.fillMaxSize().padding(horizontal = if (lay.wide) 28.dp else 20.dp)) {
        Header("Diary", sum.takeIf { lay.wide }, store, ui, lay)
        if (!lay.wide) Text(sum, Modifier.padding(bottom = 4.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Stepper(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)), { month = month.minusMonths(1) }, if (month < YearMonth.now()) ({ month = month.plusMonths(1) }) else null)
            Spacer(Modifier.weight(1f))
            TextButton({ ui.page = t }) {
                Icon(ICONS.getValue("diary"), null, Modifier.size(18.dp), tint = W.act)
                Text(if (t in s.diary) "Open today" else "Write today", Modifier.padding(start = 6.dp), color = W.act)
            }
        }
        Card(Modifier.weight(1f).padding(bottom = 8.dp), pad = 12.dp) {
            Month(month, { it, _ -> if (it in s.diary) Icon(ICONS.getValue("diary"), "has a page", Modifier.size(18.dp), tint = W.steel) }, ahead = false, Modifier.weight(1f)) { ui.page = it }
        }
    }
}

// ---------- the pen ----------

// Strokes are kept as vectors in page units, so a page redraws sharp at any size. Finished ink is cached in a bitmap;
// only the stroke under the pen is drawn each frame.
private class Pad(val page: Page) {
    val redo = mutableListOf<InkStroke>()
    var cur: InkStroke? = null
    var pts = FloatArray(0); var n = 0 // the live stroke's points, n floats used
    var bmp: ImageBitmap? = null
    var k = 1f // pixels per page unit
    var ver by mutableIntStateOf(0)

    fun width(s: InkStroke, v: Float) = if (s.e) s.w * 5 else s.w * (NIB_MIN + (NIB_MAX - NIB_MIN) * v)

    // the piece of s ending at point i, curving through midpoints
    fun DrawScope.seg(s: InkStroke, p: FloatArray, i: Int) {
        val col = hex(s.c); val blend = if (s.e) BlendMode.Clear else BlendMode.SrcOver
        if (i == 0) { drawCircle(col, width(s, p[2]) / 2, Offset(p[0], p[1]), blendMode = blend); return }
        val a = (i - 1) * 3; val b = i * 3
        val path = Path()
        if (i == 1) { path.moveTo(p[0], p[1]); path.lineTo((p[a] + p[b]) / 2, (p[a + 1] + p[b + 1]) / 2) }
        else { path.moveTo((p[a - 3] + p[a]) / 2, (p[a - 2] + p[a + 1]) / 2); path.quadraticTo(p[a], p[a + 1], (p[a] + p[b]) / 2, (p[a + 1] + p[b + 1]) / 2) }
        drawPath(path, col, style = Stroke(width(s, (p[a + 2] + p[b + 2]) / 2), cap = StrokeCap.Round), blendMode = blend)
    }
    // seg stops at the last midpoint; this closes the stroke to its final point
    fun DrawScope.tail(s: InkStroke, p: FloatArray, count: Int) {
        val b = count * 3 - 3; val a = b - 3
        if (a < 0) return
        drawLine(hex(s.c), Offset((p[a] + p[b]) / 2, (p[a + 1] + p[b + 1]) / 2), Offset(p[b], p[b + 1]), width(s, p[b + 2]), StrokeCap.Round, blendMode = if (s.e) BlendMode.Clear else BlendMode.SrcOver)
    }
    fun DrawScope.whole(s: InkStroke) { val c = s.p.size / 3; for (i in 0 until c) seg(s, s.p, i); tail(s, s.p, c) }

    fun onBitmap(density: androidx.compose.ui.unit.Density, block: DrawScope.() -> Unit) {
        val b = bmp ?: return
        CanvasDrawScope().draw(density, androidx.compose.ui.unit.LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(b), Size(b.width.toFloat(), b.height.toFloat())) {
            scale(k, k, Offset.Zero) { block() }
        }
    }
    fun redraw(density: androidx.compose.ui.unit.Density) {
        bmp?.let { b -> CanvasDrawScope().draw(density, androidx.compose.ui.unit.LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(b), Size(b.width.toFloat(), b.height.toFloat())) { drawRect(Color.Black, blendMode = BlendMode.Clear) } }
        onBitmap(density) { page.strokes.forEach { whole(it) } }
        ver++
    }
    fun resize(w: Int, h: Int, density: androidx.compose.ui.unit.Density) {
        if (w == 0) return
        // stay inside what tablet GPUs take as one texture and about 12 megapixels
        val d = min(1f, min(4096f / h, kotlin.math.sqrt(12e6f / (w.toFloat() * h))))
        val bw = (w * d).toInt(); val bh = (h * d).toInt()
        if (bmp?.width == bw) return
        bmp = ImageBitmap(bw, bh)
        k = bw / PAGE_W
        redraw(density)
    }
}

// near: when the pen was last seen, hovering or writing; buttons: the pen's side buttons as last reported;
// pred: where the pen is predicted to be a few milliseconds ahead, in page units, drawn ahead of the live stroke.
private class PenState {
    var sm = FloatArray(2); var last = FloatArray(3); var lastT = 0L; var nib = 0f; var pid = PointerId(-1); var pen = false
    var near = 0L; var buttons = 0; var pred: Offset? = null
}

// The open page's handler for pen buttons that arrive as keys (see MainActivity).
object PenKeys { var on: ((Int) -> Unit)? = null }

private fun MotionEvent.penIndex() = (0 until pointerCount).firstOrNull { getToolType(it) == MotionEvent.TOOL_TYPE_STYLUS || getToolType(it) == MotionEvent.TOOL_TYPE_ERASER }

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Editor(store: Store, ui: Ui, date: LocalDate) {
    val s = store.s
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val pad = remember(date) { Pad(store.page(date) ?: Page(s.papers.getOrElse(s.pen.paper) { PAPERS[0] }, mutableListOf())) }
    var erase by remember { mutableStateOf(false) }
    var pop by remember { mutableStateOf<String?>(null) }
    var bin by remember { mutableStateOf(false) }
    var live by remember { mutableIntStateOf(0) }
    var ring by remember { mutableStateOf<Offset?>(null) }
    var px by remember { mutableStateOf(IntSize.Zero) }
    val ps = remember { PenState() }
    val ctx = LocalContext.current
    val view = LocalView.current
    val mmPx = ctx.resources.displayMetrics.xdpi / 25.4f // screen pixels in a millimetre
    val scroll = rememberScrollState()
    // Android 14 and later predict the pen a few milliseconds ahead; the live stroke reaches for that point
    val predictor = remember { if (Build.VERSION.SDK_INT >= 34) MotionPredictor(ctx) else null }
    fun penNear() = SystemClock.uptimeMillis() - ps.near < 1500
    // with palm rejection on and the pen near, a touch on the tools is taken for the palm and does nothing
    val touchOk = { !(s.pen.palm && s.pen.seen && penNear()) }

    fun persist() {
        val p = Page(pad.page.paper, pad.page.strokes.toMutableList())
        scope.launch {
            withContext(Dispatchers.IO) { store.writePage(date, p) }
            store.change { if (p.strokes.isEmpty()) diary -= date else diary += date }
        }
    }
    // keep the ink readable: light paper takes the black ink, dark paper the ice blue
    fun setPaper(c: String) {
        pad.page.paper = c
        val light = lightPaper(c); val ice = PEN_COLORS[0].second; val black = PEN_COLORS[4].second
        if (s.pen.color == (if (light) ice else black)) store.change { pen.color = if (light) black else ice }
        if (pad.page.strokes.isNotEmpty()) persist()
        pad.ver++
    }
    fun put(x: Float, y: Float, v: Float) {
        val n = pad.n
        if (n >= 3 && hypot(x - pad.pts[n - 3], y - pad.pts[n - 2]) < 0.5f) return
        if (n + 3 > pad.pts.size) pad.pts = pad.pts.copyOf(max(64, pad.pts.size * 2))
        pad.pts[n] = Math.round(x * 10) / 10f; pad.pts[n + 1] = Math.round(y * 10) / 10f; pad.pts[n + 2] = Math.round(v * 100) / 100f
        pad.n = n + 3
        val st = pad.cur!!
        if (st.e) pad.onBitmap(density) { with(pad) { seg(st, this@with.pts, this@with.n / 3 - 1) } }
        live++
    }
    fun undoStroke() { pad.page.strokes.removeLastOrNull()?.let { pad.redo += it; pad.redraw(density); persist() } }
    fun nextInk() { val i = PEN_COLORS.indexOfFirst { it.second == s.pen.color }; erase = false; store.change { pen.color = PEN_COLORS[(i + 1) % PEN_COLORS.size].second } }
    fun press(b: Int) {
        pop = null
        when (if (b == 1) s.pen.b1 else s.pen.b2) { "eraser" -> erase = !erase; "ink" -> nextInk(); "undo" -> undoStroke() }
    }
    DisposableEffect(Unit) { PenKeys.on = ::press; onDispose { PenKeys.on = null } }

    fun finish(lift: Boolean, x: Float, y: Float) {
        val st = pad.cur ?: return
        if (pad.n == 3) pad.pts[2] = ps.last[2] // a tap is a full dot, not a closed nib
        else if (lift) put(x, y, ps.nib * 0.35f) // catch up to where the pen lifted, tapering out
        val done = InkStroke(st.c, st.w, st.e, pad.pts.copyOf(pad.n))
        pad.onBitmap(density) { with(pad) { whole(done) } }
        pad.page.strokes += done
        pad.redo.clear(); pad.cur = null; pad.n = 0
        live++
        persist()
    }

    Row(Modifier.fillMaxSize()) {
        // The page fills what the rail leaves and scrolls. With palm rejection on (Settings), once a pen has been used a
        // finger drags the page instead of drawing, and a palm near the pen does neither.
        Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(scroll)) {
            Box(Modifier.fillMaxWidth().aspectRatio(PAGE_W / PAGE_H).background(hex(pad.page.paper)).onSizeChanged { px = it; pad.resize(it.width, it.height, density) }) {
                Template(date, pad.page.paper, s.name, pad.ver)
                Canvas(Modifier.fillMaxSize().semantics { contentDescription = "Diary page for ${date.format(dayFmt)}" }
                    // A pen always writes; once a pen has been used, a finger scrolls (palm rejection). Every pointer is
                    // consumed here while a stroke is live, before the scrolling page sees it, so a line drawn down the
                    // page no longer drags the page and cuts the stroke, and a resting palm neither scrolls nor blocks the pen.
                    .pointerInput(pad) {
                        fun PointerType.pen() = this == PointerType.Stylus || this == PointerType.Eraser
                        awaitPointerEventScope {
                            while (true) {
                                val ev = awaitPointerEvent()
                                val s = store.s
                                val me = ev.motionEvent
                                if (ev.changes.any { it.type.pen() }) {
                                    ps.near = SystemClock.uptimeMillis()
                                    if (!s.pen.seen) store.change { pen.seen = true }
                                    // a side button newly pressed, hovering or writing, does what Settings gives it
                                    val bs = me?.buttonState ?: 0
                                    val fresh = bs and ps.buttons.inv(); ps.buttons = bs
                                    if (fresh and MotionEvent.BUTTON_STYLUS_PRIMARY != 0) press(1)
                                    if (fresh and MotionEvent.BUTTON_STYLUS_SECONDARY != 0) press(2)
                                    if (me != null && predictor != null) predictor.record(me)
                                }
                                // Palm rejection: with the pen near, or a contact broader than a fingertip, a touch
                                // neither writes nor scrolls the page.
                                if (s.pen.palm && s.pen.seen) {
                                    val palm = penNear() || (me != null && (0 until me.pointerCount).any {
                                        me.getToolType(it) == MotionEvent.TOOL_TYPE_FINGER && me.getTouchMajor(it) > 9 * mmPx
                                    })
                                    if (palm) ev.changes.filter { it.type == PointerType.Touch }.forEach { it.consume() }
                                }
                                val sc = PAGE_W / max(1, px.width)
                                fun mm(v: Float) = v * mmPx * sc
                                fun pt(p: Offset, pr: Float) = floatArrayOf(p.x * sc, p.y * sc, if (ps.pen) max(pr, 0.1f) else 0.5f)
                                if (pad.cur == null) {
                                    // a hovering pen shows the eraser's outline before it touches, when the eraser is in hand
                                    ev.changes.firstOrNull { it.type.pen() && !it.pressed }?.let { h ->
                                        ring = if ((erase || h.type == PointerType.Eraser) && ev.type != PointerEventType.Exit) h.position else null
                                    }
                                    val d = ev.changes.firstOrNull { it.changedToDown() && !it.isConsumed && (it.type.pen() || !(s.pen.palm && s.pen.seen)) } ?: continue
                                    ev.changes.forEach { it.consume() }
                                    ps.pid = d.id; ps.pen = d.type.pen(); ps.pred = null
                                    // the pen's samples come as they happen instead of once a frame, which takes a frame off the lag
                                    if (ps.pen && me != null && Build.VERSION.SDK_INT >= 30) view.requestUnbufferedDispatch(me)
                                    val rubbing = erase || d.type == PointerType.Eraser
                                    pad.cur = InkStroke(s.pen.color, if (rubbing) mm(s.pen.rub) / 5 else mm(s.pen.nib), rubbing, FloatArray(0))
                                    ps.last = pt(d.position, d.pressure); ps.lastT = d.uptimeMillis; ps.sm = floatArrayOf(ps.last[0], ps.last[1])
                                    ps.nib = ps.last[2] * 0.3f // touch down closed, so the stroke tapers in
                                    ring = if (rubbing) d.position else null
                                    put(ps.sm[0], ps.sm[1], ps.nib)
                                    continue
                                }
                                val c = ev.changes.firstOrNull { it.id == ps.pid }
                                ev.changes.forEach { it.consume() }
                                if (c == null || !c.pressed) { ps.pred = null; finish(c != null, (c?.position?.x ?: 0f) * sc, (c?.position?.y ?: 0f) * sc); ring = null; continue }
                                val k = follow(s.pen.stab)
                                // past samples carry no pressure of their own, so it is eased from the last sample to this one
                                val p0 = ps.last[2]; val hs = c.historical
                                for (i in 0..hs.size) {
                                    val h = i < hs.size
                                    val raw = pt(if (h) hs[i].position else c.position, p0 + (c.pressure - p0) * (i + 1) / (hs.size + 1))
                                    val tm = if (h) hs[i].uptimeMillis else c.uptimeMillis
                                    val speed = hypot(raw[0] - ps.last[0], raw[1] - ps.last[1]) / max(1L, tm - ps.lastT) // page units per ms
                                    ps.nib += (min(1f, raw[2] * max(0.45f, 1.15f - NIB_SPEED * speed)) - ps.nib) * NIB_EASE
                                    ps.last = raw; ps.lastT = tm
                                    // stabilizer: the drawn point trails the pen, which irons out jitter; it lets go as the pen speeds
                                    // up, where jitter does not show and lag does
                                    val kk = k + (1f - k) * min(1f, speed / 1.5f)
                                    ps.sm[0] += (raw[0] - ps.sm[0]) * kk; ps.sm[1] += (raw[1] - ps.sm[1]) * kk
                                    put(ps.sm[0], ps.sm[1], ps.nib)
                                }
                                if (pad.cur?.e == true) ring = c.position
                                // where the pen will be next, from the platform's predictor, mapped from window to page units
                                ps.pred = if (predictor == null || me == null || !ps.pen || !predictor.isPredictionAvailable(me.deviceId, me.source)) null else {
                                    val i = me.penIndex()
                                    val p = predictor.predict(System.nanoTime() + 12_000_000L)
                                    val j = p?.penIndex()
                                    if (i == null || p == null || j == null) null
                                    else (c.position + Offset(p.getX(j) - me.getX(i), p.getY(j) - me.getY(i))).let { Offset(it.x * sc, it.y * sc) }
                                }
                            }
                        }
                    }) {
                    pad.ver; live // redraw when either changes
                    pad.bmp?.let { drawImage(it, dstSize = IntSize(size.width.toInt(), size.height.toInt()), srcSize = IntSize(it.width, it.height)) }
                    val st = pad.cur
                    if (st != null && !st.e) scale(size.width / PAGE_W, size.width / PAGE_W, Offset.Zero) { with(pad) { val c = n / 3; for (i in 0 until c) seg(st, pts, i) } }
                    // the eraser's true size under the pen, while it rubs and while it hovers
                    ring?.let { eraserRing(it, s.pen.rub * mmPx / 2) }
                    // the stretch from the last point to where the pen is predicted to be, so the ink keeps up with the nib
                    val pr = ps.pred
                    if (st != null && !st.e && pr != null && pad.n >= 3) scale(size.width / PAGE_W, size.width / PAGE_W, Offset.Zero) {
                        drawLine(hex(st.c), Offset(pad.pts[pad.n - 3], pad.pts[pad.n - 2]), pr, pad.width(st, ps.nib), StrokeCap.Round)
                    }
                }
            }
        }
        // The tools stand in a rail down the right edge, away from the writing hand's sweep: back to the calendar at
        // the top; the pen, the eraser and the inks in the middle; undo, redo, paper and bin at the foot. Menus open to
        // its left. On a short window the inks fold into the current ink's swatch.
        BoxWithConstraints(Modifier.fillMaxHeight().width(56.dp).background(W.rail).padding(vertical = 6.dp)) {
            val short = maxHeight < 600.dp
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
                Tool("calendar", "Back to calendar", false, touchOk) { ui.page = null }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box {
                        // the first tap picks the pen back up from the eraser; a tap on the pen in hand opens its settings
                        Tool("diary", "Pen. Tap again for nib and stabilizer", !erase, touchOk, more = true) { if (erase) erase = false else pop = "pen" }
                        Menu(pop == "pen", { pop = null }) {
                            Setting("Nib", s.pen.nib, NIB_MM, 17, { "%.1f mm".format(it) }, { v -> store.change { pen.nib = v } }) { v ->
                                Canvas(Modifier.fillMaxWidth().height(28.dp)) {
                                    drawLine(hex(s.pen.color), Offset(12f, center.y), Offset(size.width - 12f, center.y), v * mmPx, StrokeCap.Round)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Setting("Stabilizer", s.pen.stab.toFloat(), 0f..100f, 9, { "${it.roundToInt()}%" }, { v -> store.change { pen.stab = v.roundToInt() } }, ticks = true)
                            Text("At 0% the line follows the pen exactly; higher irons out shake on slow strokes. Fast strokes are never held back.", style = MaterialTheme.typography.bodyMedium, color = W.dim)
                        }
                    }
                    Box {
                        Tool("eraser", "Eraser. Tap again for its size", erase, touchOk, more = true, onLong = { erase = true; pop = "eraser" }) { if (erase) pop = "eraser" else { erase = true; pop = null } }
                        Menu(pop == "eraser", { pop = null }) {
                            Setting("Eraser", s.pen.rub, RUB_MM, 13, { "${it.roundToInt()} mm" }, { v -> store.change { pen.rub = v.roundToInt().toFloat() } }) { v ->
                                Canvas(Modifier.fillMaxWidth().height(100.dp)) { eraserRing(center, v * mmPx / 2) }
                            }
                        }
                    }
                    Box(Modifier.padding(vertical = 6.dp).size(22.dp, 1.dp).background(W.line))
                    val inks = @Composable { PEN_COLORS.forEach { (name, c) -> Swatch(c, name, !erase && s.pen.color == c, round = true, touchOk) { erase = false; pop = null; store.change { pen.color = c } } } }
                    if (short) Box {
                        Swatch(s.pen.color, "Ink colour", !erase, round = true, touchOk) { pop = "ink" }
                        Menu(pop == "ink", { pop = null }) { Row { inks() } }
                    } else inks()
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Tool("undo", "Undo stroke", false, touchOk) { undoStroke() }
                    Tool("redo", "Redo stroke", false, touchOk) { pad.redo.removeLastOrNull()?.let { pad.page.strokes += it; pad.redraw(density); persist() } }
                    Box {
                        Tool("paper", "Paper", false, touchOk) { pop = "paper" }
                        Menu(pop == "paper", { pop = null }) {
                            Row(Modifier.padding(bottom = 8.dp)) {
                                s.papers.forEachIndexed { i, c -> Swatch(c, "Paper ${i + 1}", pad.page.paper.equals(c, true), round = false) { store.change { pen.paper = i }; setPaper(c) } }
                            }
                            Papers(store) { i, c -> if (pad.page.paper.equals(s.papers[i], true)) setPaper(c) }
                        }
                    }
                    Tool("trash", "Bin this page", false, touchOk) { if (pad.page.strokes.isNotEmpty()) bin = true }
                }
            }
        }
    }
    if (bin) AlertDialog(onDismissRequest = { bin = false }, title = { Text("Bin this page?") },
        text = { Text("Everything written on it is erased and cannot be brought back.") },
        confirmButton = { TextButton({ pad.page.strokes.clear(); pad.redo.clear(); pad.redraw(density); persist(); bin = false }) { Text("Bin it", color = W.coral) } },
        dismissButton = { TextButton({ bin = false }) { Text("Keep it", color = W.text) } })
}

// Remembers what kind of pointer last pressed a control, so a palm on it can be told from the pen or a finger.
private fun Modifier.lastPointer(into: Array<PointerType?>) = pointerInput(Unit) {
    awaitPointerEventScope { while (true) awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull()?.let { into[0] = it.type } }
}

// Tool buttons are borderless; the tool in hand gets a raised tile and a short green bar on its inner (left) side.
// more: a corner tick for a button with settings behind a second tap or a long press.
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tool(icon: String, label: String, on: Boolean, touchOk: () -> Boolean = { true }, more: Boolean = false, onLong: (() -> Unit)? = null, onClick: () -> Unit) {
    val kind = remember { arrayOfNulls<PointerType>(1) }
    val ok = { kind[0] != PointerType.Touch || touchOk() }
    Box(Modifier.size(44.dp, 40.dp).clip(RoundedCornerShape(10.dp)).background(if (on) W.raised else Color.Transparent).lastPointer(kind)
        .combinedClickable(onClickLabel = label, onLongClick = onLong?.let { l -> { if (ok()) l() } }, onClick = { if (ok()) onClick() }).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Icon(ICONS.getValue(icon), null, Modifier.size(22.dp), tint = if (on) W.text else W.dim)
        if (on) Box(Modifier.align(Alignment.CenterStart).padding(start = 2.dp).size(2.dp, 14.dp).background(W.act))
        if (more) Canvas(Modifier.align(Alignment.BottomEnd).padding(5.dp).size(4.dp)) {
            drawPath(Path().apply { moveTo(size.width, 0f); lineTo(size.width, size.height); lineTo(0f, size.height); close() }, W.dim)
        }
    }
}

@Composable
private fun Swatch(c: String, label: String, on: Boolean, round: Boolean, touchOk: () -> Boolean = { true }, onClick: () -> Unit) {
    val shape = if (round) CircleShape else RoundedCornerShape(6.dp)
    val kind = remember { arrayOfNulls<PointerType>(1) }
    Box(Modifier.size(40.dp, 36.dp).clip(RoundedCornerShape(10.dp)).lastPointer(kind).clickable(onClickLabel = label) { if (kind[0] != PointerType.Touch || touchOk()) onClick() }.semantics { contentDescription = label; selected = on }, contentAlignment = Alignment.Center) {
        Box(Modifier.size(if (on) 26.dp else 22.dp).clip(shape).background(hex(c))
            .border(if (on) 2.dp else 1.dp, if (on) W.text else W.edge, shape))
    }
}

@Composable
private fun Menu(open: Boolean, close: () -> Unit, content: @Composable () -> Unit) =
    DropdownMenu(open, close, offset = androidx.compose.ui.unit.DpOffset((-344).dp, (-40).dp), containerColor = W.raised, shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp).width(300.dp)) { content() } }

// A labelled slider with its value at the right. It moves freely under the finger and saves once, on release.
@Composable
private fun Setting(title: String, value: Float, range: ClosedFloatingPointRange<Float>, steps: Int, show: (Float) -> String, save: (Float) -> Unit,
                    ticks: Boolean = false, preview: @Composable (Float) -> Unit = {}) {
    var v by remember(value) { mutableFloatStateOf(value) }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title.uppercase(), Modifier.weight(1f), style = Label)
        Text(show(v), style = Numeral.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize), color = W.text)
    }
    preview(v)
    Slider(v, { v = it }, valueRange = range, steps = steps, onValueChangeFinished = { save(v) },
        colors = SliderDefaults.colors(thumbColor = W.act, activeTrackColor = W.act, inactiveTrackColor = W.line,
            activeTickColor = if (ticks) W.onAct else Color.Transparent, inactiveTickColor = if (ticks) W.dim else Color.Transparent))
}

// The eraser's outline: a dark ring around a white one over a faint fill, so it reads on dark and light paper alike.
private fun DrawScope.eraserRing(c: Offset, r: Float) {
    drawCircle(Color.White.copy(alpha = 0.12f), r, c)
    drawCircle(Color.Black.copy(alpha = 0.6f), r, c, style = Stroke(3.dp.toPx()))
    drawCircle(Color.White, r, c, style = Stroke(1.5.dp.toPx()))
}

// Typing a hex value into a paper slot; the page on screen follows when it uses that slot.
@Composable
private fun Papers(store: Store, changed: (Int, String) -> Unit) {
    Text("PAPER COLOURS", Modifier.padding(bottom = 8.dp), style = Label)
    store.s.papers.forEachIndexed { i, c ->
        var txt by remember(c) { mutableStateOf(c) }
        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(hex(c)).border(1.dp, W.edge, RoundedCornerShape(6.dp)))
            OutlinedTextField(txt, { v ->
                txt = "#" + v.filter { it.isLetterOrDigit() }.uppercase().filter { it in "0123456789ABCDEF" }.take(6)
                if (txt.length == 7) { changed(i, txt); store.change { papers[i] = txt } }
            }, Modifier.padding(start = 12.dp).weight(1f), singleLine = true, label = { Text("Paper ${i + 1}") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = W.act, unfocusedBorderColor = W.edge, cursorColor = W.act))
        }
    }
    OutlinedButton({ store.change { PAPERS.forEachIndexed { i, c -> papers[i] = c } } }, Modifier.padding(top = 8.dp), border = BorderStroke(1.dp, W.edge)) { Text("Restore the originals", color = W.text) }
}

// The printed page: weekday and date, a five-cell gauge to fill in by hand, the day number and this look's emblem,
// ruled lines, a large faint emblem near the foot, and the log's catalog number.
@Composable
private fun Template(date: LocalDate, paper: String, name: String, ver: Int) {
    val tm = rememberTextMeasurer()
    val light = lightPaper(paper)
    val head = if (light) Color(0xFF393939) else Color(0xFF00FF88)
    val sub = if (light) Color(0xBF393939) else Color(0xB8E0F7FA)
    val rule = if (light) Color(0x38393939) else Color(0x26FFFFFF)
    val art = if (light) Color(0x21393939) else Color(0x3300E5FF)
    val doy = date.dayOfYear
    Canvas(Modifier.fillMaxSize()) {
        ver
        withTransform({ scale(size.width / PAGE_W, size.width / PAGE_W, Offset.Zero) }) {
            // a bolt: the page's emblem; the gauge is five hex holes to fill in
            fun shape(p: androidx.compose.ui.graphics.Path, cx: Float, cy: Float, k: Float, c: Color, w: Float) =
                withTransform({ translate(cx, cy); scale(k, k, Offset.Zero); translate(-11f, -24f) }) { drawPath(p, c, style = Stroke(w / k)) }
            fun emblem(cx: Float, cy: Float, r: Float, c: Color, w: Float) = shape(EMBLEM, cx, cy + r * 0.6f, r / 14f, c, w)
            fun cell(at: Offset, r: Float, c: Color) = shape(CELL, at.x, at.y + r, r / 9f, c, 2f)
            emblem(1290f, PAGE_H - 330, 250f, art, 2f)
            for (i in 0 until 37) drawLine(rule, Offset(90f, 220f + i * 55), Offset(1510f, 220f + i * 55), 1.5f)
            drawLine(head, Offset(90f, 160f), Offset(1510f, 160f), 2.5f)
            val lbl = TextStyle(fontFamily = display, fontSize = 15.sp, letterSpacing = 0.16.em, color = sub)
            val txt = { s: String, st: TextStyle, x: Float, base: Float, end: Boolean ->
                // sizes are in page units; the canvas is already scaled to the page
                val r = tm.measure(s, st.copy(fontSize = (st.fontSize.value / (density * fontScale)).sp))
                drawText(r, topLeft = Offset(if (end) x - r.size.width else x, base - r.firstBaseline))
            }
            txt(date.format(DateTimeFormatter.ofPattern("EEEE", Locale.UK)), TextStyle(fontFamily = display, fontSize = 50.sp, color = head), 88f, 100f, false)
            txt(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)), TextStyle(fontFamily = display, fontSize = 24.sp, color = sub), 90f, 138f, false)
            txt("BOLTS", lbl, 1014f, 58f, false)
            for (i in 0 until 5) cell(Offset(1028f + i * 46, 88f), 15f, sub)
            txt("DAY $doy", lbl, 1392f, 96f, true)
            emblem(1462f, 90f, 44f, head, 2f)
            txt("${name.uppercase()}'S LOG", lbl, 90f, PAGE_H - 18, false)
            txt("NB-D" + date.toString().replace("-", ""), lbl, 1510f, PAGE_H - 18, true)
        }
    }
}

