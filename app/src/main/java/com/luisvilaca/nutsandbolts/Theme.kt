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

// The skin's faces (Skin.kt: display for titles and numerals, body for the rest); labels are small spaced caps.
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
