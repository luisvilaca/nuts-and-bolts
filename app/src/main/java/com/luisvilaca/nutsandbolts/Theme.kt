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

// Atomium Night: Expo 58 after dark. Night indigo ground, polished steel, turquoise only where something can be
// pressed or is done, gold for nuts, coral only for failed.
object W {
    val ground = Color(0xFF0E1430)
    val rail = Color(0xFF0A0F25)
    val raised = Color(0xFF172050) // controls, the selected rail item
    val line = Color(0xFF1E2750) // hairlines between rows
    val orbit = Color(0xFF2B3870)
    val text = Color(0xFFE8EDF5)
    val dim = Color(0xFF97A3C2)
    val steel = Color(0xFFC9D3E0)
    val turq = Color(0xFF3FE0C5)
    val onTurq = Color(0xFF052823)
    val gold = Color(0xFFF2C14E)
    val coral = Color(0xFFFF6B4A)
    val joint = Color(0xFF7F8BA8)
    val glass = Color(0xFF080C1E)
}

val jost = FontFamily(
    Font(R.font.jost, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.jost, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.jost, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)
val michroma = FontFamily(Font(R.font.michroma))

// Michroma is wide: display sizes stay modest and labels carry tracking instead of weight.
val Label = TextStyle(fontFamily = michroma, fontSize = 11.sp, letterSpacing = 0.14.em, color = W.dim)
// counts and balances in Jost tabular figures: Michroma's squared zero reads as a missing glyph on its own
val Numeral = TextStyle(fontFamily = jost, fontWeight = FontWeight.Medium, fontSize = 18.sp, fontFeatureSettings = "tnum")

private val type = Typography(
    displaySmall = TextStyle(fontFamily = michroma, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = michroma, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = jost, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = jost, fontWeight = FontWeight.Medium, fontSize = 19.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontFamily = jost, fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = jost, fontSize = 15.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = jost, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = jost, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 16.sp),
)

@Composable
fun NbTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(
        primary = W.turq, onPrimary = W.onTurq,
        secondary = W.steel, onSecondary = W.ground,
        secondaryContainer = W.raised, onSecondaryContainer = W.text,
        background = W.ground, onBackground = W.text,
        surface = W.ground, onSurface = W.text, onSurfaceVariant = W.dim,
        surfaceContainer = W.rail, surfaceContainerHigh = W.raised, surfaceContainerHighest = W.raised,
        inverseSurface = W.steel, inverseOnSurface = W.ground, inversePrimary = W.onTurq,
        error = W.coral, onError = W.ground,
        outline = W.orbit, outlineVariant = W.line,
    ),
    typography = type,
    content = content,
)

// How each drawing role in Parts.kt is painted.
class Ink(val fill: Brush? = null, val stroke: Color? = null, val width: Float = 0f, val alpha: Float = 1f, val dash: FloatArray? = null, val butt: Boolean = false)

private val shell = Brush.linearGradient(listOf(Color(0xFFFDFEFF), Color(0xFFC6D2EA)), start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(150f, 500f))
private fun f(c: Color, a: Float = 1f) = Ink(fill = SolidColor(c), alpha = a)
private fun s(c: Color, w: Float, a: Float = 1f, dash: FloatArray? = null) = Ink(stroke = c, width = w, alpha = a, dash = dash)
private val green = Color(0xFF4CCB7A)
val FLOOR = Color(0xFF121A3D)

val ROLES: Map<String, Ink> = mapOf(
    "sh" to Ink(fill = shell), "shr" to Ink(stroke = Color(0xFFE6ECF6), width = 14f), "sh2" to f(Color(0xFFB4C2E0)),
    "gl" to f(W.glass), "space" to f(W.glass), "zn" to f(W.joint), "br" to f(W.gold), "ey" to f(W.turq), "mt" to f(W.turq),
    "cr" to f(W.coral), "ice" to f(Color(0xFF9FDCFF)), "moon" to f(W.steel), "leaf" to f(green), "scarf" to f(W.coral),
    "ln" to s(Color(0xFF9AABCF), 3.5f), "hl" to s(Color.White, 5f, 0.28f), "claw" to s(W.joint, 8f), "spring" to s(W.joint, 8f),
    "tread" to s(W.joint, 6f, dash = floatArrayOf(6f, 8f)), "ring" to s(W.turq, 5f, dash = floatArrayOf(20f, 12f)), "rod" to s(W.joint, 6f),
    "stem" to s(Color(0xFF3FA866), 6f), "halo" to s(W.gold, 7f), "bowl" to Ink(SolidColor(Color(0x33AAD7FF)), Color(0xBFFFFFFF), 4f),
    "water" to f(Color(0x4D00E5FF)), "bub" to s(Color.White, 2f, 0.7f), "smile" to s(W.turq, 5f), "mag" to s(W.coral, 12f).let { Ink(stroke = it.stroke, width = 12f, butt = true) },
    "magtip" to Ink(stroke = W.steel, width = 12f, butt = true), "tent" to s(Color(0xFFDFE6F5), 16f), "mono" to Ink(SolidColor(Color(0x26AAD7FF)), W.gold, 4f),
    "chain" to s(W.gold, 2.5f), "orbit" to s(Color.White, 2f, 0.3f),
    // the bay (its floor is transparent: BayPanel lays the floor edge to edge)
    "wire" to s(W.glass, 3f), "stars" to s(Color.White, 5f, 0.8f), "pring" to s(W.steel, 5f), "cone" to f(Color.White, 0.05f),
    "panel" to f(Color(0xFF1B2558)), "tool" to s(W.joint, 9f), "jar" to f(Color.White, 0.16f), "jarfill" to s(W.gold, 10f),
    "floor" to f(Color.Transparent), "skirt" to f(W.steel, 0.12f), "orb" to s(W.turq, 2.5f, 0.45f), "cable" to s(W.glass, 9f),
    "pot" to f(W.steel), "plat-side" to f(Color(0xFF141C45)), "plat-top" to Ink(SolidColor(Color(0xFF26336E)), Color(0x38FFFFFF), 3f),
    "plat-ring" to s(W.turq, 4f, 0.85f, floatArrayOf(26f, 18f)),
)
