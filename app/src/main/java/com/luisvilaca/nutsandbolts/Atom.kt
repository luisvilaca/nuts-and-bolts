package com.luisvilaca.nutsandbolts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

// One slot of a habit's period: its state ("" open, done, skip, fail) and whether it was logged on another day of the week.
class Slot(val s: String, val other: Boolean)

fun slots(s: Data, h: Habit, d: LocalDate): List<Slot> {
    val ms = periodMarks(s, h, d)
    return List(h.target) { i -> ms.getOrNull(i)?.let { Slot(it.second.s, it.first != d) } ?: Slot("", false) }
}

// Bloom shapes in a 22 x 26 box, each anchored where it meets the stem at (11, 24). The flower head sits at (6, 11).
private fun shape(d: String) = PathParser().parsePathString(d).toPath()
val LEAF = shape("M11 24C11 14 4 10 3 3C10 3 16 9 11 24Z") // the diary page's emblem and its gauge buds
val BUD = shape("M11 24C11 18 8 15 8 11C8 8 10 6 11 5C12 6 14 8 14 11C14 15 11 18 11 24Z")
private val STALK = shape("M11 24Q10 15 6 11")
private val CLOSED = shape("M0 4C-3.5 2 -3.5 -3 0 -6C3.5 -3 3.5 2 0 4Z") // closed petal plates, centred on the head
private val WILT = shape("M11 24C11 18 12 14 18 12C16 17 13 18 11 24Z")
// one cherry petal pointing up from the head, notched at the tip; five make the flower
private val PETAL = shape("M0 0C-3.5 -1.5 -4.2 -6 -2.2 -7.6L0 -6.6L2.2 -7.6C4.2 -6 3.5 -1.5 0 0Z")
// one spider lily petal, thin and curled back at the tip; six make the flower, with long wire stamens between them
private val CURL = shape("M0 -1C1.6 -3.5 1.4 -6.5 -0.4 -7.6Q-1.6 -8 -1.6 -6.8")
private val STAMEN = shape("M0 0Q2.5 -6 0.5 -10.5")
private val HEX = shape("M1.8 0L0.9 1.56L-0.9 1.56L-1.8 0L-0.9 -1.56L0.9 -1.56Z")

// Which flower a habit grows: cherry blossoms and spider lilies take turns down the habit list.
fun lily(s: Data, h: Habit) = s.habits.indexOf(h) % 2 == 1

// A slot is a machined flower on a steel stalk: done has opened (pink cherry or red spider lily, petals riveted round
// a hex nut), skipped is the same flower in dashed outline, failed has wilted (coral, drooping), open is a closed bud.
// Each state has its own form; colour only confirms it.
fun DrawScope.bloom(at: Offset, k: Float, slot: Slot, flip: Boolean = false, lily: Boolean = false) {
    val a = if (slot.other) 0.45f else 1f
    val dash = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.5f, 2f)))
    withTransform({ translate(at.x, at.y); scale(if (flip) -k else k, k, Offset.Zero); translate(-11f, -24f) }) {
        if (slot.s == "fail") drawPath(WILT, W.coral, alpha = a)
        else {
            drawPath(STALK, W.steel, alpha = a, style = Stroke(1.3f, cap = StrokeCap.Round))
            translate(6f, 11f) {
                val open = slot.s == "done"
                when {
                    slot.s == "" -> { drawPath(CLOSED, W.dim, alpha = a, style = dash); drawLine(W.dim, Offset(0f, 3f), Offset(0f, -4.5f), 0.8f, alpha = a) }
                    lily -> {
                        val ink = if (open) W.lily else W.dim
                        for (i in 0 until 6) rotate(i * 60f + 30f, Offset.Zero) {
                            drawPath(STAMEN, W.steel, alpha = a, style = Stroke(0.5f))
                            drawCircle(W.gold, 0.6f, Offset(0.5f, -10.5f), alpha = a)
                        }
                        for (i in 0 until 6) rotate(i * 60f, Offset.Zero) { drawPath(CURL, ink, alpha = a, style = if (open) Stroke(1.5f, cap = StrokeCap.Round) else dash) }
                    }
                    else -> for (i in 0 until 5) rotate(i * 72f, Offset.Zero) {
                        if (open) { drawPath(PETAL, W.blossom, alpha = a); drawPath(PETAL, W.lily, alpha = a * 0.5f, style = Stroke(0.5f)); drawCircle(W.steel, 0.55f, Offset(0f, -4.2f), alpha = a) }
                        else drawPath(PETAL, W.dim, alpha = a, style = dash)
                    }
                }
                if (slot.s != "") { drawPath(HEX, if (open) W.steel else W.dim, alpha = a); drawCircle(W.ground, 0.7f, Offset.Zero, alpha = a) }
            }
        }
    }
}

@Composable
fun SlotMark(slot: Slot, size: Dp = 26.dp, modifier: Modifier = Modifier, lily: Boolean = false) =
    Canvas(modifier.size(size)) { bloom(Offset(center.x + 4f * this.size.height / 23f, this.size.height * 0.96f), this.size.height / 23f, slot, lily = lily) }

// A day as a small machined stem: it stands taller and opens more flowers the more was done; a wilted petal when
// something failed; a dashed seed while the day is ahead.
@Composable
fun DayMark(p: Progress, future: Boolean, modifier: Modifier = Modifier, today: Boolean = false) = Canvas(modifier) {
    if (today) drawCircle(W.steel, size.minDimension * 0.47f, center, style = Stroke(1.5.dp.toPx()))
    val base = Offset(size.width / 2, size.height * 0.92f)
    val k = size.height / 44f
    if (future || p.slots == 0) {
        drawCircle(W.edge, size.width * 0.07f, base - Offset(0f, size.width * 0.08f),
            style = Stroke(size.width * 0.04f, pathEffect = if (future) PathEffect.dashPathEffect(floatArrayOf(4f, 4f)) else null))
        return@Canvas
    }
    val share = min(1f, p.done.toFloat() / p.slots)
    val top = base.y - size.height * (0.18f + 0.5f * share)
    drawLine(W.steel, base, Offset(base.x, top), size.width * 0.05f, StrokeCap.Round)
    val n = when { share >= 1f -> 3; share >= 0.5f -> 2; p.done > 0 -> 1; else -> 0 }
    for (i in 0 until n) bloom(Offset(base.x, top + size.height * (0.14f + 0.16f * i)), k, Slot("done", false), i % 2 == 1, lily = i == 1)
    if (p.fail > 0) bloom(Offset(base.x, base.y - size.height * 0.08f), k, Slot("fail", false), true)
}

class Orbit(val h: Habit, val slots: List<Slot>)

// The bay: Rivet on its platform and, beside it on the floor under a hanging magenta grow light, a glass tray with
// one machined plant per habit running today: a steel stem with a hex nut at each joint and a flower per slot.
// A habit just done opens its newest flower.
// waiting: no habits yet, so the tray holds one dashed seed.
fun DrawScope.bay(s: Data, t: Long, react: Reaction?, orbits: List<Orbit>, fresh: Pair<String, Long>?, equip: Map<String, String?> = s.equip, waiting: Boolean = false) {
    val tier = PART[equip["base"]]?.tier ?: 1
    val ground = 680f
    val glow = Offset(765f, ground - 300f)
    drawCircle(Brush.radialGradient(listOf(W.grow.copy(alpha = 0.22f), Color.Transparent), glow, 460f), 460f, glow)
    // Rivet and its base step left to share the floor with the tray
    translate(-140f, 0f) {
        // the charger would sit off the frame and its cable cut by it, and the base's own pot and jar would land in the
        // tray, so this look draws the base without them
        SCENE.forEach { sh -> if (sh.tier <= tier && "cable" !in sh.roles && sh.path.getBounds().let { b -> b.right > 140f && !(b.left > 740f && b.top > 400f) }) paint(sh, t) }
        translate(250f, 165f) { scale(1.25f, 1.25f, Offset.Zero) { robot(equip, t, mood(s), react) } }
    }
    for (x in listOf(650f, 880f)) drawLine(W.joint, Offset(x, 60f), Offset(x, ground - 316f), 3f)
    drawRoundRect(W.grow, Offset(620f, ground - 320f), Size(290f, 14f), CornerRadius(7f), alpha = 0.3f, style = Stroke(16f))
    drawRoundRect(W.grow, Offset(620f, ground - 320f), Size(290f, 14f), CornerRadius(7f))

    drawRoundRect(Color(0x229FE3D6), Offset(620f, ground - 8f), Size(290f, 60f), CornerRadius(14f))
    drawRoundRect(Color(0x332FC6B8), Offset(626f, ground + 16f), Size(278f, 30f), CornerRadius(10f))
    drawRoundRect(W.steel, Offset(620f, ground - 8f), Size(290f, 60f), CornerRadius(14f), alpha = 0.7f, style = Stroke(2.5f))
    if (waiting) drawCircle(W.dim, 12f, Offset(765f, ground - 14f), style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
    val shown = orbits.take(6)
    val gap = 290f / max(1, shown.size)
    shown.forEachIndexed { k, o ->
        val x = 620f + gap * (k + 0.5f)
        val failed = o.slots.any { it.s == "fail" }
        drawLine(if (failed) W.joint else W.steel, Offset(x, ground), Offset(x, ground - 54f - 44f * o.slots.size), 5f, StrokeCap.Round)
        val newest = o.slots.indexOfLast { it.s == "done" && !it.other }
        val size = min(3.4f, gap / 26f) // flowers shrink in a crowded tray so neighbours do not overlap
        val lily = lily(s, o.h)
        o.slots.forEachIndexed { j, sl ->
            var grow = 1f
            if (fresh != null && fresh.first == o.h.id && j == newest) grow = min(1f, since(t, fresh.second) / 700f).let { 1 - (1 - it).pow(3) }
            val y = ground - 34f - 44f * j
            bloom(Offset(x, y), size * grow, sl, j % 2 == 1, lily)
            withTransform({ translate(x, y); scale(3f, 3f, Offset.Zero) }) { drawPath(HEX, W.joint) }
        }
    }
}

fun orbitsFor(s: Data, d: LocalDate) = s.habits.filter { active(it, d) }.map { Orbit(it, slots(s, it, d)) }
