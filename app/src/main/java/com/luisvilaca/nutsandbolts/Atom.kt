package com.luisvilaca.nutsandbolts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

// One slot of a habit's period: its state ("" open, done, skip, fail) and whether it was logged on another day of the week.
class Slot(val s: String, val other: Boolean)

fun slots(s: Data, h: Habit, d: LocalDate): List<Slot> {
    val ms = periodMarks(s, h, d)
    return List(h.target) { i -> ms.getOrNull(i)?.let { Slot(it.second.s, it.first != d) } ?: Slot("", false) }
}

// The electron: each state has its own form, lit, hollow, barred, struck; colour only confirms it.
fun DrawScope.electron(c: Offset, r: Float, slot: Slot, burst: Float = -1f) {
    val a = if (slot.other) 0.45f else 1f
    val w = r * 0.28f
    when (slot.s) {
        "done" -> {
            drawCircle(W.turq, r * 0.72f, c, alpha = a)
            drawCircle(W.turq, r * 1.05f, c, alpha = 0.45f * a, style = Stroke(w * 0.7f))
        }
        "skip" -> {
            drawCircle(W.steel, r * 0.8f, c, alpha = a, style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.35f, r * 0.3f))))
            drawLine(W.steel, c - Offset(r * 0.55f, 0f), c + Offset(r * 0.55f, 0f), w, alpha = a)
        }
        "fail" -> {
            drawCircle(W.coral, r * 0.8f, c, alpha = a, style = Stroke(w))
            drawLine(W.coral, c - Offset(r * 0.56f, -r * 0.56f), c + Offset(r * 0.56f, -r * 0.56f), w, alpha = a)
        }
        else -> drawCircle(W.dim, r * 0.8f, c, alpha = a, style = Stroke(w))
    }
    // the starburst: eight rays thrown out once as the electron lands
    if (burst in 0f..1f) for (i in 0 until 8) {
        val ang = i * PI / 4
        val d = Offset(cos(ang).toFloat(), sin(ang).toFloat())
        drawLine(W.turq, c + d * (r * (1.2f + 1.6f * burst)), c + d * (r * (1.5f + 2.6f * burst)), w * 0.8f, alpha = 1 - burst)
    }
}

@Composable
fun ElectronMark(slot: Slot, size: Dp = 26.dp, modifier: Modifier = Modifier) =
    Canvas(modifier.size(size)) { electron(center, this.size.minDimension / 2.6f, slot) }

// A day as a small atom: the nucleus grows with the share done, the orbit says how the day went.
@Composable
fun MiniAtom(p: Progress, future: Boolean, modifier: Modifier = Modifier) = Canvas(modifier) {
    val c = center
    val rx = size.width * 0.44f; val ry = rx * 0.36f
    val col = when { p.fail > 0 -> W.coral; p.done > 0 -> W.turq; else -> W.orbit }
    rotate(-18f, c) {
        drawOval(col, Offset(c.x - rx, c.y - ry), Size(rx * 2, ry * 2), style = Stroke(size.width * 0.035f,
            pathEffect = if (future) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null))
    }
    if (p.slots > 0 && (p.done > 0 || p.fail > 0)) drawCircle(W.turq, size.width * (0.05f + 0.11f * p.done / p.slots), c)
    else if (p.slots > 0) drawCircle(W.orbit, size.width * 0.04f, c)
}

class Orbit(val h: Habit, val slots: List<Slot>)

private val TILT = floatArrayOf(0f, 32f, -32f, 64f, -64f, 90f)
private const val CX = 500f
private const val CY = 520f

// The bay: the base scene, the robot on its platform, and today's habits as orbits around it.
// fresh: the habit whose newest done electron is still travelling home, and when it set off.
// waiting: no habits yet, so one empty dashed orbit stands ready around the robot.
fun DrawScope.bay(s: Data, t: Long, react: Reaction?, orbits: List<Orbit>, fresh: Pair<String, Long>?, equip: Map<String, String?> = s.equip, waiting: Boolean = false) {
    val tier = PART[equip["base"]]?.tier ?: 1
    scene(tier, t)
    if (waiting) drawOval(W.orbit, Offset(CX - 320f, CY - 110f), Size(640f, 220f), // clears the wall socket
        style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), -(t % 6000) / 6000f * 64f)))
    val shown = orbits.take(TILT.size)
    val drift = (t % 90_000) / 90_000f * 2 * PI.toFloat()
    fun place(k: Int, theta: Float): Offset {
        val rx = 400f - k * 14f; val ry = 118f
        val a = TILT[k] * PI.toFloat() / 180
        val x = rx * cos(theta); val y = ry * sin(theta)
        return Offset(CX + x * cos(a) - y * sin(a), CY + x * sin(a) + y * cos(a))
    }
    shown.forEachIndexed { k, o ->
        val col = when { o.slots.any { it.s == "fail" } -> W.coral; o.slots.all { it.s == "done" } -> W.turq; else -> W.orbit }
        rotate(TILT[k], Offset(CX, CY)) {
            drawOval(col, Offset(CX - (400f - k * 14f), CY - 118f), Size(2 * (400f - k * 14f), 236f), alpha = if (col == W.orbit) 0.9f else 0.55f, style = Stroke(2.5f))
        }
    }
    translate(250f, 165f) { scale(1.25f, 1.25f, Offset.Zero) { robot(equip, t, mood(s), react) } }
    shown.forEachIndexed { k, o ->
        val newest = o.slots.indexOfLast { it.s == "done" && !it.other }
        o.slots.forEachIndexed { j, sl ->
            var theta = 2 * PI.toFloat() * j / o.slots.size + k * 1.1f + drift
            var burst = -1f
            if (fresh != null && fresh.first == o.h.id && j == newest) {
                val e = since(t, fresh.second)
                val run = min(1f, e / 900f)
                theta -= PI.toFloat() * (1 - run).pow(3) // eases home from the far side of the orbit
                if (e in 900..1400) burst = (e - 900) / 500f
            }
            electron(place(k, theta), 15f, sl, burst)
        }
    }
}

fun orbitsFor(s: Data, d: LocalDate) = s.habits.filter { active(it, d) }.map { Orbit(it, slots(s, it, d)) }
