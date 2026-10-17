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

// Leaf shapes in a 22 x 26 box, each anchored where it meets the stem at (11, 24).
private fun shape(d: String) = PathParser().parsePathString(d).toPath()
val LEAF = shape("M11 24C11 14 4 10 3 3C10 3 16 9 11 24Z")
val BUD = shape("M11 24C11 18 8 15 8 11C8 8 10 6 11 5C12 6 14 8 14 11C14 15 11 18 11 24Z")
private val WILT = shape("M11 24C11 18 12 14 18 12C16 17 13 18 11 24Z")

// A slot is a leaf: done has unfurled (solid green), skipped is a dashed leaf, failed has wilted (coral, drooping),
// open is still a bud. Each state has its own form; colour only confirms it.
fun DrawScope.leaf(at: Offset, k: Float, slot: Slot, flip: Boolean = false) {
    val a = if (slot.other) 0.45f else 1f
    val line = Stroke(2f, pathEffect = if (slot.s == "skip") PathEffect.dashPathEffect(floatArrayOf(3f, 2.5f)) else null)
    withTransform({ translate(at.x, at.y); scale(if (flip) -k else k, k, Offset.Zero); translate(-11f, -24f) }) {
        when (slot.s) {
            "done" -> drawPath(LEAF, W.act, alpha = a)
            "skip" -> drawPath(LEAF, W.dim, alpha = a, style = line)
            "fail" -> drawPath(WILT, W.coral, alpha = a)
            else -> drawPath(BUD, W.dim, alpha = a, style = line)
        }
    }
}

@Composable
fun SlotMark(slot: Slot, size: Dp = 26.dp, modifier: Modifier = Modifier) =
    Canvas(modifier.size(size)) { leaf(Offset(center.x * 1.1f, this.size.height * 0.96f), this.size.height / 23f, slot) }

// A day as a small sprout: it stands taller and puts out more leaves the more was done; a wilted leaf when something
// failed; a dashed seed while the day is ahead.
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
    drawLine(W.act, base, Offset(base.x, top), size.width * 0.05f, StrokeCap.Round)
    val n = when { share >= 1f -> 3; share >= 0.5f -> 2; p.done > 0 -> 1; else -> 0 }
    for (i in 0 until n) leaf(Offset(base.x, top + size.height * (0.14f + 0.16f * i)), k, Slot("done", false), i % 2 == 1)
    if (p.fail > 0) leaf(Offset(base.x, base.y - size.height * 0.08f), k, Slot("fail", false), true)
}

class Orbit(val h: Habit, val slots: List<Slot>)

// The bay: Rivet on its platform and, beside it on the floor under a hanging magenta grow light, a glass tray with
// one sprout per habit running today and a leaf per slot. A habit just done unfurls its newest leaf.
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
        drawLine(if (failed) W.joint else W.act, Offset(x, ground), Offset(x, ground - 54f - 44f * o.slots.size), 5f, StrokeCap.Round)
        val newest = o.slots.indexOfLast { it.s == "done" && !it.other }
        o.slots.forEachIndexed { j, sl ->
            var grow = 1f
            if (fresh != null && fresh.first == o.h.id && j == newest) grow = min(1f, since(t, fresh.second) / 700f).let { 1 - (1 - it).pow(3) }
            leaf(Offset(x, ground - 34f - 44f * j), 2.8f * grow, sl, j % 2 == 1)
        }
    }
}

fun orbitsFor(s: Data, d: LocalDate) = s.habits.filter { active(it, d) }.map { Orbit(it, slots(s, it, d)) }
