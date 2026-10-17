package com.luisvilaca.nutsandbolts

import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Shape(val roles: List<String>, val d: String, val m: FloatArray? = null, val layer: String = "", val pulse: Boolean = false) {
    val path: Path by lazy {
        PathParser().parsePathString(d).toPath().apply {
            if (m != null) transform(Matrix().apply { values[0] = m[0]; values[1] = m[1]; values[4] = m[2]; values[5] = m[3]; values[12] = m[4]; values[13] = m[5] })
        }
    }
}

class Part(
    val id: String, val slot: String, val name: String, val no: String,
    val bolts: Int = 0, val nuts: Int = 0, val box: FloatArray? = null, val at: String? = null, val tier: Int = 0,
    val pivots: Map<String, Offset> = emptyMap(), val shapes: List<Shape> = emptyList(), val pal: Map<String, Ink> = emptyMap(),
)

val PART = PARTS.associateBy { it.id }
val SLOTS = linkedMapOf("head" to "Head", "body" to "Body", "arms" to "Arms", "legs" to "Legs", "acc" to "Accessories", "base" to "Base")

// What the robot is doing right now, set by logging and buying.
class Reaction(val kind: String, val at: Long)

// One clock for every animation on screen; frozen when the system's animations are off.
@Composable
fun clock(): Long {
    val ctx = LocalContext.current
    val off = remember { Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
    var t by remember { mutableLongStateOf(0L) }
    if (!off) LaunchedEffect(Unit) { while (true) withFrameMillis { t = it } }
    return t
}

// Milliseconds on the shared clock since `at`. A frozen clock (animations off) reads as long settled, so anything
// keyed to a fresh mark shows its end state at once instead of its first frame.
fun since(t: Long, at: Long) = if (t == 0L) Long.MAX_VALUE / 2 else (t - at).coerceAtLeast(0)

private fun wave(t: Long, period: Float) = sin(2 * PI * (t % (period * 1000).toLong()) / (period * 1000)).toFloat()

// pal: the part's finish, which overrides the house roles it names.
fun DrawScope.paint(sh: Shape, t: Long, alpha: Float = 1f, pal: Map<String, Ink> = emptyMap()) {
    val a = alpha * if (sh.pulse) 0.725f + 0.275f * cos(2 * PI * (t % 2200) / 2200).toFloat() else 1f
    for (r in sh.roles) {
        val ink = pal[r] ?: ROLES[r] ?: continue
        if (r == "spin") continue
        val spin = "spin" in sh.roles
        val draw: DrawScope.() -> Unit = {
            ink.fill?.let { drawPath(sh.path, it, alpha = ink.alpha * a) }
            ink.stroke?.let {
                val phase = if (r == "plat-ring" || r == "plasma") -(t % 3000) / 3000f * 88f else 0f
                drawPath(sh.path, it, alpha = ink.alpha * a, style = Stroke(ink.width, cap = if (ink.butt) StrokeCap.Butt else StrokeCap.Round, join = StrokeJoin.Round,
                    pathEffect = ink.dash?.let { d -> PathEffect.dashPathEffect(d, phase) }))
            }
        }
        if (spin) rotate((t % 7000) / 7000f * 360f, sh.path.getBounds().center) { draw() } else draw()
    }
}

// The robot in its 400x520 space. mood: 0 low power. reaction: cheer, sad or wave while it plays.
fun DrawScope.robot(equip: Map<String, String?>, t: Long, mood: Int = 1, react: Reaction? = null) {
    val p = { slot: String -> PART[equip[slot]] }
    val acc = PART[equip["acc"]]
    val since = react?.let { t - it.at } ?: Long.MAX_VALUE
    val kind = if (since in 0..1500) react!!.kind else ""

    val hop = if (kind == "cheer" && since < 1000) -36f * sin(PI * (since % 500) / 500).toFloat() else 0f
    val bob = -3f * (1 - cos(2 * PI * (t % 3400) / 3400).toFloat())
    val head = when { kind == "sad" -> 10f; mood == 0 -> 6f + wave(t, 6.8f); else -> 0.25f + 2.25f * wave(t, 6.8f) }
    val blink = ((t % 4600) / 4600f).let { if (it in 0.93f..1f) 1 - 0.92f * (1 - kotlin.math.abs(it - 0.965f) / 0.035f) else 1f }
    val eyes = when (kind) { "cheer" -> 0.55f; "sad" -> 0.3f; else -> blink }
    val sway = 3f * wave(t, 3.4f)
    val armL = if (kind == "cheer") 140f else sway
    val armR = when (kind) {
        "cheer" -> -140f
        "wave" -> if (since < 1300) -120f - 30f * (0.5f + 0.5f * cos(2 * PI * since / 300).toFloat()) * min(1f, since / 200f) else -sway
        else -> -sway
    }
    val eyeAlpha = if (mood == 0) 0.6f else 1f

    fun part(pt: Part?) = pt?.shapes?.forEach { sh ->
        when (sh.layer) {
            "armL" -> rotate(armL, Offset(120f, 264f)) { paint(sh, t, pal = pt.pal) }
            "armR" -> rotate(armR, Offset(280f, 264f)) { paint(sh, t, pal = pt.pal) }
            "eyes" -> scale(1f, eyes, pt.pivots["eyes"] ?: Offset(200f, 150f)) { paint(sh, t, eyeAlpha, pt.pal) }
            "prop" -> scale(0.12f + 0.88f * kotlin.math.abs(cos(PI * (t % 450) / 450).toFloat()), 1f, pt.pivots["prop"] ?: Offset(200f, 54f)) { paint(sh, t, pal = pt.pal) }
            "hover" -> translate(0f, -3f * (1 - cos(2 * PI * (t % 2200) / 2200).toFloat())) { paint(sh, t, pal = pt.pal) }
            "moon" -> {
                // place the moon on its untilted orbit, then turn that offset by the tilt baked into the path
                val a = 2 * PI * (t % 7000) / 7000
                val x = (200 + 112 * cos(a)).toFloat(); val y = (150 + 34 * sin(a)).toFloat(); val m = sh.m!!
                translate(m[0] * x + m[2] * y, m[1] * x + m[3] * y) { paint(sh, t, pal = pt.pal) }
            }
            else -> if ("halo" in sh.roles) translate(0f, -3f * (1 - cos(2 * PI * (t % 2600) / 2600).toFloat())) { paint(sh, t, pal = pt.pal) } else paint(sh, t, pal = pt.pal)
        }
    }
    fun accAt(k: String) { if (acc?.at == k) part(acc) }

    drawOval(Color.Black, Offset(108f, 462f), androidx.compose.ui.geometry.Size(184f, 24f), alpha = 0.3f)
    translate(0f, hop) {
        part(p("legs"))
        translate(0f, bob) {
            accAt("body-back")
            // the neck takes the body's joint colour
            drawRoundRect(p("body")?.pal?.get("zn")?.fill ?: androidx.compose.ui.graphics.SolidColor(W.joint), Offset(187f, 198f), androidx.compose.ui.geometry.Size(26f, 42f), androidx.compose.ui.geometry.CornerRadius(6f))
            part(p("body"))
            rotate(head, Offset(200f, 212f)) { accAt("head-back"); part(p("head")); accAt("head-front") }
            part(p("arms"))
            accAt("front")
        }
    }
}

// Fits a w x h drawing space into the canvas: contain (meet) or cover (slice), anchored bottom centre like the bay.
fun DrawScope.fit(w: Float, h: Float, cover: Boolean = false, block: DrawScope.() -> Unit) {
    val k = if (cover) max(size.width / w, size.height / h) else min(size.width / w, size.height / h)
    withTransform({ translate((size.width - w * k) / 2, size.height - h * k); scale(k, k, Offset.Zero) }, block)
}

@Composable
fun RobotView(store: Store, modifier: Modifier = Modifier, equip: Map<String, String?> = store.s.equip) {
    val t = clock()
    Canvas(modifier) { fit(400f, 520f) { robot(equip, t, mood(store.s), store.react) } }
}

// One part on its own, square-cropped for a shop tile (bases draw their room instead, see Shop.kt).
fun DrawScope.thumb(p: Part, t: Long = 0) {
    val b = p.box ?: return
    val s = max(b[2], b[3]) * 1.12f
    val k = min(size.width, size.height) / s
    withTransform({ translate(size.width / 2, size.height / 2); scale(k, k, Offset.Zero); translate(-(b[0] + b[2] / 2), -(b[1] + b[3] / 2)) }) { p.shapes.forEach { paint(it, t, pal = p.pal) } }
}
