package com.luisvilaca.nutsandbolts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.graphics.PathMeasure

// The Reactor Workshop's art: the slots as bolts, the day plates, the diary marks and the rooms (the bay). Its other
// half is Parts.kt; the circus flavour has its own of both.

private fun shape(d: String) = PathParser().parsePathString(d).toPath()
// The diary page's marks, in a 22 x 26 box anchored at (11, 24): an atom (three orbits round a nucleus), and a hex
// hole to fill in.
val EMBLEM = Path().apply {
    for (a in listOf(0.0, PI / 3, 2 * PI / 3)) for (i in 0..48) {
        val t = i * 2 * PI / 48
        val x = (11 + 10 * cos(t) * cos(a) - 3.6 * sin(t) * sin(a)).toFloat()
        val y = (13 + 10 * cos(t) * sin(a) + 3.6 * sin(t) * cos(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    addOval(Rect(Offset(11f, 13f), 2.2f))
}
val CELL = shape("M11 24L5.8 21V15L11 12L16.2 15V21Z")

// A slot is a bolt hole seen face on: done is a glowing bolt head driven home, green like the progress bars (the bolts currency's cross on it),
// skipped is a dashed ring with a dash, failed is a sheared coral head split in two, open is an empty tapped hole.
// Each state has its own form; colour only confirms it. drive runs 0..1 as a fresh bolt screws in.
fun DrawScope.bolt(c: Offset, r: Float, slot: Slot, drive: Float = 1f) {
    val a = if (slot.other) 0.45f else 1f
    val w = r * 0.16f
    when (slot.s) {
        Outcome.Done -> {
            val h = r * (0.6f + 0.4f * drive)
            drawCircle(W.glow, h, c, alpha = a)
            drawCircle(W.joint, h - w / 2, c, alpha = a, style = Stroke(w))
            rotate(270f * (1 - drive), c) {
                drawLine(W.ground, c - Offset(h * 0.5f, 0f), c + Offset(h * 0.5f, 0f), w * 1.3f, alpha = a)
                drawLine(W.ground, c - Offset(0f, h * 0.5f), c + Offset(0f, h * 0.5f), w * 1.3f, alpha = a)
            }
        }
        Outcome.Skip -> {
            drawCircle(W.dim, r - w / 2, c, alpha = a, style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.45f, r * 0.35f))))
            drawLine(W.dim, c - Offset(r * 0.4f, 0f), c + Offset(r * 0.4f, 0f), w, alpha = a)
        }
        Outcome.Fail -> for ((start, d) in listOf(-45f to 1f, 135f to -1f)) {
            val o = Offset(r * 0.16f * d, r * 0.16f * d)
            drawArc(W.coral, start, 180f, true, c - Offset(r, r) * 0.9f + o, Size(r * 1.8f, r * 1.8f), alpha = a)
        }
        null -> {
            drawCircle(W.rail, r, c, alpha = a)
            drawCircle(W.dim, r - w / 2, c, alpha = a, style = Stroke(w))
            drawCircle(W.dim, r * 0.4f, c, alpha = a * 0.6f, style = Stroke(w * 0.7f))
        }
    }
}

@Composable
fun SlotMark(slot: Slot, size: Dp = 26.dp, modifier: Modifier = Modifier) =
    Canvas(modifier.size(size)) { bolt(center, this.size.minDimension * 0.4f, slot) }

// A day as a small plate with three bolt holes: one, two or three driven home (any done, half, all), a sheared bolt
// when something failed; a dashed blank plate while the day is ahead.
@Composable
fun DayMark(p: Progress, future: Boolean, modifier: Modifier = Modifier, today: Boolean = false) = Canvas(modifier) {
    if (today) drawCircle(W.act, size.minDimension * 0.47f, center, style = Stroke(1.5.dp.toPx()))
    val pw = size.width * 0.72f; val ph = size.height * 0.36f
    val tl = center - Offset(pw / 2, ph / 2)
    if (future || p.slots == 0) {
        drawRoundRect(W.edge, tl, Size(pw, ph), CornerRadius(ph * 0.25f),
            style = Stroke(size.width * 0.035f, pathEffect = if (future) PathEffect.dashPathEffect(floatArrayOf(4f, 4f)) else null))
        return@Canvas
    }
    drawRoundRect(W.raised, tl, Size(pw, ph), CornerRadius(ph * 0.25f))
    drawRoundRect(W.joint, tl, Size(pw, ph), CornerRadius(ph * 0.25f), style = Stroke(size.width * 0.03f))
    val share = min(1f, p.done.toFloat() / p.slots)
    val n = when { share >= 1f -> 3; share >= 0.5f -> 2; p.done > 0 -> 1; else -> 0 }
    for (i in 0 until 3) {
        val st = when { i < n -> Outcome.Done; i == 2 && p.fail > 0 -> Outcome.Fail; else -> null }
        bolt(Offset(center.x + (i - 1) * pw * 0.3f, center.y), size.width * 0.085f, Slot(st, false))
    }
}


// Each base is its own room: wall, floor and the colour of the light the reactor feeds, plus what hangs on the wall.
class Room(val sky: Color, val wall: Color, val floor: Color, val light: Color)
val ROOMS = listOf(
    Room(Color(0xFF13302B), Color(0xFF0B1A1A), Color(0xFF0E2420), W.glow), // Charging Bay: greenhouse teal, uranium glow
    Room(Color(0xFF33231A), Color(0xFF181010), Color(0xFF26180F), Color(0xFFFFB547)), // Workshop: warm wood, amber lamp
    Room(Color(0xFF261B4A), Color(0xFF110C24), Color(0xFF1A1430), Color(0xFFB48CFF)), // Observatory: violet night
    Room(Color(0xFF0B2C33), Color(0xFF071518), Color(0xFF0C2226), Color(0xFF3FF0D0)), // Star Deck: teal station light
    Room(Color(0xFF12291B), Color(0xFF08130C), Color(0xFF0E1D12), Color(0xFF9CFF6A)), // Reactor Hall: uranium green
)

// The whole bay, room and all, fitted into the canvas. Scene space is 1000 x 900; x 40..960 and y 120..880 always
// show, so Today, Shop and the base tiles all frame the same picture. On a canvas narrower than the scene (a phone held
// upright) the empty wall at the sides is cropped to x 60..940, so Rivet and the bench draw bigger. band: room left
// at the top for the nameplate.
fun DrawScope.room(s: Data, t: Long, react: Reaction?, orbits: List<Orbit>, fresh: Pair<String, Long>?, equip: Map<String, String?>, band: Float = 0f, guest: ImageBitmap? = null) {
    val tier = (PART[equip["base"]]?.tier ?: 1).coerceIn(1, ROOMS.size)
    val r = ROOMS[tier - 1]
    val tight = size.width / (size.height - band) < 920f / 760f
    val k = min(size.width / (if (tight) 880f else 920f), (size.height - band) / 760f)
    val ox = size.width / 2 - 500f * k; val oy = band + (size.height - band - 760f * k) / 2 - 120f * k
    val fy = oy + 700f * k
    drawRect(Brush.verticalGradient(listOf(r.sky, r.wall), 0f, fy))
    drawRect(Brush.verticalGradient(listOf(r.floor, W.rail), fy, size.height), Offset(0f, fy), Size(size.width, size.height - fy))
    withTransform({ translate(ox, oy); scale(k, k, Offset.Zero) }) { bay(s, t, react, orbits, fresh, equip, tier, r, guest) }
}

// Rivet on its platform and, beside it, a workbench with one steel frame per habit running today, clamped upright
// with a bolt hole per slot. Under the bench a small reactor powers it all; its Cherenkov glow, the light rail it
// feeds and the gauge on its side follow the Core attribute. A habit just done screws its newest bolt in.
// With no habits yet the bench holds one dashed frame.
private fun DrawScope.bay(s: Data, t: Long, react: Reaction?, orbits: List<Orbit>, fresh: Pair<String, Long>?, equip: Map<String, String?>, tier: Int, r: Room, guest: ImageBitmap?) {
    val core = level(s.stats.getValue(Attr.Core)) / 10f // the reactor burns brighter as Core levels up
    val pulse = if (t == 0L) 1f else 0.85f + 0.15f * sin(t / 700.0).toFloat()
    val bench = 596f
    drawRect(r.light.copy(alpha = 0.14f), Offset(-600f, 692f), Size(2200f, 8f)) // the skirting, lit by the room
    when (tier) { 2 -> workshop(); 3 -> observatory(); 4 -> starDeck(); 5 -> reactorHall(t); else -> chargingBay(s) }
    val heart = Offset(765f, 706f)
    drawCircle(Brush.radialGradient(listOf(W.glow.copy(alpha = (0.08f + 0.18f * core) * pulse), Color.Transparent), heart, 420f), 420f, heart)
    lightRail(r.light, core)
    // Rivet and its platform stand left to share the floor with the bench
    translate(-140f, 0f) {
        SCENE.forEach { paint(it, t) }
        // a buddy joins on the platform's left, and this one steps right to make room
        guest?.let { guest(it, 290f, t) }
        translate(if (guest != null) 330f else 250f, 165f) { scale(1.25f, 1.25f, Offset.Zero) { robot(equip, t, mood(s), react) } }
    }
    benchBase()
    reactor(level(s.stats.getValue(Attr.Core)), core, pulse, t)
    benchTop()

    if (s.habits.isEmpty()) drawRoundRect(W.dim, Offset(750f, bench - 90f), Size(30f, 90f), CornerRadius(6f), style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
    val shown = orbits.take(6)
    val gap = 290f / max(1, shown.size)
    // bolts shrink on a crowded bench so neighbours do not overlap, and tall frames close up their holes so a dozen
    // still stand below the light rail
    val tallest = shown.maxOfOrNull { it.slots.size } ?: 1
    val pitch = min(44f, 190f / tallest)
    val br = min(min(13f, gap * 0.3f), pitch * 0.4f)
    shown.forEachIndexed { k, o ->
        val x = 620f + gap * (k + 0.5f)
        val w = br * 2.6f
        val top = bench - 38f - pitch * (o.slots.size - 1) - (br + 23f) // the top bolt sits as far below the frame head as before
        // a habit with every slot done goes critical: its frame glows uranium green, breathing, specks of light rising off
        // it; the glow ignites as the last bolt seats
        val hot = o.slots.all { it.s == Outcome.Done }
        if (hot) {
            val ignite = if (fresh?.first == o.h.id) ((since(t, fresh.second) - 500f) / 700f).coerceIn(0f, 1f) else 1f
            val beat = if (t == 0L) 1f else 0.8f + 0.2f * sin(t / 450.0 + k).toFloat()
            val g = ignite * beat
            val mid = Offset(x, (top + bench) / 2)
            drawOval(Brush.radialGradient(listOf(RADIANT.copy(alpha = 0.75f * g), Color.Transparent), mid, (bench - top) * 0.9f),
                mid - Offset(w * 3.2f, (bench - top) * 0.9f), Size(w * 6.4f, (bench - top) * 1.8f))
            for ((grow, a) in listOf(28f to 0.12f, 16f to 0.25f, 7f to 0.45f)) drawRoundRect(RADIANT, Offset(x - w / 2 - grow / 2, top - grow / 2), Size(w + grow, bench - top + grow), CornerRadius(6f + grow / 2), alpha = a * g, style = Stroke(grow))
            for (i in 0 until 3) positron(Offset(x, top), w, ((t / 3000.0 + i / 3.0 + k * 0.21) % 1.0).toFloat(), i, ignite)
        }
        drawRoundRect(if (hot) Color(0xFF24562E) else W.raised, Offset(x - w / 2, top), Size(w, bench - top), CornerRadius(6f))
        drawRoundRect(when { hot -> RADIANT; o.slots.any { it.s == Outcome.Fail } -> W.joint; else -> W.steel }, Offset(x - w / 2, top), Size(w, bench - top), CornerRadius(6f), alpha = if (hot) 0.95f else 0.7f, style = Stroke(2.5f))
        drawRect(W.joint, Offset(x - w / 2 - 8f, bench - 12f), Size(w + 16f, 12f)) // the clamp
        val newest = o.slots.indexOfLast { it.s == Outcome.Done && !it.other }
        o.slots.forEachIndexed { j, sl ->
            var drive = 1f
            if (fresh != null && fresh.first == o.h.id && j == newest) drive = min(1f, since(t, fresh.second) / 700f).let { 1 - (1 - it).pow(3) }
            bolt(Offset(x, bench - 38f - pitch * j), br, sl, drive)
        }
    }
}

// A positron rising off a critical frame over its life ph (0..1): a four-point spark that swells and fades as it
// climbs, drifting a little to one side, with a thin trail of light below it and a soft halo.
private fun DrawScope.positron(top: Offset, w: Float, ph: Float, i: Int, ignite: Float) {
    val life = sin(ph * PI.toFloat()) // 0 at birth and death, 1 at mid-flight
    val side = if (i % 2 == 0) 1f else -1f
    val p = top + Offset(side * (w * 0.15f + 14f * ph * ph), -10f - 80f * ph)
    val a = life * ignite
    if (a <= 0.01f) return
    drawLine(Brush.verticalGradient(listOf(RADIANT.copy(alpha = 0.5f * a), Color.Transparent), p.y, p.y + 26f), p, p + Offset(-side * 4f, 26f), 2f, StrokeCap.Round)
    drawCircle(Brush.radialGradient(listOf(RADIANT.copy(alpha = 0.35f * a), Color.Transparent), p, 12f), 12f, p)
    val r = 3f + 4f * life
    rotate(ph * 90f, p) {
        drawPath(Path().apply {
            moveTo(p.x, p.y - r); quadraticBezierTo(p.x, p.y, p.x + r, p.y); quadraticBezierTo(p.x, p.y, p.x, p.y + r)
            quadraticBezierTo(p.x, p.y, p.x - r, p.y); quadraticBezierTo(p.x, p.y, p.x, p.y - r); close()
        }, Color.White, alpha = 0.9f * a)
    }
    drawCircle(RADIANT, r * 0.35f, p, alpha = a)
}

// The light rail hangs on two wires and throws a soft cone of the room's light down onto the bench.
private fun DrawScope.lightRail(light: Color, core: Float) {
    for (x in listOf(650f, 880f)) drawLine(W.joint, Offset(x, -300f), Offset(x, 346f), 3f)
    drawPath(Path().apply { moveTo(624f, 370f); lineTo(906f, 370f); lineTo(934f, 596f); lineTo(596f, 596f); close() },
        Brush.verticalGradient(listOf(light.copy(alpha = 0.12f + 0.1f * core), Color.Transparent), 370f, 596f))
    drawRoundRect(Color(0xFF16302C), Offset(612f, 344f), Size(306f, 22f), CornerRadius(8f))
    drawLine(Color.White.copy(alpha = 0.18f), Offset(620f, 347f), Offset(910f, 347f), 2f)
    drawRoundRect(light, Offset(622f, 362f), Size(286f, 9f), CornerRadius(4.5f), alpha = 0.3f, style = Stroke(12f))
    drawRoundRect(light, Offset(622f, 362f), Size(286f, 9f), CornerRadius(4.5f), alpha = 0.6f + 0.4f * core)
}

private val BENCH = Color(0xFF24443E)
private val RADIANT = W.glow // a completed habit's frame, glowing like uranium glass

private fun DrawScope.benchBase() {
    for (x in listOf(628f, 888f)) {
        drawRect(BENCH, Offset(x, 618f), Size(14f, 150f))
        drawRect(Color.Black.copy(alpha = 0.25f), Offset(x + 9f, 618f), Size(5f, 150f))
    }
}

private fun DrawScope.benchTop() {
    drawRect(Color(0xFF1F4640), Offset(604f, 606f), Size(322f, 16f))
    drawRect(Color(0xFF8FB8AE), Offset(604f, 596f), Size(322f, 10f))
    drawLine(Color.White.copy(alpha = 0.35f), Offset(604f, 597f), Offset(926f, 597f), 2f)
    for (i in 0 until 8) drawCircle(W.joint, 2.5f, Offset(620f + i * 41f, 614f))
}

// The reactor: a glass fusion capsule standing on the floor under the bench. In it a plasma core burns with an orbit
// for every three Core levels (an atom, the diary's emblem), electrons racing round them; coil packs clamp it either
// side, ten lamps on the plinth read the Core level, and pulses of light run up a cable on the bench leg to power the
// bench. Everything turns with t and holds still when t is 0 (animations off).
private fun DrawScope.reactor(lv: Int, core: Float, pulse: Float, t: Long) {
    val c = Offset(765f, 706f)
    val glow = W.glow
    val metal = Brush.horizontalGradient(listOf(Color(0xFF132A26), Color(0xFF4A7A70), Color(0xFF132A26)), 700f, 830f)
    // the plinth, its hazard stripes and the level lamps
    drawRoundRect(Color(0xFF0E2420), Offset(672f, 772f), Size(186f, 22f), CornerRadius(5f))
    clipPath(Path().apply { addRect(Rect(676f, 774f, 854f, 778f)) }) {
        for (i in 0 until 24) drawLine(W.gold, Offset(670f + i * 9f, 780f), Offset(678f + i * 9f, 772f), 3.5f, alpha = 0.5f)
    }
    for (i in 0 until 10) {
        val o = Offset(698f + i * 15f, 786f)
        if (i < lv) drawCircle(glow, 6f, o, alpha = 0.25f * pulse)
        drawCircle(if (i < lv) glow else W.line, 3f, o)
    }
    // the power cable: out of the right coil pack, along the floor and up the bench leg, light running through it
    val cable = Path().apply { moveTo(830f, 742f); cubicTo(860f, 742f, 872f, 760f, 884f, 740f); lineTo(884f, 612f) }
    drawPath(cable, W.rail, style = Stroke(9f, cap = StrokeCap.Round))
    drawPath(cable, glow.copy(alpha = 0.18f + 0.3f * core), style = Stroke(3f, cap = StrokeCap.Round))
    val m = PathMeasure().apply { setPath(cable, false) }
    for (i in 0 until 3) {
        val p = m.getPosition(m.length * ((t / 1400.0 + i / 3.0) % 1.0).toFloat())
        drawCircle(glow, 9f, p, alpha = 0.22f * core + 0.08f)
        drawCircle(Color.White, 2.6f, p, alpha = 0.5f + 0.5f * core)
    }
    // coil packs either side of the capsule
    for (x in listOf(694f, 816f)) {
        drawRoundRect(metal, Offset(x, 670f), Size(20f, 78f), CornerRadius(5f))
        for (j in 0 until 6) drawLine(Color(0xFFB98A4A), Offset(x + 3f, 676f + j * 12f), Offset(x + 17f, 676f + j * 12f), 4f, StrokeCap.Round, alpha = 0.85f)
    }
    // the capsule: dark glass, a halo of Cherenkov light round the core, the core itself and its orbits
    val glass = RoundRect(Rect(714f, 650f, 816f, 764f), CornerRadius(26f))
    drawPath(Path().apply { addRoundRect(glass) }, Brush.verticalGradient(listOf(Color(0xFF07201E), Color(0xFF0B2E2B)), 650f, 764f))
    clipPath(Path().apply { addRoundRect(glass) }) {
        drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = (0.35f + 0.45f * core) * pulse), Color.Transparent), c, 70f), 70f, c)
        val rings = 1 + lv / 3
        val spin = ((t / 17000.0) % 1.0 * 360).toFloat() // in doubles: frame times are too large for floats
        for (k in 0 until rings) {
            val tilt = k * 180f / rings + 30f + spin
            rotate(tilt, c) {
                drawOval(glow, c - Offset(40f, 13f), Size(80f, 26f), alpha = 0.45f + 0.35f * core, style = Stroke(2f))
                val a = ((t / (900.0 + 230 * k)) % 1.0 + k * 0.27) * 2 * PI
                val e = c + Offset(40f * cos(a).toFloat(), 13f * sin(a).toFloat())
                drawCircle(glow, 7f, e, alpha = 0.3f)
                drawCircle(Color.White, 3f, e)
            }
        }
        val r = (11f + 9f * core) * (0.94f + 0.06f * pulse)
        drawCircle(Brush.radialGradient(listOf(Color.White, glow, glow.copy(alpha = 0f)), c, r * 1.9f), r * 1.9f, c)
        drawCircle(Color.White, r * 0.45f, c, alpha = 0.9f)
        drawLine(Color.White.copy(alpha = 0.16f), Offset(730f, 676f), Offset(730f, 740f), 6f, StrokeCap.Round) // reflection
    }
    drawPath(Path().apply { addRoundRect(glass) }, W.steel, alpha = 0.5f, style = Stroke(3f))
    // bolted flanges top and bottom, and the conduit up into the bench
    drawRect(W.joint, Offset(758f, 622f), Size(14f, 20f))
    drawRect(glow, Offset(762f, 622f), Size(6f, 20f), alpha = 0.3f + 0.6f * core * pulse)
    for (y in listOf(638f, 760f)) {
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF9DC2B8), Color(0xFF557F75)), y, y + 14f), Offset(704f, y), Size(122f, 14f), CornerRadius(4f))
        for (i in 0 until 9) drawCircle(Color(0xFF223F39), 2.2f, Offset(712f + i * 13.3f, y + 7f))
    }
    // the pressure gauge on the left coil pack, its needle reading Core
    val g = Offset(682f, 700f)
    drawCircle(Color(0xFFE6F2EE), 15f, g)
    drawCircle(W.joint, 15f, g, style = Stroke(3f))
    drawArc(W.coral, 300f, 60f, false, g - Offset(10f, 10f), Size(20f, 20f), style = Stroke(2.5f))
    val a = (150.0 + 240.0 * core) * PI / 180
    drawLine(Color(0xFF16302C), g, g + Offset(11f * cos(a).toFloat(), 11f * sin(a).toFloat()), 2.5f, StrokeCap.Round)
    drawCircle(Color(0xFF16302C), 2.5f, g)
}

// Charging Bay: panelled walls and the charging station Rivet's platform is plugged into; its screen shows the
// Battery level in five cells.
private fun DrawScope.chargingBay(s: Data) {
    for (x in listOf(90f, 300f, 540f)) drawLine(Color.White.copy(alpha = 0.04f), Offset(x, -300f), Offset(x, 692f), 3f)
    drawLine(Color.White.copy(alpha = 0.04f), Offset(-600f, 430f), Offset(600f, 430f), 3f)
    drawRoundRect(W.raised, Offset(70f, 452f), Size(96f, 150f), CornerRadius(16f))
    drawRoundRect(W.joint, Offset(70f, 452f), Size(96f, 150f), CornerRadius(16f), style = Stroke(3f))
    drawRoundRect(W.rail, Offset(84f, 468f), Size(68f, 44f), CornerRadius(8f))
    val cells = (level(s.stats.getValue(Attr.Battery)) + 1) / 2 // a cell for every two Battery levels
    for (i in 0 until 5) drawRoundRect(if (i < cells) W.glow else W.line, Offset(92f + i * 11f, 478f), Size(8f, 24f), CornerRadius(2f))
    drawCircle(W.rail, 14f, Offset(118f, 556f))
    for (dx in listOf(-5f, 5f)) drawCircle(W.joint, 3f, Offset(118f + dx, 556f))
    drawPath(Path().apply { moveTo(118f, 570f); cubicTo(118f, 680f, 150f, 740f, 220f, 770f) }, W.rail, style = Stroke(9f, cap = StrokeCap.Round))
}

// Workshop: a pegboard with a spanner, a hammer and a screwdriver, bins of parts, hazard tape along the skirting.
private fun DrawScope.workshop() {
    val board = Color(0xFF3A2A20)
    val steel = Color(0xFFCAD5E3)
    drawRoundRect(board, Offset(56f, 190f), Size(176f, 250f), CornerRadius(12f))
    drawRoundRect(Color(0xFF5A4232), Offset(56f, 190f), Size(176f, 250f), CornerRadius(12f), style = Stroke(4f))
    for (i in 0 until 7) for (j in 0 until 10) drawCircle(Color(0xFF22170F), 2.5f, Offset(72f + i * 24f, 204f + j * 24f))
    drawLine(steel, Offset(92f, 246f), Offset(92f, 318f), 10f, StrokeCap.Round)
    drawCircle(steel, 15f, Offset(92f, 236f)); drawRect(board, Offset(86f, 218f), Size(12f, 16f))
    drawCircle(steel, 9f, Offset(92f, 326f)); drawCircle(board, 4f, Offset(92f, 326f))
    drawRoundRect(Color(0xFFA0683C), Offset(139f, 240f), Size(12f, 96f), CornerRadius(5f))
    drawRoundRect(steel, Offset(120f, 226f), Size(50f, 22f), CornerRadius(4f))
    drawRect(Color(0xFF8E9AAE), Offset(120f, 240f), Size(50f, 8f))
    drawRoundRect(Color(0xFFFF9A3C), Offset(186f, 224f), Size(20f, 46f), CornerRadius(8f))
    drawRect(steel, Offset(190f, 270f), Size(12f, 6f)); drawRect(steel, Offset(194f, 276f), Size(4f, 52f))
    listOf(0xFFC8743F, 0xFF3FA9A0, 0xFFE0A62F).forEachIndexed { i, c ->
        val x = 70f + i * 52f
        drawRoundRect(Color(c), Offset(x, 372f), Size(44f, 50f), CornerRadius(6f))
        drawRect(Color.Black.copy(alpha = 0.25f), Offset(x + 8f, 386f), Size(28f, 8f))
    }
    drawRect(Color(0xFFFFC83D), Offset(-600f, 680f), Size(2200f, 14f))
    var x = -600f
    while (x < 1600f) {
        drawPath(Path().apply { moveTo(x, 680f); lineTo(x + 14f, 680f); lineTo(x + 4f, 694f); lineTo(x - 10f, 694f); close() }, Color(0xFF1A1410))
        x += 28f
    }
}

private val STARS = listOf(Offset(-60f, -40f), Offset(-20f, -70f), Offset(30f, -50f), Offset(70f, -10f), Offset(-75f, 20f), Offset(-40f, 55f),
    Offset(10f, 70f), Offset(55f, 50f), Offset(-10f, -15f), Offset(85f, -45f), Offset(-85f, -15f), Offset(25f, 20f))

// Observatory: a bolted porthole between the rail's wires onto a violet nebula and a ringed sunset planet.
private fun DrawScope.observatory() {
    val c = Offset(765f, 222f)
    drawCircle(Color(0xFF3A3360), 114f, c)
    clipPath(Path().apply { addOval(Rect(c, 104f)) }) {
        drawCircle(Brush.radialGradient(listOf(Color(0xFF3B1E6E), Color(0xFF0A0618)), c + Offset(-30f, -30f), 160f), 104f, c)
        drawCircle(Brush.radialGradient(listOf(Color(0x66FF5FD2), Color.Transparent), c + Offset(-40f, -20f), 80f), 80f, c + Offset(-40f, -20f))
        drawCircle(Brush.radialGradient(listOf(Color(0x444CC9FF), Color.Transparent), c + Offset(45f, 40f), 70f), 70f, c + Offset(45f, 40f))
        STARS.forEachIndexed { i, o -> drawCircle(Color.White, if (i % 4 == 0) 2.6f else 1.6f, c + o, alpha = 0.85f) }
        // the ring passes behind the planet, then in front
        val p = c + Offset(30f, -18f)
        rotate(-18f, p) { drawArc(Color(0xFFFFE0B0), 180f, 180f, false, p - Offset(58f, 13f), Size(116f, 26f), alpha = 0.8f, style = Stroke(4f)) }
        drawCircle(Brush.linearGradient(listOf(Color(0xFFFFB86B), Color(0xFFE0568A)), p - Offset(30f, 30f), p + Offset(30f, 30f)), 32f, p)
        rotate(-18f, p) { drawArc(Color(0xFFFFE0B0), 0f, 180f, false, p - Offset(58f, 13f), Size(116f, 26f), alpha = 0.9f, style = Stroke(4f)) }
        drawArc(Color.White, 200f, 50f, false, c - Offset(92f, 92f), Size(184f, 184f), alpha = 0.12f, style = Stroke(6f))
    }
    for (i in 0 until 12) {
        val a = i * PI / 6
        drawCircle(W.steel, 3f, c + Offset(109f * cos(a).toFloat(), 109f * sin(a).toFloat()), alpha = 0.6f)
    }
}

// Star Deck: a panoramic window onto a planet's limb and its moon, and a console of coloured keys.
private fun DrawScope.starDeck() {
    val frame = Color(0xFF12313A)
    val win = Rect(-600f, 140f, 1600f, 330f)
    clipPath(Path().apply { addRect(win) }) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF02060C), Color(0xFF06223A)), 140f, 330f), win.topLeft, win.size)
        for (i in 0 until 40) drawCircle(Color.White, if (i % 5 == 0) 2.4f else 1.4f, Offset((i * 137 % 1000).toFloat(), 150f + i * 53 % 160), alpha = 0.8f)
        val pc = Offset(320f, 820f)
        drawCircle(Brush.verticalGradient(listOf(Color(0xFF2A9FD6), Color(0xFF0B3A5A)), 280f, 420f), 540f, pc)
        drawCircle(Color(0xFF7FF5FF), 540f, pc, alpha = 0.5f, style = Stroke(6f))
        drawCircle(Color(0xFFDDE6F0), 13f, Offset(860f, 190f))
    }
    drawRect(frame, Offset(-600f, 130f), Size(2200f, 12f))
    drawRect(frame, Offset(-600f, 328f), Size(2200f, 12f))
    for (x in listOf(100f, 280f, 460f, 640f, 820f)) drawRect(frame, Offset(x - 5f, 140f), Size(10f, 190f))
    drawPath(Path().apply { moveTo(56f, 600f); lineTo(196f, 600f); lineTo(210f, 690f); lineTo(44f, 690f); close() }, Color(0xFF14323A))
    drawRoundRect(Color(0xFF041014), Offset(70f, 540f), Size(112f, 66f), CornerRadius(8f))
    drawRoundRect(Color(0xFF3FF0D0), Offset(80f, 550f), Size(92f, 46f), CornerRadius(4f), alpha = 0.18f)
    for (i in 0 until 4) drawLine(Color(0xFF3FF0D0), Offset(86f, 560f + i * 10f), Offset(116f + i * 19 % 50, 560f + i * 10f), 3f, alpha = 0.8f)
    listOf(0xFFF2C14E, 0xFF3FF0D0, 0xFFFF5FD2, 0xFF6EE787).forEachIndexed { i, cl -> drawCircle(Color(cl), 6f, Offset(82f + i * 30f, 642f)) }
}

// Reactor Hall: an arched window onto cooling towers breathing steam at dusk, the radiation sign, and coolant pipes
// running along the wall into the reactor.
private fun DrawScope.reactorHall(t: Long) {
    val frame = Color(0xFF1E3A26)
    val win = Rect(46f, 150f, 218f, 560f)
    val pane = Path().apply { addRoundRect(RoundRect(win, CornerRadius(86f), CornerRadius(86f), CornerRadius(8f), CornerRadius(8f))) }
    clipPath(pane) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0E2A1F), Color(0xFF3F6B3A), Color(0xFFE89A4F)), 150f, 560f), win.topLeft, win.size)
        val tower = Color(0xFF0A1410)
        drawPath(Path().apply { moveTo(60f, 560f); quadraticBezierTo(80f, 470f, 64f, 400f); lineTo(138f, 400f); quadraticBezierTo(122f, 470f, 142f, 560f); close() }, tower)
        drawPath(Path().apply { moveTo(150f, 560f); quadraticBezierTo(164f, 500f, 152f, 450f); lineTo(206f, 450f); quadraticBezierTo(194f, 500f, 208f, 560f); close() }, tower)
        val puff = 0.22f + 0.06f * sin(t / 900.0).toFloat()
        for ((x, y, rr) in listOf(Triple(100f, 380f, 30f), Triple(86f, 340f, 38f), Triple(112f, 296f, 44f), Triple(180f, 430f, 22f), Triple(172f, 400f, 28f)))
            drawCircle(Color.White, rr, Offset(x, y), alpha = puff)
        for (o in listOf(Offset(101f, 400f), Offset(179f, 450f))) drawCircle(Color(0xFFFF5A5A), 3f, o)
    }
    drawPath(pane, frame, style = Stroke(10f))
    drawLine(frame, Offset(132f, 150f), Offset(132f, 560f), 6f)
    drawLine(frame, Offset(46f, 380f), Offset(218f, 380f), 6f)
    for (y in listOf(612f, 640f)) {
        drawRect(Color(0xFF2C4A32), Offset(-600f, y), Size(1236f, 16f))
        drawRect(Color.White.copy(alpha = 0.12f), Offset(-600f, y + 2f), Size(1236f, 3f))
    }
    val v = Offset(540f, 620f)
    drawCircle(Color(0xFFFF8A3D), 16f, v, style = Stroke(5f))
    for (i in 0 until 3) rotate(i * 60f, v) { drawLine(Color(0xFFFF8A3D), v - Offset(14f, 0f), v + Offset(14f, 0f), 3f) }
    val ink = Color(0xFF1A1410)
    drawRoundRect(Color(0xFFFFC83D), Offset(504f, 196f), Size(96f, 96f), CornerRadius(12f))
    drawRoundRect(ink, Offset(504f, 196f), Size(96f, 96f), CornerRadius(12f), style = Stroke(5f))
    val c = Offset(552f, 244f)
    for (mid in listOf(-90f, 30f, 150f)) drawArc(ink, mid - 30f, 60f, true, c - Offset(36f, 36f), Size(72f, 72f))
    drawCircle(Color(0xFFFFC83D), 11f, c)
    drawCircle(ink, 7f, c)
}

// A habit card's progress (Tracker in Kit.kt): segments, one per slot; done in solid glow green, failed in coral, skipped as
// a dashed outline, open as an empty track. The whole tracker glows once the period is complete.
fun DrawScope.tracker(slots: List<Slot>) {
    val gap = 4.dp.toPx()
    val w = (size.width - gap * (slots.size - 1)) / slots.size
    val h = size.height
    val r = CornerRadius(h / 2)
    if (slots.isNotEmpty() && slots.all { it.s == Outcome.Done }) for ((k, a) in listOf(2.4f to 0.08f, 1.6f to 0.16f))
        drawRoundRect(W.glow, Offset(0f, -h * (k - 1) / 2), Size(size.width, h * k), CornerRadius(h * k / 2), alpha = a)
    slots.forEachIndexed { i, sl ->
        val o = Offset(i * (w + gap), 0f)
        val a = if (sl.other) 0.5f else 1f
        when (sl.s) {
            Outcome.Done -> drawRoundRect(W.glow, o, Size(w, h), r, alpha = a)
            Outcome.Fail -> drawRoundRect(W.coral, o, Size(w, h), r, alpha = a)
            Outcome.Skip -> drawRoundRect(W.dim, o, Size(w, h), r, alpha = a, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
            null -> drawRoundRect(W.edge, o, Size(w, h), r)
        }
    }
}

// A card's accent (Card in Kit.kt): a 3dp bar of the colour down its left edge.
fun DrawScope.cardAccent(c: Color) = drawRect(c, size = Size(3.dp.toPx(), size.height))
