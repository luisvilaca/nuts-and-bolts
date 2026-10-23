package com.luisvilaca.nutsandbolts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// The circus's art: the slots as marquee bulbs, the day plates, the diary marks and the rooms (the ring). Its other
// half is Parts.kt; the workshop flavour has its own of both.

// The diary page's marks, in a 22 x 26 box anchored at (11, 24): a big top (striped roof, pennant), and a star to fill in.
val EMBLEM = Path().apply {
    moveTo(11f, 1f); lineTo(11f, 5f); moveTo(11f, 1f); lineTo(15f, 2f); lineTo(11f, 3f) // the pennant
    moveTo(1.5f, 13f); lineTo(11f, 5f); lineTo(20.5f, 13f) // the roof
    quadraticTo(18.1f, 15f, 15.75f, 13f); quadraticTo(13.4f, 15f, 11f, 13f); quadraticTo(8.6f, 15f, 6.25f, 13f); quadraticTo(3.9f, 15f, 1.5f, 13f)
    moveTo(11f, 5f); lineTo(8f, 13f); moveTo(11f, 5f); lineTo(14f, 13f) // its stripes
    moveTo(3f, 14f); lineTo(3f, 24f); lineTo(19f, 24f); lineTo(19f, 14f) // the walls
    moveTo(8.5f, 24f); lineTo(11f, 17f); lineTo(13.5f, 24f) // the door flap
}
val CELL = Path().apply {
    for (i in 0 until 10) {
        val a = -PI / 2 + i * PI / 5; val r = if (i % 2 == 0) 6.4 else 2.7
        val x = (11 + r * cos(a)).toFloat(); val y = (18 + r * sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private val FILAMENT = Color(0xFFFFE6A8)

// A slot is a marquee bulb: done is lit (a warm halo, its filament white-hot), skipped is a bulb unscrewed (a dashed
// socket), failed is smashed (coral shards round a dark socket), open is a dark bulb waiting. Each state has its own
// form; colour only confirms it. drive runs 0..1 as a fresh bulb buzzes on, flickering twice before it holds.
fun DrawScope.bulb(c: Offset, r: Float, slot: Slot, drive: Float = 1f, flare: Float = 0f) {
    val a = if (slot.other) 0.45f else 1f
    val w = r * 0.16f
    when (slot.s) {
        Outcome.Done -> {
            val on = if (drive >= 1f) 1f else if (((drive * 6).toInt() % 2) == 0) 0.25f + 0.75f * drive else 1f
            val halo = r * (2.1f + 1.3f * flare)
            drawCircle(Brush.radialGradient(listOf(W.glow.copy(alpha = (0.55f + 0.3f * flare) * on * a), Color.Transparent), c, halo), halo, c)
            drawCircle(FILAMENT, r * 0.86f, c, alpha = a * (0.4f + 0.6f * on))
            drawCircle(Color.White, r * 0.38f, c, alpha = a * on)
        }
        Outcome.Skip -> {
            drawCircle(W.dim, r * 0.86f - w / 2, c, alpha = a, style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.4f, r * 0.34f))))
            drawLine(W.dim, c + Offset(-r * 0.35f, r * 0.35f), c + Offset(r * 0.35f, -r * 0.35f), w, alpha = a)
        }
        Outcome.Fail -> {
            drawCircle(W.rail, r * 0.62f, c, alpha = a)
            val shards = Path().apply {
                for (i in 0 until 14) {
                    val ang = i * PI / 7 + 0.3; val rr = if (i % 2 == 0) r * 1.02f else r * 0.55f
                    val p = c + Offset((rr * cos(ang)).toFloat(), (rr * sin(ang)).toFloat())
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                close()
            }
            drawPath(shards, W.coral, alpha = a, style = Stroke(w * 1.1f))
            drawCircle(W.coral, r * 0.2f, c, alpha = a)
        }
        null -> {
            drawCircle(W.raised, r * 0.86f, c, alpha = a)
            drawCircle(W.dim, r * 0.86f - w / 2, c, alpha = a * 0.7f, style = Stroke(w))
            drawArc(W.dim, 200f, 140f, false, c - Offset(r * 0.35f, r * 0.2f), Size(r * 0.7f, r * 0.5f), alpha = a * 0.5f, style = Stroke(w * 0.7f))
        }
    }
}

@Composable
fun SlotMark(slot: Slot, size: Dp = 26.dp, modifier: Modifier = Modifier) =
    Canvas(modifier.size(size)) { bulb(center, this.size.minDimension * 0.4f, slot) }

// A day as a small marquee plate with three bulbs: one, two or three lit (any done, half, all), a smashed bulb when
// something failed; a dashed blank plate while the day is ahead.
@Composable
fun DayMark(p: Progress, future: Boolean, modifier: Modifier = Modifier, today: Boolean = false) = Canvas(modifier) {
    if (today) drawCircle(W.act, size.minDimension * 0.47f, center, style = Stroke(1.5.dp.toPx()))
    val pw = size.width * 0.74f; val ph = size.height * 0.38f
    val tl = center - Offset(pw / 2, ph / 2)
    if (future || p.slots == 0) {
        drawRoundRect(W.edge, tl, Size(pw, ph), CornerRadius(ph * 0.3f),
            style = Stroke(size.width * 0.035f, pathEffect = if (future) PathEffect.dashPathEffect(floatArrayOf(4f, 4f)) else null))
        return@Canvas
    }
    drawRoundRect(W.raised, tl, Size(pw, ph), CornerRadius(ph * 0.3f))
    drawRoundRect(W.joint, tl, Size(pw, ph), CornerRadius(ph * 0.3f), style = Stroke(size.width * 0.03f))
    val share = min(1f, p.done.toFloat() / p.slots)
    val n = when { share >= 1f -> 3; share >= 0.5f -> 2; p.done > 0 -> 1; else -> 0 }
    for (i in 0 until 3) {
        val st = when { i < n -> Outcome.Done; i == 2 && p.fail > 0 -> Outcome.Fail; else -> null }
        bulb(Offset(center.x + (i - 1) * pw * 0.3f, center.y), size.width * 0.085f, Slot(st, false))
    }
}

// Each stage is its own room: the canvas or wall, the floor, and the colour of its light.
class Room(val sky: Color, val wall: Color, val floor: Color, val light: Color)
val ROOMS = listOf(
    Room(Color(0xFF1E0C1A), Color(0xFF0A050B), Color(0xFF1A0B14), Color(0xFFFFC94A)), // Center Ring: plum canvas, bulb amber
    Room(Color(0xFF141C1A), Color(0xFF070A09), Color(0xFF111614), Color(0xFFB8F0A0)), // Parts & Service: grime, a sick tube light
    Room(Color(0xFF1C0E2E), Color(0xFF09050F), Color(0xFF140A1E), Color(0xFFB48CFF)), // Pirate Cove: violet curtain
    Room(Color(0xFF1E0A0C), Color(0xFF0A0405), Color(0xFF120809), Color(0xFFFFE3B0)), // Show Stage: red velvet, warm white
    Room(Color(0xFF2A0C22), Color(0xFF0C040B), Color(0xFF1C0A16), Color(0xFFFF6FA3)), // Circus Gallery: pink neon
)

// The whole ring, room and all, fitted into the canvas. Scene space is 1000 x 900; x 40..960 and y 120..880 always
// show, so the Ring, the Prize Corner and the stage tiles frame the same picture. A canvas narrower than the scene
// crops to x 60..940. band: room left at the top for the nameplate.
fun DrawScope.room(s: Data, t: Long, react: Reaction?, orbits: List<Orbit>, fresh: Pair<String, Long>?, equip: Map<String, String?>, band: Float = 0f, guest: ImageBitmap? = null) {
    val tier = (PART[equip["base"]]?.tier ?: 1).coerceIn(1, ROOMS.size)
    val r = ROOMS[tier - 1]
    val tight = size.width / (size.height - band) < 920f / 760f
    val k = min(size.width / (if (tight) 880f else 920f), (size.height - band) / 760f)
    val ox = size.width / 2 - 500f * k; val oy = band + (size.height - band - 760f * k) / 2 - 120f * k
    val fy = oy + 700f * k
    drawRect(Brush.verticalGradient(listOf(r.sky, r.wall), 0f, fy))
    drawRect(Brush.verticalGradient(listOf(r.floor, W.rail), fy, size.height), Offset(0f, fy), Size(size.width, size.height - fy))
    withTransform({ translate(ox, oy); scale(k, k, Offset.Zero) }) { ring(s, t, react, orbits, fresh, equip, tier, r, guest) }
    // a fail: for one frame the whole ring snaps to its negative under a burst of camera static (never with animations off)
    val ms = react?.takeIf { it.kind == "sad" && t != 0L }?.let { since(t, it.at) } ?: Long.MAX_VALUE
    if (ms < 160) {
        if (ms < 70) drawRect(Color.White, size = size, blendMode = BlendMode.Difference)
        var x = (t * 2654435761L).toInt()
        repeat(420) {
            x = x * 1103515245 + 12345
            val px = ((x ushr 8) % 1000) / 1000f * size.width
            x = x * 1103515245 + 12345
            val py = ((x ushr 8) % 1000) / 1000f * size.height
            drawRect(Color.White, Offset(px, py), Size(3f, 2f), alpha = 0.25f + ((x ushr 4) % 50) / 100f)
        }
    }
}

// The ring: one spotlight from the dark on the animatronic standing on its ring box, and beside it a stage riser with
// a marquee sign for every habit running today, a bulb per slot. Everything else stays dark. A sign with every bulb
// lit chases its border lights. Under the riser a calliope plays the Soul attribute: its pipes glow and its ten
// lamps read the level. A habit just done buzzes its newest bulb on, and that bulb keeps its flare while it is fresh.
private fun DrawScope.ring(s: Data, t: Long, react: Reaction?, orbits: List<Orbit>, fresh: Pair<String, Long>?, equip: Map<String, String?>, tier: Int, r: Room, guest: ImageBitmap?) {
    val soul = level(s.stats.getValue(Attr.Core)) / 10f
    val pulse = if (t == 0L) 1f else 0.85f + 0.15f * sin(t / 700.0).toFloat()
    val riser = 596f
    when (tier) { 2 -> partsAndService(t); 3 -> pirateCove(t); 4 -> showStage(t); 5 -> gallery(t); else -> centerRing(s, t) }
    // the dark everywhere the spotlight does not reach
    drawRect(Brush.radialGradient(0f to Color.Transparent, 0.3f to Color.Black.copy(alpha = 0.45f), 0.6f to Color.Black.copy(alpha = 0.78f), 1f to Color.Black.copy(alpha = 0.92f),
        center = Offset(380f, 600f), radius = 640f), Offset(-600f, -300f), Size(2200f, 1300f))
    spotlight(Offset(360f, 120f), 360f, r.light)
    marqueeLight(r.light, t)
    translate(-140f, 0f) {
        SCENE.forEach { paint(it, t) }
        // a buddy joins on the platform's left, and this one steps right to make room
        guest?.let { guest(it, 290f, t) }
        translate(if (guest != null) 330f else 250f, 165f) { scale(1.25f, 1.25f, Offset.Zero) { robot(equip, t, mood(s), react) } }
    }
    riserBack()
    calliope(level(s.stats.getValue(Attr.Core)), soul, pulse, t)
    riserTop()

    if (s.habits.isEmpty()) drawRoundRect(W.dim, Offset(745f, riser - 110f), Size(40f, 110f), CornerRadius(8f), style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
    val shown = orbits.take(6)
    val gap = 290f / max(1, shown.size)
    val tallest = shown.maxOfOrNull { it.slots.size } ?: 1
    val pitch = min(44f, 190f / tallest)
    val br = min(min(13f, gap * 0.3f), pitch * 0.4f)
    shown.forEachIndexed { k, o ->
        val x = 620f + gap * (k + 0.5f)
        val w = br * 2.9f
        val top = riser - 38f - pitch * (o.slots.size - 1) - (br + 23f)
        val box = Rect(x - w / 2, top, x + w / 2, riser)
        val hot = o.slots.all { it.s == Outcome.Done }
        val ignite = if (hot && fresh?.first == o.h.id) ((since(t, fresh.second) - 500f) / 700f).coerceIn(0f, 1f) else if (hot) 1f else 0f
        if (hot) drawRoundRect(Brush.radialGradient(listOf(W.glow.copy(alpha = 0.35f * ignite), Color.Transparent), box.center, (riser - top) * 0.8f),
            box.topLeft - Offset(w, 20f), Size(w * 3f, box.height + 40f), CornerRadius(20f))
        // the sign: a dark board on a pole, its border studded with small bulbs that chase when every slot is lit
        drawRoundRect(Color(0xFF2A1426), box.topLeft, box.size, CornerRadius(7f))
        drawRoundRect(if (hot) W.glow else if (o.slots.any { it.s == Outcome.Fail }) W.joint else W.edge, box.topLeft, box.size, CornerRadius(7f), alpha = if (hot) 0.9f else 1f, style = Stroke(2.5f))
        drawRect(Color(0xFF3A1F33), Offset(x - w / 2 - 6f, riser - 10f), Size(w + 12f, 10f))
        if (hot) {
            val n = 14
            val step = if (t == 0L) 0 else ((t / 120) % 3).toInt()
            for (i in 0 until n) {
                val f = i / n.toFloat()
                val p = perimeter(box, f)
                val lit = (i + step) % 3 == 0
                drawCircle(if (lit) Color.White else W.glow, if (lit) 2.8f else 2f, p, alpha = (if (lit) 1f else 0.5f) * ignite)
            }
        }
        val newest = o.slots.indexOfLast { it.s == Outcome.Done && !it.other }
        o.slots.forEachIndexed { j, sl ->
            var drive = 1f; var flare = 0f
            if (fresh != null && fresh.first == o.h.id && j == newest) {
                drive = min(1f, since(t, fresh.second) / 700f)
                flare = if (t == 0L) 0.3f else 0.4f + 0.3f * sin(t / 260.0).toFloat() // held while it is fresh, so it is seen
            }
            bulb(Offset(x, riser - 38f - pitch * j), br, sl, drive, flare)
        }
    }
}

// A point at share f round a rectangle's border, clockwise from the top left.
private fun perimeter(b: Rect, f: Float): Offset {
    val p = 2 * (b.width + b.height); var d = f * p
    if (d < b.width) return Offset(b.left + d, b.top); d -= b.width
    if (d < b.height) return Offset(b.right, b.top + d); d -= b.height
    if (d < b.width) return Offset(b.right - d, b.bottom); d -= b.width
    return Offset(b.left, b.bottom - d)
}

// The one spotlight: a cone of light from the rig above, pooling on the ring box.
private fun DrawScope.spotlight(from: Offset, x: Float, light: Color) {
    val cone = Path().apply { moveTo(from.x - 20f, from.y - 300f); lineTo(from.x + 20f, from.y - 300f); lineTo(x + 230f, 770f); lineTo(x - 230f, 770f); close() }
    drawPath(cone, Brush.verticalGradient(listOf(light.copy(alpha = 0.08f), light.copy(alpha = 0.26f)), from.y - 300f, 770f))
    drawOval(Brush.radialGradient(listOf(light.copy(alpha = 0.42f), Color.Transparent), Offset(x, 762f), 260f), Offset(x - 260f, 712f), Size(520f, 100f))
}

// A swag of marquee bulbs over the signs, strung on a wire, chasing slowly.
private fun DrawScope.marqueeLight(light: Color, t: Long) {
    val a = Offset(600f, 300f); val b = Offset(940f, 300f)
    drawPath(Path().apply { moveTo(a.x, a.y); quadraticTo(770f, 370f, b.x, b.y) }, W.joint, style = Stroke(3f))
    val step = if (t == 0L) 0 else ((t / 400) % 2).toInt()
    for (i in 0..10) {
        val f = i / 10f
        val p = Offset(a.x + (b.x - a.x) * f, a.y + 2 * (1 - f) * f * 70f + 2f)
        val on = (i + step) % 2 == 0
        if (on) drawCircle(Brush.radialGradient(listOf(light.copy(alpha = 0.5f), Color.Transparent), p, 18f), 18f, p)
        drawCircle(if (on) FILAMENT else W.raised, 5f, p)
    }
}

private val RISER = Color(0xFF3A1F33)

private fun DrawScope.riserBack() {
    for (x in listOf(628f, 888f)) drawRect(RISER, Offset(x, 618f), Size(14f, 150f))
}

// The riser's front: a red and cream striped skirt, its edge trimmed in brass.
private fun DrawScope.riserTop() {
    clipPath(Path().apply { addRect(Rect(604f, 596f, 926f, 622f)) }) {
        var x = 604f; var i = 0
        while (x < 926f) { drawRect(if (i % 2 == 0) Color(0xFF8A1A2E) else Color(0xFFD9C7B0), Offset(x, 596f), Size(23f, 26f)); x += 23f; i++ }
    }
    drawRect(Color(0xFFF2C14E), Offset(604f, 594f), Size(322f, 5f), alpha = 0.8f)
    for (i in 0 until 7) drawCircle(Color(0xFFF2C14E), 3f, Offset(626f + i * 46f, 622f))
}

// The calliope under the riser: a little steam organ whose seven pipes glow with Soul, ten lamps on its base reading
// the level, a crank turning on its side. Everything turns with t and holds still when t is 0.
private fun DrawScope.calliope(lv: Int, soul: Float, pulse: Float, t: Long) {
    val glow = W.glow
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF7A1426), Color(0xFF3A0A14)), 700f, 790f), Offset(694f, 700f), Size(142f, 90f), CornerRadius(10f))
    drawRoundRect(Color(0xFFF2C14E), Offset(694f, 700f), Size(142f, 90f), CornerRadius(10f), alpha = 0.7f, style = Stroke(3f))
    for (i in 0 until 7) {
        val h = 26f + 22f * sin(i * 0.9f + 0.4f).let { it * it }
        val x = 706f + i * 18f
        drawRoundRect(Brush.horizontalGradient(listOf(Color(0xFF8E6A1C), Color(0xFFF4D06A), Color(0xFF8E6A1C)), x, x + 12f), Offset(x, 700f - h), Size(12f, h), CornerRadius(4f))
        drawCircle(glow, 5f, Offset(x + 6f, 700f - h), alpha = (0.15f + 0.6f * soul) * pulse)
    }
    for (i in 0 until 10) {
        val o = Offset(712f + i * 12f, 774f)
        if (i < lv) drawCircle(glow, 5.5f, o, alpha = 0.25f * pulse)
        drawCircle(if (i < lv) FILAMENT else W.line, 2.8f, o)
    }
    val hub = Offset(846f, 740f)
    drawCircle(Color(0xFFF2C14E), 6f, hub)
    val ang = if (t == 0L) 0.0 else (t / 900.0) % (2 * PI)
    drawLine(Color(0xFFF2C14E), hub, hub + Offset((18 * cos(ang)).toFloat(), (18 * sin(ang)).toFloat()), 4f, StrokeCap.Round)
    // a music note drifting up while Soul is high
    if (soul >= 0.5f && t != 0L) {
        val ph = ((t / 2600.0) % 1.0).toFloat()
        val n = Offset(760f + 20f * sin(ph * 6f), 640f - 80f * ph)
        drawCircle(glow, 4.5f, n, alpha = 1f - ph)
        drawLine(glow, n + Offset(4f, 0f), n + Offset(4f, -14f), 2f, alpha = 1f - ph)
    }
}

// A scalloped valance of tent canvas across the top of the ring, red and cream gone grey in the dark.
private fun DrawScope.valance(red: Color = Color(0xFF5A1428), cream: Color = Color(0xFF6E5A54)) {
    var x = -600f; var i = 0
    while (x < 1600f) {
        val p = Path().apply { moveTo(x, -300f); lineTo(x + 60f, -300f); lineTo(x + 60f, 160f); quadraticTo(x + 30f, 196f, x, 160f); close() }
        drawPath(p, if (i % 2 == 0) red else cream)
        x += 60f; i++
    }
    drawRect(Color.Black.copy(alpha = 0.25f), Offset(-600f, 150f), Size(2200f, 6f))
}

// Center Ring: the big top's canvas stripes fanning up into the dark, the valance, and the show sign on the wall whose
// five bulbs read Power (a bulb for every two levels).
private fun DrawScope.centerRing(s: Data, t: Long) {
    val apex = Offset(500f, -260f)
    for (i in -9..9) {
        val p = Path().apply { moveTo(apex.x, apex.y); lineTo(500f + i * 150f, 700f); lineTo(500f + (i + 0.5f) * 150f, 700f); close() }
        drawPath(p, if (i % 2 == 0) Color(0xFF3A0F22) else Color(0xFF241018), alpha = 0.8f)
    }
    valance()
    drawRoundRect(Color(0xFF2A1426), Offset(66f, 440f), Size(104f, 150f), CornerRadius(12f))
    drawRoundRect(Color(0xFFF2C14E), Offset(66f, 440f), Size(104f, 150f), CornerRadius(12f), alpha = 0.7f, style = Stroke(3f))
    val lit = (level(s.stats.getValue(Attr.Battery)) + 1) / 2
    for (i in 0 until 5) {
        val p = Offset(118f, 462f + i * 26f)
        bulb(p, 9f, Slot(if (i >= 5 - lit) Outcome.Done else null, false))
    }
}

// Parts & Service: a back room. A bare bulb swinging on its cord, a shelf of spare heads with dead eyes, and a
// withered suit slumped against the wall whose eyes light now and then.
private fun DrawScope.partsAndService(t: Long) {
    drawRect(Color(0xFF0E1412), Offset(-600f, 380f), Size(1200f, 4f))
    for (x in listOf(-40f, 140f, 320f, 500f)) drawLine(Color.White.copy(alpha = 0.03f), Offset(x, 120f), Offset(x, 692f), 3f)
    val swing = if (t == 0L) 0f else 6f * sin(t / 1300.0).toFloat()
    rotate(swing, Offset(250f, -300f)) {
        drawLine(W.joint, Offset(250f, -300f), Offset(250f, 230f), 2f)
        val flick = if (t == 0L || (t / 90) % 37 != 0L) 1f else 0.2f
        drawCircle(Brush.radialGradient(listOf(Color(0xFFB8F0A0).copy(alpha = 0.3f * flick), Color.Transparent), Offset(250f, 244f), 140f), 140f, Offset(250f, 244f))
        drawCircle(Color(0xFFE6FFD8), 12f, Offset(250f, 244f), alpha = 0.6f + 0.4f * flick)
    }
    // the shelf of heads
    drawRect(Color(0xFF2A2420), Offset(40f, 330f), Size(210f, 10f))
    for ((i, col) in listOf(0xFF4E3A2C, 0xFF3C3784, 0xFFB08A2A).withIndex()) {
        val c = Offset(78f + i * 66f, 300f)
        if (i == 1) for (dx in listOf(-14f, 10f)) drawRoundRect(Color(col), Offset(c.x + dx, 240f), Size(10f, 40f), CornerRadius(5f))
        else for (dx in listOf(-18f, 18f)) drawCircle(Color(col), 9f, c + Offset(dx, -24f))
        drawRoundRect(Color(col), c - Offset(26f, 28f), Size(52f, 56f), CornerRadius(22f))
        for (dx in listOf(-10f, 10f)) drawCircle(Color(0xFF080506), 6f, c + Offset(dx, -4f))
    }
    // the slumped suit
    val sx = 120f; val sy = 600f
    drawRoundRect(Color(0xFF3E2E24), Offset(sx - 50f, sy - 60f), Size(100f, 96f), CornerRadius(40f))
    drawRoundRect(Color(0xFF3E2E24), Offset(sx - 34f, sy - 118f), Size(68f, 60f), CornerRadius(26f))
    for (dx in listOf(-26f, 26f)) drawCircle(Color(0xFF3E2E24), 11f, Offset(sx + dx, sy - 116f))
    drawRoundRect(Color(0xFF3E2E24), Offset(sx - 70f, sy + 20f), Size(56f, 22f), CornerRadius(10f))
    drawRoundRect(Color(0xFF3E2E24), Offset(sx + 14f, sy + 20f), Size(56f, 22f), CornerRadius(10f))
    for (dx in listOf(-13f, 13f)) drawCircle(Color(0xFF080506), 8f, Offset(sx + dx, sy - 92f))
    val wake = if (t == 0L) 0f else ((t % 9000) / 9000f).let { if (it in 0.8f..0.9f) 1f else 0f }
    if (wake > 0f) for (dx in listOf(-13f, 13f)) {
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.6f), Color.Transparent), Offset(sx + dx, sy - 92f), 14f), 14f, Offset(sx + dx, sy - 92f))
        drawCircle(Color.White, 2.4f, Offset(sx + dx, sy - 92f))
    }
}

// Pirate Cove: a violet star-spangled curtain drawn almost shut on a little stage, and in the gap, now and then, a pair
// of yellow eyes.
private fun DrawScope.pirateCove(t: Long) {
    val cur = Color(0xFF3B1E63); val fold = Color(0xFF26113F)
    drawRect(Color(0xFF070409), Offset(150f, 180f), Size(60f, 500f))
    for ((x0, x1) in listOf(-600f to 176f, 184f to 560f)) {
        var x = x0; var i = 0
        while (x < x1) { drawRect(if (i % 2 == 0) cur else fold, Offset(x, 170f), Size(min(26f, x1 - x), 520f)); x += 26f; i++ }
    }
    for ((i, p) in listOf(Offset(60f, 260f), Offset(110f, 420f), Offset(260f, 300f), Offset(330f, 480f), Offset(440f, 250f), Offset(500f, 400f), Offset(20f, 560f)).withIndex())
        star(p, if (i % 2 == 0) 12f else 8f, Color(0xFFF2C14E).copy(alpha = 0.75f))
    drawRect(Color(0xFF2A1A12), Offset(-600f, 680f), Size(1200f, 14f))
    drawRoundRect(Color(0xFF5A3A20), Offset(232f, 560f), Size(120f, 44f), CornerRadius(6f), alpha = 0.9f) // a plank sign, its words long worn off
    drawLine(Color(0xFF2A1A12), Offset(240f, 582f), Offset(344f, 582f), 3f)
    val peek = if (t == 0L) 0f else ((t % 11000) / 11000f).let { if (it in 0.6f..0.75f) 1f else 0f }
    if (peek > 0f) for (dy in listOf(0f)) for (dx in listOf(-9f, 9f)) {
        val e = Offset(180f + dx, 330f + dy)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFD24A).copy(alpha = 0.6f), Color.Transparent), e, 16f), 16f, e)
        drawCircle(Color(0xFFFFE08A), 3.4f, e)
    }
}

// Show Stage: red velvet curtains at the sides, a garland of foil stars, and the black and white checkered floor.
private fun DrawScope.showStage(t: Long) {
    for ((x0, x1) in listOf(-600f to 70f, 500f to 1600f)) {
        var x = x0; var i = 0
        while (x < x1) { drawRect(if (i % 2 == 0) Color(0xFF6A0E1A) else Color(0xFF3E0810), Offset(x, -300f), Size(min(22f, x1 - x), 996f)); x += 22f; i++ }
    }
    drawPath(Path().apply { moveTo(-600f, -300f); lineTo(1600f, -300f); lineTo(1600f, 120f); quadraticTo(500f, 220f, -600f, 120f); close() }, Color(0xFF7A1020))
    val sway = if (t == 0L) 0f else 3f * sin(t / 1600.0).toFloat()
    drawPath(Path().apply { moveTo(80f, 210f); quadraticTo(290f, 290f + sway, 500f, 210f) }, Color(0xFFF2C14E), alpha = 0.6f, style = Stroke(2f))
    for (i in 1..6) {
        val f = i / 7f
        val p = Offset(80f + 420f * f, 210f + 2 * (1 - f) * f * (80f + sway) + 18f)
        star(p, 11f, listOf(Color(0xFFF2C14E), Color(0xFFC0C6D6), Color(0xFFFF6FA3))[i % 3])
    }
    // the checkered floor, receding
    for (row in 0 until 6) {
        val y0 = 694f + row * row * 6f + row * 14f; val y1 = 694f + (row + 1) * (row + 1) * 6f + (row + 1) * 14f
        val cw = 30f + row * 10f
        var x = -600f + (row % 2) * cw; var i = 0
        while (x < 1600f) { if (i % 2 == 0) drawRect(Color(0xFFD9D2C6), Offset(x, y0), Size(cw, y1 - y0), alpha = 0.22f); x += cw; i++ }
    }
}

// Circus Gallery: swags of bulbs across the dark, a striped carousel pole, and a bunch of balloons gone soft.
private fun DrawScope.gallery(t: Long) {
    valance(Color(0xFF6A1440), Color(0xFF5A4A5E))
    val step = if (t == 0L) 0 else ((t / 300) % 3).toInt()
    for ((j, y) in listOf(220f, 300f).withIndex()) {
        drawPath(Path().apply { moveTo(-20f, y); quadraticTo(280f, y + 90f, 580f, y) }, W.joint, style = Stroke(2.5f))
        for (i in 0..12) {
            val f = i / 12f
            val p = Offset(-20f + 600f * f, y + 2 * (1 - f) * f * 90f + 3f)
            val on = (i + j + step) % 3 != 0
            if (on) drawCircle(Brush.radialGradient(listOf(Color(0xFFFF6FA3).copy(alpha = 0.45f), Color.Transparent), p, 16f), 16f, p)
            drawCircle(if (on) Color(0xFFFFD6E6) else W.raised, 4.5f, p)
        }
    }
    clipPath(Path().apply { addRect(Rect(96f, 120f, 124f, 692f)) }) {
        var y = 100f; var i = 0
        while (y < 700f) { drawPath(Path().apply { moveTo(96f, y); lineTo(124f, y - 20f); lineTo(124f, y + 10f); lineTo(96f, y + 30f); close() }, if (i % 2 == 0) Color(0xFFF2C14E) else Color(0xFF6A1440)); y += 30f; i++ }
    }
    val bob = if (t == 0L) 0f else 4f * sin(t / 1100.0).toFloat()
    for ((i, col) in listOf(0xFFC8263A, 0xFF3A7BF0, 0xFFFF6FA3).withIndex()) {
        val c = Offset(440f + i * 34f, 430f + (i % 2) * 24f + bob)
        drawLine(Color.White.copy(alpha = 0.4f), c + Offset(0f, 34f), Offset(470f, 640f), 1.5f)
        drawOval(Color(col), c - Offset(24f, 30f), Size(48f, 62f), alpha = 0.85f)
        drawCircle(Color.White, 5f, c + Offset(-9f, -14f), alpha = 0.3f)
    }
}

private fun DrawScope.star(c: Offset, r: Float, col: Color) {
    drawPath(Path().apply {
        for (i in 0 until 10) {
            val a = -PI / 2 + i * PI / 5; val rr = if (i % 2 == 0) r else r * 0.42f
            val x = (c.x + rr * cos(a)).toFloat(); val y = (c.y + rr * sin(a)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }, col)
}

// A habit card's progress (Tracker in Kit.kt): a string of marquee bulbs on a wire, one per slot, in the bulb() states;
// once every bulb is lit the wire glows too.
fun DrawScope.tracker(slots: List<Slot>) {
    if (slots.isEmpty()) return
    val r = min(size.height * 0.9f, size.width / (slots.size * 2.8f))
    val pitch = min(r * 3.2f, (size.width - 2 * r) / max(1, slots.size - 1))
    val y = size.height / 2
    val end = r + pitch * (slots.size - 1)
    val lit = slots.all { it.s == Outcome.Done }
    if (lit) drawLine(W.glow, Offset(0f, y), Offset(end + r, y), r * 0.9f, StrokeCap.Round, alpha = 0.18f)
    drawLine(if (lit) W.glow else W.edge, Offset(0f, y), Offset(end + r, y), 1.5.dp.toPx(), alpha = if (lit) 0.8f else 1f)
    slots.forEachIndexed { i, sl -> bulb(Offset(r + pitch * i, y), r, sl) }
}

// A card's accent (Card in Kit.kt): its whole edge lit in the colour, as a sign is when every bulb is on.
fun DrawScope.cardAccent(c: Color) {
    val k = 12.dp.toPx(); val w = 1.5.dp.toPx()
    drawRoundRect(c, Offset(w / 2, w / 2), Size(size.width - w, size.height - w), CornerRadius(k), alpha = 0.25f, style = Stroke(w * 4))
    drawRoundRect(c, Offset(w / 2, w / 2), Size(size.width - w, size.height - w), CornerRadius(k), alpha = 0.9f, style = Stroke(w))
}
