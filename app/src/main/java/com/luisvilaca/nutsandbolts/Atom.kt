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
import kotlin.math.sin

// One slot of a habit's period: its state ("" open, done, skip, fail) and whether it was logged on another day of the week.
class Slot(val s: String, val other: Boolean)

fun slots(s: Data, h: Habit, d: LocalDate): List<Slot> {
    val ms = periodMarks(s, h, d)
    return List(h.target) { i -> ms.getOrNull(i)?.let { Slot(it.second.s, it.first != d) } ?: Slot("", false) }
}

private fun shape(d: String) = PathParser().parsePathString(d).toPath()
// The diary page's marks, in a 22 x 26 box anchored at (11, 24): a bolt seen from the side, and a hex hole to fill in.
val EMBLEM = shape("M5 3H17V9H14V24H8V9H5ZM8 13L14 11M8 17L14 15M8 21L14 19")
val CELL = shape("M11 24L5.8 21V15L11 12L16.2 15V21Z")

// A slot is a bolt hole seen face on: done is a steel bolt head driven home (the bolts currency's cross on it),
// skipped is a dashed ring with a dash, failed is a sheared coral head split in two, open is an empty tapped hole.
// Each state has its own form; colour only confirms it. drive runs 0..1 as a fresh bolt screws in.
fun DrawScope.bolt(c: Offset, r: Float, slot: Slot, drive: Float = 1f) {
    val a = if (slot.other) 0.45f else 1f
    val w = r * 0.16f
    when (slot.s) {
        "done" -> {
            val h = r * (0.6f + 0.4f * drive)
            drawCircle(W.steel, h, c, alpha = a)
            drawCircle(W.joint, h - w / 2, c, alpha = a, style = Stroke(w))
            rotate(270f * (1 - drive), c) {
                drawLine(W.ground, c - Offset(h * 0.5f, 0f), c + Offset(h * 0.5f, 0f), w * 1.3f, alpha = a)
                drawLine(W.ground, c - Offset(0f, h * 0.5f), c + Offset(0f, h * 0.5f), w * 1.3f, alpha = a)
            }
        }
        "skip" -> {
            drawCircle(W.dim, r - w / 2, c, alpha = a, style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.45f, r * 0.35f))))
            drawLine(W.dim, c - Offset(r * 0.4f, 0f), c + Offset(r * 0.4f, 0f), w, alpha = a)
        }
        "fail" -> for ((start, d) in listOf(-45f to 1f, 135f to -1f)) {
            val o = Offset(r * 0.16f * d, r * 0.16f * d)
            drawArc(W.coral, start, 180f, true, c - Offset(r, r) * 0.9f + o, Size(r * 1.8f, r * 1.8f), alpha = a)
        }
        else -> {
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
    if (today) drawCircle(W.steel, size.minDimension * 0.47f, center, style = Stroke(1.5.dp.toPx()))
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
        val st = when { i < n -> "done"; i == 2 && p.fail > 0 -> "fail"; else -> "" }
        bolt(Offset(center.x + (i - 1) * pw * 0.3f, center.y), size.width * 0.085f, Slot(st, false))
    }
}

class Orbit(val h: Habit, val slots: List<Slot>)

// The bay: Rivet on its platform and, beside it, a workbench with one steel frame per habit running today, clamped
// upright with a bolt hole per slot. Under the bench a small reactor powers it all; its Cherenkov glow and the light
// rail it feeds brighten with the Core attribute. A habit just done screws its newest bolt in.
// waiting: no habits yet, so the bench holds one dashed frame.
fun DrawScope.bay(s: Data, t: Long, react: Reaction?, orbits: List<Orbit>, fresh: Pair<String, Long>?, equip: Map<String, String?> = s.equip, waiting: Boolean = false) {
    val tier = PART[equip["base"]]?.tier ?: 1
    val core = s.stats.getValue("core") / 100f
    val pulse = if (t == 0L) 1f else 0.85f + 0.15f * sin(t / 700.0).toFloat()
    val bench = 600f
    val heart = Offset(765f, 700f)
    drawCircle(Brush.radialGradient(listOf(W.glow.copy(alpha = (0.1f + 0.22f * core) * pulse), Color.Transparent), heart, 480f), 480f, heart)
    // Rivet and its base step left to share the floor with the bench
    translate(-140f, 0f) {
        // the charger would sit off the frame and its cable cut by it, and the base's own pieces would land on the
        // bench, so this look draws the base without them
        SCENE.forEach { sh -> if (sh.tier <= tier && "cable" !in sh.roles && sh.path.getBounds().let { b -> b.right > 140f && !(b.left > 740f && b.top > 400f) }) paint(sh, t) }
        translate(250f, 165f) { scale(1.25f, 1.25f, Offset.Zero) { robot(equip, t, mood(s), react) } }
    }
    // the light rail on its hanging wires
    for (x in listOf(650f, 880f)) drawLine(W.joint, Offset(x, 60f), Offset(x, 364f), 3f)
    drawRoundRect(W.glow, Offset(620f, 360f), Size(290f, 14f), CornerRadius(7f), alpha = 0.3f * (0.4f + 0.6f * core), style = Stroke(16f))
    drawRoundRect(W.glow, Offset(620f, 360f), Size(290f, 14f), CornerRadius(7f), alpha = 0.5f + 0.5f * core)

    // the bench: a steel top on two legs, and the reactor between them feeding it through a short conduit
    for (x in listOf(628f, 888f)) drawRect(W.edge, Offset(x, bench + 16f), Size(14f, 150f))
    drawRect(W.joint, Offset(758f, bench + 16f), Size(14f, 30f))
    drawRoundRect(W.raised, Offset(712f, bench + 44f), Size(106f, 124f), CornerRadius(30f))
    drawRoundRect(W.joint, Offset(712f, bench + 44f), Size(106f, 124f), CornerRadius(30f), style = Stroke(4f))
    drawRoundRect(W.glow, Offset(736f, bench + 66f), Size(58f, 80f), CornerRadius(16f), alpha = (0.35f + 0.55f * core) * pulse)
    for (x in listOf(752f, 765f, 778f)) drawLine(W.rail, Offset(x, bench + 74f), Offset(x, bench + 138f), 5f, StrokeCap.Round)
    drawRoundRect(W.edge, Offset(608f, bench), Size(314f, 18f), CornerRadius(6f))
    drawLine(W.steel, Offset(614f, bench + 1f), Offset(916f, bench + 1f), 2.5f, alpha = 0.7f)

    if (waiting) drawRoundRect(W.dim, Offset(750f, bench - 90f), Size(30f, 90f), CornerRadius(6f), style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
    val shown = orbits.take(6)
    val gap = 290f / max(1, shown.size)
    val r = min(13f, gap * 0.3f) // bolts shrink on a crowded bench so neighbours do not overlap
    shown.forEachIndexed { k, o ->
        val x = 620f + gap * (k + 0.5f)
        val w = r * 2.6f
        val top = bench - 30f - 44f * o.slots.size
        drawRoundRect(W.raised, Offset(x - w / 2, top), Size(w, bench - top), CornerRadius(6f))
        drawRoundRect(if (o.slots.any { it.s == "fail" }) W.joint else W.steel, Offset(x - w / 2, top), Size(w, bench - top), CornerRadius(6f), alpha = 0.7f, style = Stroke(2.5f))
        drawRect(W.joint, Offset(x - w / 2 - 8f, bench - 12f), Size(w + 16f, 12f)) // the clamp
        val newest = o.slots.indexOfLast { it.s == "done" && !it.other }
        o.slots.forEachIndexed { j, sl ->
            var drive = 1f
            if (fresh != null && fresh.first == o.h.id && j == newest) drive = min(1f, since(t, fresh.second) / 700f).let { 1 - (1 - it).pow(3) }
            bolt(Offset(x, bench - 38f - 44f * j), r, sl, drive)
        }
    }
}

fun orbitsFor(s: Data, d: LocalDate) = s.habits.filter { active(it, d) }.map { Orbit(it, slots(s, it, d)) }
