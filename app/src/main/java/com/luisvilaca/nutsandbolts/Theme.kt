package com.luisvilaca.nutsandbolts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Reactor Workshop: a robot's workbench after dark, powered by a small reactor. Deep graphite blue, Cherenkov blue
// only as the reactor's light, ready green only where something can be pressed or is done, coral only for failed.
object W {
    val ground = Color(0xFF0C1320)
    val rail = Color(0xFF070C15)
    val raised = Color(0xFF17223A)
    val line = Color(0xFF1B2638)
    val edge = Color(0xFF2A3A55)
    val text = Color(0xFFE8EEF7)
    val dim = Color(0xFF94A3BA)
    val steel = Color(0xFFD3DDEA)
    val act = Color(0xFF6EE787) // a ready lamp
    val onAct = Color(0xFF06281A)
    val gold = Color(0xFFF2C14E)
    val coral = Color(0xFFFF6B4A)
    val joint = Color(0xFF6F809A)
    val glass = Color(0xFF070C15)
    val glow = Color(0xFF4CC9FF) // the reactor's Cherenkov light
}

private fun lexend(vararg w: Int) = FontFamily(w.map { Font(R.font.lexend, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) })
val display = lexend(600, 700)
val body = lexend(400, 500, 600)

// One rounded, open face (Lexend) for everything; labels are small spaced caps.
val Label = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.14.em, color = W.dim)
val Numeral = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, fontFeatureSettings = "tnum")

private val type = Typography(
    displaySmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em),
    headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = body, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = body, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 16.sp),
)

@Composable
fun NbTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(
        primary = W.act, onPrimary = W.onAct,
        secondary = W.steel, onSecondary = W.ground,
        secondaryContainer = W.raised, onSecondaryContainer = W.text,
        background = W.ground, onBackground = W.text,
        surface = W.ground, onSurface = W.text, onSurfaceVariant = W.dim,
        surfaceContainer = W.rail, surfaceContainerHigh = W.raised, surfaceContainerHighest = W.raised,
        inverseSurface = W.steel, inverseOnSurface = W.ground, inversePrimary = W.onAct,
        error = W.coral, onError = W.ground,
        outline = W.edge, outlineVariant = W.line,
    ),
    typography = type,
    content = content,
)

// How each drawing role in Parts.kt is painted.
class Ink(val fill: Brush? = null, val stroke: Color? = null, val width: Float = 0f, val alpha: Float = 1f, val dash: FloatArray? = null, val butt: Boolean = false)

private val shell = Brush.linearGradient(listOf(Color(0xFFF7FAFD), Color(0xFFBFCADB)), start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(150f, 500f))
private fun f(c: Color, a: Float = 1f) = Ink(fill = SolidColor(c), alpha = a)
private fun s(c: Color, w: Float, a: Float = 1f, dash: FloatArray? = null) = Ink(stroke = c, width = w, alpha = a, dash = dash)
val FLOOR = Color(0xFF0F1828)

val ROLES: Map<String, Ink> = mapOf(
    "sh" to Ink(fill = shell), "shr" to Ink(stroke = Color(0xFFE3EAF3), width = 14f), "sh2" to f(Color(0xFFA9B7CB)),
    "gl" to f(W.glass), "space" to f(W.glass), "zn" to f(W.joint), "br" to f(W.gold), "ey" to f(W.act), "mt" to f(W.act),
    "cr" to f(W.coral), "ice" to f(Color(0xFF9FDCFF)), "moon" to f(W.steel), "leaf" to f(W.act), "core" to f(W.glow), "plasma" to s(W.glow, 5f, dash = floatArrayOf(14f, 10f)), "scarf" to f(W.coral),
    "ln" to s(W.dim, 3.5f), "hl" to s(Color.White, 5f, 0.28f), "claw" to s(W.joint, 8f), "spring" to s(W.joint, 8f),
    "tread" to s(W.joint, 6f, dash = floatArrayOf(6f, 8f)), "ring" to s(W.act, 5f, dash = floatArrayOf(20f, 12f)), "rod" to s(W.joint, 6f),
    "stem" to s(Color(0xFF3FA866), 6f), "halo" to s(W.gold, 7f), "bowl" to Ink(SolidColor(Color(0x339FE3D6)), Color(0xBFFFFFFF), 4f),
    "water" to f(Color(0x4D4CC9FF)), "bub" to s(Color.White, 2f, 0.7f), "smile" to s(W.act, 5f), "mag" to Ink(stroke = W.coral, width = 12f, butt = true),
    "magtip" to Ink(stroke = W.steel, width = 12f, butt = true), "tent" to s(Color(0xFFDCEBE6), 16f), "mono" to Ink(SolidColor(Color(0x269FE3D6)), W.gold, 4f),
    "chain" to s(W.gold, 2.5f), "orbit" to s(Color.White, 2f, 0.3f),
    // the bay (its floor is transparent: BayPanel lays the floor edge to edge)
    "wire" to s(W.glass, 3f), "stars" to s(Color.White, 4f, 0.4f), "pring" to s(W.steel, 5f), "cone" to f(W.glow, 0.05f),
    "panel" to f(Color(0xFF15213A)), "tool" to s(W.joint, 9f), "jar" to f(Color.White, 0.16f), "jarfill" to s(W.gold, 10f),
    "floor" to f(Color.Transparent), "tower" to Ink(SolidColor(Color(0xFF22304A)), W.glow.copy(alpha = 0.35f), 3f), "steam" to f(Color.White, 0.1f), "skirt" to f(W.steel, 0.1f), "orb" to s(W.glow, 2.5f, 0.5f), "cable" to s(W.glass, 9f),
    "plat-side" to f(Color(0xFF0E1626)), "plat-top" to Ink(SolidColor(Color(0xFF22304A)), Color(0x38FFFFFF), 3f),
    "plat-ring" to s(W.act, 4f, 0.85f, floatArrayOf(26f, 18f)),
)
