package com.luisvilaca.nutsandbolts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Halloween: a limited season, all of October and the first two days of November. While it runs the Shop sells the
// limited parts (each flavour's Parts.kt marks its own, one per category) and every room is dressed: tombstones by the
// wall, bats under the ceiling, spiders on their silk, cobwebs in the corners, jack-o'-lanterns and a cauldron of
// bubbling green on the floor. A limited part bought in the season stays owned after it, and the haunted base keeps
// its dressing all year.

fun halloween(d: LocalDate) = d.monthValue == 10 || (d.monthValue == 11 && d.dayOfMonth <= 2)
val spooky get() = halloween(LocalDate.now())

// Whether the Shop shows a part: a limited one only in its season, or once it is owned.
fun forSale(p: Part, s: Data, d: LocalDate) = !p.limited || halloween(d) || p.id in s.owned

const val HAUNT_TIER = 6 // the haunted base's room
fun dressed(tier: Int) = tier == HAUNT_TIER || spooky

val HALLOWEEN = Color(0xFFFF9A3C) // the season's orange, for its labels in the Shop

// ---------- the parts' finish and shapes, in robot space (400x520) ----------

private fun f(c: Long, a: Float = 1f) = Ink(fill = SolidColor(Color(c)), alpha = a)
private fun s(c: Long, w: Float, a: Float = 1f) = Ink(stroke = Color(c), width = w, alpha = a)

// The roles the limited parts paint with; a part passes this as its pal, so it overrides the flavour's own.
val SPOOKY: Map<String, Ink> = mapOf(
    "pk" to Ink(fill = Brush.linearGradient(listOf(Color(0xFFFFB24A), Color(0xFFC4501A)), Offset(140f, 80f), Offset(260f, 220f))),
    "pkln" to s(0xFF8A3510, 4f, 0.75f), "stem" to s(0xFF4E6B2A, 10f), "zn" to f(0xFF3A3346),
    "lamp" to f(0xFFFFD34D), "lampglow" to Ink(fill = Brush.radialGradient(listOf(Color(0x66FFD34D), Color.Transparent), Offset(200f, 166f), 64f)),
    "bone" to f(0xFFF1E8D4), "bonel" to s(0xFFF1E8D4, 11f), "bonef" to s(0xFFF1E8D4, 5f),
    "pot" to Ink(fill = Brush.verticalGradient(listOf(Color(0xFF454B58), Color(0xFF15171D)), 250f, 372f)), "potrim" to f(0xFF5A6070),
    "slime" to f(0xFF7CFF4F), "slimed" to f(0xFF3FBF2A), "bubs" to s(0xFFC8FF9A, 2.5f, 0.9f),
    "hat" to Ink(fill = Brush.linearGradient(listOf(Color(0xFF5A3384), Color(0xFF1E0F33)), Offset(150f, 10f), Offset(250f, 100f))),
    "hatband" to f(0xFFFF8A1F), "buckle" to s(0xFFF2C14E, 4f),
    "sheet" to Ink(fill = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFC9C6E0)), 356f, 468f)), "sheetsh" to s(0xFF9D99BF, 3f, 0.8f),
    "wrap" to Ink(fill = Brush.linearGradient(listOf(Color(0xFFF0E6CE), Color(0xFFB9AA88)), Offset(130f, 230f), Offset(270f, 380f))),
    "wrapln" to s(0xFF8E7F62, 3f, 0.8f), "wrapend" to s(0xFFE8DCC2, 9f),
)

private val MIRROR = floatArrayOf(-1f, 0f, 0f, 1f, 400f, 0f)
private fun o(x: Float, y: Float, rx: Float, ry: Float = rx) = "M${x - rx} ${y}a$rx $ry 0 1 0 ${2 * rx} 0a$rx $ry 0 1 0 ${-2 * rx} 0z"
private fun sh(d: String, vararg r: String, layer: String = "", pulse: Boolean = false) = Shape(r.toList(), d, layer = layer, pulse = pulse)
private fun arm(d: String, vararg r: String) = listOf(Shape(r.toList(), d, layer = "armL"), Shape(r.toList(), d, MIRROR, "armR"))

// A jack-o'-lantern for a head: three lobes, a curled stem, a candle behind the carved eyes and grin.
val JACK_HEAD = listOf(
    sh(o(152f, 152f, 50f, 60f), "pk"), sh(o(248f, 152f, 50f, 60f), "pk"),
    sh("M152 104q-26 48 0 96M248 104q26 48 0 96", "pkln"),
    sh(o(200f, 150f, 60f, 68f), "pk"),
    sh("M178 88q-20 62 0 124M222 88q20 62 0 124", "pkln"),
    sh("M200 86q-2-18 14-28", "stem"),
    sh("M146 116q12-16 30-20", "hl"),
    sh(o(200f, 166f, 64f), "lampglow", pulse = true),
    sh("M156 152l20-30 20 30z", "lamp", layer = "eyes"), sh("M204 152l20-30 20 30z", "lamp", layer = "eyes"),
    sh("M192 170l8-11 8 11z", "lamp"),
    sh("M146 180l16 6 9-9 11 10 9-8 9 8 9-8 9 8 11-10 9 9 16-6q-12 32-54 32t-54-32z", "lamp"),
)

// A witch's cauldron for a body, brimming green, a porthole on the brew where a reactor would glow.
val CAULDRON_BODY = listOf(
    sh("M146 356h18l-4 20h-10zM236 356h18l-4 20h-10z", "zn"),
    sh("M128 262h144q10 0 8 12l-8 56q-6 40-72 40t-72-40l-8-56q-2-12 8-12z", "pot"),
    sh("M150 282q-4 34 14 60", "hl"),
    sh(o(200f, 314f, 27f), "zn"), sh(o(200f, 314f, 20f), "slime", pulse = true), sh(o(193f, 307f, 5f), "bubs"),
    sh("M122 250h156a9 9 0 0 1 0 18h-156a9 9 0 0 1 0 -18z", "potrim"),
    sh("M130 252q8-16 28-8q14-14 34-4q16-12 36-2q18-10 32 4q12-2 12 10z", "slime"),
    sh("M152 262v16a5 5 0 0 0 10 0v-14zM234 262v24a5 5 0 0 0 10 0v-20z", "slimed"),
    sh(o(168f, 236f, 6f), "bubs", pulse = true), sh(o(214f, 228f, 4f), "bubs", pulse = true), sh(o(240f, 240f, 5f), "bubs", pulse = true),
)

// A mummy's wrappings round the body, one end come loose.
val MUMMY_BODY = listOf(
    sh("M184 228h32a60 60 0 0 1 60 60v28a60 60 0 0 1 -60 60h-32a60 60 0 0 1 -60 -60v-28a60 60 0 0 1 60 -60z", "wrap"),
    sh("M134 264l130-10M126 292l148 8M126 318l148-12M140 346l120 8", "wrapln"),
    sh(o(176f, 330f, 10f, 5f), "zn"),
    sh("M146 250q20-14 46-14", "hl"),
    sh("M258 300q26 8 22 40", "wrapend"),
)

// Bones for arms: the shoulder joint, two long bones knuckled at the elbow, and a hand of finger bones.
val BONE_ARMS = arm(o(120f, 264f, 13f), "zn") + arm("M115 276l-9 40", "bonel") + arm(o(111f, 276f, 6f) + o(121f, 278f, 6f), "bone") +
    arm("M106 320l-6 30", "bonel") + arm(o(101f, 317f, 6f) + o(111f, 320f, 6f), "bone") + arm(o(100f, 352f, 7f), "bone") +
    arm("M100 354l-10 13M100 355l-2 16M101 354l7 13", "bonef")

// No legs at all: the trailing hem of a ghost's sheet, floating out of step with the body.
val GHOST_TAIL = listOf(
    sh("M140 352h120q12 0 12 16l-2 72q-3 22-14 6q-14 24-28 0q-14 24-28 0q-14 24-28 0q-14 24-28 0q-11 16-14-6l-2-72q0-16 12-16z", "sheet", layer = "hover"),
    sh("M170 378q-6 32 2 62M230 378q6 32-2 62M200 386v54", "sheetsh", layer = "hover"),
)

// A witch's hat, its tip bent over, an orange band and a brass buckle.
val WITCH_HAT = listOf(
    sh(o(200f, 94f, 78f, 13f), "hat"),
    sh("M152 92c18-30 30-54 44-70c10-10 26-16 44-14c-12 6-18 16-18 28c4 20 14 38 26 56z", "hat"),
    sh("M162 76h78l5 14h-88z", "hatband"),
    sh("M191 74h20v18h-20z", "buckle"),
    sh("M170 70q10-24 24-38", "hl"),
)

// ---------- the dressing, in the room's 1000 x 900 space ----------

private val STONE = Color(0xFF6A6E7A)
private val STONE_DARK = Color(0xFF2E3038)
private val MOSS = Color(0xFF4E7A2E)
private val SLIME = Color(0xFF7CFF4F)
private val CANDLE = Color(0xFFFFD34D)
private val BAT = Color(0xFF1A1024)
private val IRON = Color(0xFF0E0A14)
private fun wob(t: Long, period: Double, ph: Double = 0.0) = if (t == 0L) sin(ph).toFloat() else sin(t / period + ph).toFloat()

// The haunted base's own room, drawn where the other bases hang their wall: a full moon behind a dead tree, a starry
// sky, an iron fence along the back and fog drifting at its foot.
fun DrawScope.crypt(t: Long) {
    for (i in 0 until 34) drawCircle(Color.White, if (i % 5 == 0) 2.2f else 1.3f, Offset((i * 137 % 1300 - 150).toFloat(), 130f + i * 71 % 330), alpha = 0.55f)
    val m = Offset(175f, 255f)
    drawCircle(Brush.radialGradient(listOf(Color(0x55D8FFB0), Color.Transparent), m, 210f), 210f, m)
    drawCircle(Brush.radialGradient(listOf(Color(0xFFF4FFE0), Color(0xFFBFDDA0)), m - Offset(24f, 24f), 100f), 74f, m)
    for ((d, r) in listOf(Offset(-22f, -14f) to 13f, Offset(24f, 10f) to 9f, Offset(-6f, 30f) to 7f, Offset(30f, -28f) to 5f)) drawCircle(Color(0x2A3E5A2A), r, m + d)
    // the tree, branch by branch: (from, to, width)
    for ((a, b, w) in listOf(
        Triple(Offset(70f, 700f), Offset(82f, 520f), 22f), Triple(Offset(82f, 520f), Offset(72f, 420f), 15f), Triple(Offset(80f, 540f), Offset(150f, 470f), 10f),
        Triple(Offset(150f, 470f), Offset(214f, 446f), 6f), Triple(Offset(150f, 470f), Offset(172f, 408f), 5f), Triple(Offset(74f, 452f), Offset(40f, 380f), 8f),
        Triple(Offset(72f, 420f), Offset(112f, 330f), 9f), Triple(Offset(112f, 330f), Offset(164f, 302f), 5f), Triple(Offset(112f, 330f), Offset(102f, 272f), 4f),
        Triple(Offset(102f, 272f), Offset(132f, 230f), 3f), Triple(Offset(164f, 302f), Offset(204f, 310f), 3f),
    )) drawLine(IRON, a, b, w, StrokeCap.Round)
    // the fence: bars with spear tips, two rails
    var x = -600f
    while (x < 1600f) {
        drawRect(IRON, Offset(x - 3f, 606f), Size(6f, 94f))
        drawPath(Path().apply { moveTo(x - 7f, 610f); lineTo(x, 592f); lineTo(x + 7f, 610f); close() }, IRON)
        x += 34f
    }
    for (y in listOf(620f, 672f)) drawRect(IRON, Offset(-600f, y), Size(2200f, 6f))
    fog(t, 690f)
}

// Fog along a line: soft green-white banks drifting slowly to the right.
private fun DrawScope.fog(t: Long, y: Float) {
    val drift = if (t == 0L) 0f else ((t / 60.0) % 260.0).toFloat()
    for (i in -3 until 6) {
        val c = Offset(i * 260f + drift, y + 14f * sin(i * 1.3f))
        drawOval(Brush.radialGradient(listOf(Color(0x24D8FFE0), Color.Transparent), c, 180f), c - Offset(180f, 40f), Size(360f, 80f))
    }
}

// Behind the robot: tombstones along the wall and bats wheeling under the ceiling.
fun DrawScope.hauntBack(t: Long) {
    tomb(Offset(82f, 704f), 60f, 92f, cross = false)
    tomb(Offset(166f, 708f), 0f, 74f, cross = true)
    tomb(Offset(580f, 706f), 44f, 62f, cross = false)
    for ((i, a) in listOf(Offset(250f, 150f), Offset(470f, 136f), Offset(812f, 150f)).withIndex()) {
        val p = a + Offset(26f * wob(t, 2100.0, i * 2.0), 12f * wob(t, 1300.0, i * 1.0))
        bat(p, listOf(1.5f, 1.2f, 1.3f)[i], wob(t, 110.0, i * 1.7))
    }
}

// In front of everything: jack-o'-lanterns and a cauldron on the floor, two potion flasks, spiders on their silk.
fun DrawScope.hauntFront(t: Long) {
    lantern(Offset(92f, 814f), 34f, t, 0)
    lantern(Offset(162f, 858f), 20f, t, 1)
    lantern(Offset(912f, 846f), 28f, t, 2)
    cauldron(Offset(646f, 840f), t)
    flask(Offset(744f, 846f), 15f, SLIME)
    flask(Offset(782f, 852f), 11f, Color(0xFFB48CFF))
    spider(Offset(588f, 300f + 18f * wob(t, 1700.0)), t, 0)
    spider(Offset(196f, 392f + 14f * wob(t, 2300.0, 2.0)), t, 1)
}

private fun DrawScope.tomb(base: Offset, w: Float, h: Float, cross: Boolean) {
    val shade = Brush.verticalGradient(listOf(STONE, STONE_DARK), base.y - h, base.y)
    if (cross) {
        drawRect(shade, Offset(base.x - 8f, base.y - h), Size(16f, h))
        drawRect(shade, Offset(base.x - 26f, base.y - h * 0.74f), Size(52f, 14f))
        drawLine(Color.White.copy(alpha = 0.18f), Offset(base.x - 7f, base.y - h + 2f), Offset(base.x - 7f, base.y - 4f), 2f)
    } else {
        val slab = Path().apply {
            moveTo(base.x - w / 2, base.y); lineTo(base.x - w / 2, base.y - h + w / 2)
            arcTo(Rect(base.x - w / 2, base.y - h, base.x + w / 2, base.y - h + w), 180f, 180f, false)
            lineTo(base.x + w / 2, base.y); close()
        }
        drawPath(slab, shade)
        drawPath(slab, Color.White.copy(alpha = 0.14f), style = Stroke(2f))
        // the engraving: a small cross over two lines of worn letters, and a crack down one side
        val c = Offset(base.x, base.y - h + w * 0.5f)
        drawLine(STONE_DARK, c - Offset(0f, 9f), c + Offset(0f, 9f), 3f); drawLine(STONE_DARK, c - Offset(6f, 3f), c + Offset(6f, -3f), 3f)
        for ((dy, k) in listOf(22f to 0.5f, 32f to 0.34f)) drawLine(STONE_DARK, Offset(base.x - w * k / 2, c.y + dy), Offset(base.x + w * k / 2, c.y + dy), 3f, StrokeCap.Round)
        drawPath(Path().apply { moveTo(base.x + w * 0.3f, base.y - h * 0.62f); lineTo(base.x + w * 0.16f, base.y - h * 0.48f); lineTo(base.x + w * 0.26f, base.y - h * 0.36f); lineTo(base.x + w * 0.12f, base.y - h * 0.22f) },
            STONE_DARK, style = Stroke(2f))
    }
    drawOval(MOSS, Offset(base.x - (w.coerceAtLeast(30f)) * 0.6f, base.y - 7f), Size(w.coerceAtLeast(30f) * 1.2f, 12f), alpha = 0.8f)
    for (i in -2..2) drawLine(MOSS, Offset(base.x + i * 9f, base.y), Offset(base.x + i * 11f + 3f, base.y - 12f - (i * i) * 1.5f), 2.5f, StrokeCap.Round)
}

// A bat in flight, its wings beating with flap (-1..1); scale k on a body about 60 wide.
private fun DrawScope.bat(c: Offset, k: Float, flap: Float) = withTransform({ translate(c.x, c.y); scale(k, k, Offset.Zero) }) {
    val up = -14f * flap
    val wing = Path().apply {
        moveTo(0f, -2f); quadraticBezierTo(14f, -6f + up / 2, 30f, up)
        quadraticBezierTo(26f, 4f, 22f, 8f); quadraticBezierTo(18f, 2f, 14f, 6f); quadraticBezierTo(10f, 2f, 6f, 6f); quadraticBezierTo(3f, 2f, 0f, 4f); close()
    }
    for (side in listOf(1f, -1f)) scale(side, 1f, Offset.Zero) {
        drawPath(wing, BAT)
        drawPath(wing, Color(0x66C9A8FF), style = Stroke(1.2f)) // a rim of moonlight, so it reads on a dark wall
    }
    drawOval(BAT, Offset(-5f, -6f), Size(10f, 14f))
    drawPath(Path().apply { moveTo(-4f, -4f); lineTo(-3f, -11f); lineTo(0f, -6f); lineTo(3f, -11f); lineTo(4f, -4f); close() }, BAT)
    for (dx in listOf(-1.8f, 1.8f)) drawCircle(CANDLE, 1.2f, Offset(dx, -2f))
}

// A jack-o'-lantern on the floor, its candle flickering behind the carving.
private fun DrawScope.lantern(c: Offset, r: Float, t: Long, i: Int) {
    val flick = if (t == 0L) 1f else 0.78f + 0.12f * sin(t / 70.0 + i * 3).toFloat() + 0.1f * sin(t / 23.0 + i).toFloat()
    drawCircle(Brush.radialGradient(listOf(CANDLE.copy(alpha = 0.22f * flick), Color.Transparent), c, r * 3f), r * 3f, c)
    drawOval(Color.Black.copy(alpha = 0.3f), Offset(c.x - r * 1.2f, c.y + r * 0.7f), Size(r * 2.4f, r * 0.5f))
    val skin = Brush.linearGradient(listOf(Color(0xFFFFB24A), Color(0xFFC4501A)), c - Offset(r, r), c + Offset(r, r))
    for (dx in listOf(-0.42f, 0.42f)) drawOval(skin, Offset(c.x + dx * r - r * 0.62f, c.y - r * 0.82f), Size(r * 1.24f, r * 1.64f))
    drawOval(skin, Offset(c.x - r * 0.68f, c.y - r * 0.9f), Size(r * 1.36f, r * 1.8f))
    for (dx in listOf(-0.4f, 0.4f)) drawArc(Color(0xFF8A3510), if (dx < 0) 110f else -70f, 140f, false, Offset(c.x + dx * r - r * 0.3f, c.y - r * 0.82f), Size(r * 0.6f, r * 1.64f), alpha = 0.6f, style = Stroke(r * 0.07f))
    drawLine(Color(0xFF4E6B2A), c - Offset(0f, r * 0.8f), c + Offset(r * 0.2f, -r * 1.12f), r * 0.2f, StrokeCap.Round)
    val lit = CANDLE.copy(alpha = flick)
    for (dx in listOf(-0.34f, 0.34f)) drawPath(Path().apply {
        moveTo(c.x + dx * r - r * 0.18f, c.y - r * 0.12f); lineTo(c.x + dx * r, c.y - r * 0.44f); lineTo(c.x + dx * r + r * 0.18f, c.y - r * 0.12f); close()
    }, lit)
    drawPath(Path().apply {
        moveTo(c.x - r * 0.5f, c.y + r * 0.12f)
        for (j in 1..6) lineTo(c.x - r * 0.5f + j * r / 6f, c.y + r * (if (j % 2 == 0) 0.12f else 0.28f))
        quadraticBezierTo(c.x, c.y + r * 0.78f, c.x - r * 0.5f, c.y + r * 0.12f); close()
    }, lit)
}

// A cauldron on three legs, brimming with green, bubbles rising and popping off it.
private fun DrawScope.cauldron(c: Offset, t: Long) {
    val glow = 0.85f + 0.15f * wob(t, 600.0)
    drawCircle(Brush.radialGradient(listOf(SLIME.copy(alpha = 0.22f * glow), Color.Transparent), c - Offset(0f, 30f), 130f), 130f, c - Offset(0f, 30f))
    for (dx in listOf(-30f, 0f, 30f)) drawLine(Color(0xFF15171D), Offset(c.x + dx, c.y + 10f), Offset(c.x + dx * 1.2f, c.y + 34f), 6f, StrokeCap.Round)
    drawOval(Brush.verticalGradient(listOf(Color(0xFF454B58), Color(0xFF15171D)), c.y - 34f, c.y + 30f), Offset(c.x - 46f, c.y - 40f), Size(92f, 72f))
    drawOval(Color.White.copy(alpha = 0.12f), Offset(c.x - 36f, c.y - 24f), Size(14f, 38f))
    drawOval(Color(0xFF5A6070), Offset(c.x - 44f, c.y - 48f), Size(88f, 20f))
    drawOval(SLIME, Offset(c.x - 36f, c.y - 45f), Size(72f, 13f), alpha = glow)
    drawRoundRect(SLIME, Offset(c.x + 18f, c.y - 40f), Size(9f, 22f), androidx.compose.ui.geometry.CornerRadius(4.5f), alpha = 0.9f) // a drip over the rim
    for (i in 0 until 5) {
        val ph = if (t == 0L) i / 5f else ((t / 1700.0 + i / 5.0) % 1.0).toFloat()
        val p = Offset(c.x - 22f + i * 11f + 6f * sin(ph * 6f + i), c.y - 44f - 56f * ph)
        drawCircle(Color(0xFFC8FF9A), 3f + 3f * ph, p, alpha = (1f - ph) * 0.9f, style = Stroke(2f))
    }
}

// A round-bottomed potion flask, half full, corked.
private fun DrawScope.flask(c: Offset, r: Float, ink: Color) {
    drawCircle(ink.copy(alpha = 0.18f), r * 2.2f, c)
    drawArc(ink, 0f, 180f, true, c - Offset(r, r), Size(r * 2, r * 2), alpha = 0.9f)
    drawCircle(Color.White, r, c, alpha = 0.12f)
    drawRect(Color.White.copy(alpha = 0.16f), Offset(c.x - r * 0.3f, c.y - r * 2f), Size(r * 0.6f, r * 1.1f))
    drawCircle(Color.White, r, c, alpha = 0.5f, style = Stroke(2f))
    drawRect(Color(0xFF9A6440), Offset(c.x - r * 0.38f, c.y - r * 2.4f), Size(r * 0.76f, r * 0.5f))
    drawLine(Color.White.copy(alpha = 0.45f), c + Offset(-r * 0.55f, -r * 0.35f), c + Offset(-r * 0.3f, -r * 0.62f), 2f, StrokeCap.Round)
}

// A spider hanging on its silk from the ceiling, legs paddling slowly.
private fun DrawScope.spider(c: Offset, t: Long, i: Int) {
    drawLine(Color.White.copy(alpha = 0.35f), Offset(c.x, -300f), c, 1.5f)
    val kick = 4f * wob(t, 260.0, i * 1.3)
    for (side in listOf(-1f, 1f)) for (j in 0 until 4) {
        val a = (-50 + j * 34) * PI / 180
        val knee = c + Offset(side * 16f * cos(a).toFloat(), -10f + 14f * sin(a).toFloat() - (if (j % 2 == 0) kick else -kick))
        drawLine(BAT, c, knee, 2.5f, StrokeCap.Round)
        drawLine(BAT, knee, knee + Offset(side * 8f, 10f), 2.5f, StrokeCap.Round)
    }
    drawCircle(BAT, 10f, c + Offset(0f, 4f))
    drawCircle(BAT, 6f, c - Offset(0f, 8f))
    drawCircle(Color.White.copy(alpha = 0.25f), 10f, c + Offset(0f, 4f), style = Stroke(1.2f))
    for (dx in listOf(-2.2f, 2.2f)) drawCircle(Color(0xFFFF4D4D), 1.6f, c + Offset(dx, -9f))
}

// A cobweb across the canvas's top right corner (in canvas pixels, below the band), scaled with the room; only the
// right, since the nameplate sits at the top left.
fun DrawScope.webs(band: Float, k: Float) {
    val r = 170f * k
    scale(-1f, 1f, Offset(size.width / 2, 0f)) {
        val o = Offset(0f, band)
        val spokes = (0..5).map { j -> (j * 18f) * PI.toFloat() / 180f }
        val silk = Color.White.copy(alpha = 0.32f)
        for (a in spokes) drawLine(silk, o, o + Offset(r * cos(a), r * sin(a)), 1.4f * k + 0.6f)
        for (ring in listOf(0.24f, 0.44f, 0.66f, 0.9f)) {
            val p = Path()
            spokes.forEachIndexed { j, a ->
                val q = o + Offset(r * ring * cos(a), r * ring * sin(a))
                if (j == 0) p.moveTo(q.x, q.y) else {
                    val m = (spokes[j - 1] + a) / 2
                    p.quadraticBezierTo(o.x + r * ring * 0.84f * cos(m), o.y + r * ring * 0.84f * sin(m), q.x, q.y) // each strand sags toward the corner
                }
            }
            drawPath(p, silk, style = Stroke(1.2f * k + 0.5f))
        }
    }
}
