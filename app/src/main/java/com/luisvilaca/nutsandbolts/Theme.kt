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
import androidx.compose.ui.geometry.Offset

// One rounded, open face (Lexend) for everything; labels are small spaced caps.
val Label get() = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.14.em, color = W.dim)
val Numeral get() = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, fontFeatureSettings = "tnum")

private fun type() = Typography(
    displaySmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.01).em),
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
    typography = type(), // read again when the language, and with it the face, changes
    content = content,
)

// How each drawing role in Parts.kt is painted.
class Ink(val fill: Brush? = null, val stroke: Color? = null, val width: Float = 0f, val alpha: Float = 1f, val dash: FloatArray? = null, val butt: Boolean = false)

private val shell = Brush.linearGradient(listOf(Color(0xFFF7FCFA), Color(0xFFBFD9D1)), start = Offset(0f, 0f), end = Offset(150f, 500f))
private fun f(c: Color, a: Float = 1f) = Ink(fill = SolidColor(c), alpha = a)
private fun s(c: Color, w: Float, a: Float = 1f, dash: FloatArray? = null) = Ink(stroke = c, width = w, alpha = a, dash = dash)

val ROLES: Map<String, Ink> = mapOf(
    "sh" to Ink(fill = shell), "shr" to Ink(stroke = Color(0xFFE2F0EB), width = 14f), "sh2" to f(Color(0xFFA9C9C1)),
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
    "panel" to f(Color(0xFF12302B)), "tool" to s(W.joint, 9f), "jar" to f(Color.White, 0.16f), "jarfill" to s(W.gold, 10f),
    "floor" to f(Color.Transparent), "tower" to Ink(SolidColor(Color(0xFF1F4640)), W.glow.copy(alpha = 0.35f), 3f), "steam" to f(Color.White, 0.1f), "skirt" to f(W.steel, 0.1f), "orb" to s(W.glow, 2.5f, 0.5f), "cable" to s(W.glass, 9f),
    "plat-side" to f(Color(0xFF0E2420)), "plat-top" to Ink(SolidColor(Color(0xFF1F4640)), Color(0x38FFFFFF), 3f),
    "plat-ring" to s(W.act, 4f, 0.85f, floatArrayOf(26f, 18f)),
)

// Finishes: each part can carry its own enamel, so a robot assembled from the catalogue becomes a patchwork of colour.
// The starter parts keep the white factory shell; everything else is painted. Eyes take the finish's own colour.
private fun finish(top: Long, bottom: Long, flat: Long, joint: Long, eye: Long, glass: Long): Map<String, Ink> {
    val j = Color(joint)
    return mapOf(
        "sh" to Ink(fill = Brush.linearGradient(listOf(Color(top), Color(bottom)), Offset(0f, 0f), Offset(150f, 500f))),
        "shr" to Ink(stroke = Color(top), width = 14f), "sh2" to f(Color(flat)), "zn" to f(j), "gl" to f(Color(glass)),
        "ey" to f(Color(eye)), "smile" to s(Color(eye), 5f), "tent" to s(Color(flat), 16f),
        "claw" to s(j, 8f), "spring" to s(j, 8f), "rod" to s(j, 6f), "tread" to s(j, 6f, dash = floatArrayOf(6f, 8f)),
    )
}
val COPPER = finish(0xFFF6B98A, 0xFFB8643A, 0xFFD98B5F, 0xFF6B3A22, 0xFFFFC14D, 0xFF1C0F0A)
val COBALT = finish(0xFF8FB2FF, 0xFF2E4DB3, 0xFF5677D6, 0xFF1F2C5C, 0xFFE8F6FF, 0xFF0A1030)
val SIGNAL = finish(0xFFFFB36B, 0xFFE0661F, 0xFFF08A3E, 0xFF4A2A18, 0xFF3CF2FF, 0xFF1A0E06)
val MINT = finish(0xFFD2F7E9, 0xFF6CC4A8, 0xFF8FD9BF, 0xFF2E6B5C, 0xFFFF6FA8, 0xFF0E2620)
val STEALTH = finish(0xFF6A7488, 0xFF2C3342, 0xFF434C60, 0xFF3A4252, 0xFFFF4DD8, 0xFF1E2430)
val BRASS = finish(0xFFFBE3A1, 0xFFC0902F, 0xFFDDB45A, 0xFF6B4E14, 0xFF7CF2FF, 0xFF1E1606)
val VIOLET = finish(0xFFDCCEFF, 0xFF7E62D9, 0xFFA58EEB, 0xFF3A2A70, 0xFFFFE066, 0xFF140C2E)
val URANIUM = finish(0xFFDDFFA6, 0xFF78C83A, 0xFFA6E266, 0xFF2D4A16, 0xFFFFFFFF, 0xFF0D1606)
val CHROME = finish(0xFFFFFFFF, 0xFF8E9AAE, 0xFFB9C3D3, 0xFF4A5468, 0xFF4CC9FF, 0xFF0A0F18)
val HAZARD = finish(0xFFFFE36A, 0xFFE6B000, 0xFFF2C631, 0xFF2A2A2A, 0xFFFF8A00, 0xFF151515)
val ROSE = finish(0xFFFFCCDD, 0xFFE0709A, 0xFFF09AB8, 0xFF6A2A44, 0xFF7CF2C8, 0xFF230A14)
val CHERENKOV = finish(0xFF4A70BC, 0xFF15244A, 0xFF27407A, 0xFF0C1530, 0xFF4CC9FF, 0xFF050A18)
