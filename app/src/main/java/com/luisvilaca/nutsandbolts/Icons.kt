package com.luisvilaca.nutsandbolts

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

// Icons share one robot grammar: angular strokes, and a pivot joint (a small circle) wherever a part would hinge.
// Drawn as 24dp line art at 1.8 stroke with square caps; Icon() tints them.
// bolts: a bolt seen side on (hex head, threaded shank), the currency; a slot's bolt is drawn face on in Atom.kt.
// settings: a gear. habits: a frame on the bench, its top bolt driven. shop: a parts crate with a nut stencilled on.
// Shared paths; the skin adds its themed ones (SKIN_ICONS: the currencies, attributes and tabs) and may redraw any.
private val PATHS = mapOf(
    "paper" to "M6 3h9l4 4v14H6zM15 3v4h4M9 11h4.400M9 14.500h2.200M9 18h4.400M13.6 11a1.1 1.1 0 1 0 2.2 0a1.1 1.1 0 1 0 -2.2 0zM11.4 14.5a1.1 1.1 0 1 0 2.2 0a1.1 1.1 0 1 0 -2.2 0zM13.6 18a1.1 1.1 0 1 0 2.2 0a1.1 1.1 0 1 0 -2.2 0z",
    "prev" to "M14.500 5.500 9.700 10.700M9.700 13.300l4.800 5.200M7 12a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "next" to "M9.500 5.500l4.800 5.200M14.300 13.300l-4.800 5.200M13.8 12a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "plus" to "M12 5v4.500M12 14.500V19M5 12h4.500M14.500 12H19M10.5 12a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0z",
    "minus" to "M5 12h14",
    "calendar" to "M4 6h16v14H4zM4 10.500h16M8.500 3.500v4.500M15.500 3.500v4.500M7.500 14h3M7.500 17h3M13.8 15.5a1.7 1.7 0 1 0 3.4 0a1.7 1.7 0 1 0 -3.4 0z",
    "undo" to "M9 5.500 5 9.500l4 4M5 9.500h9.500l4 4V16M16.9 17.8a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "redo" to "M15 5.500l4 4-4 4M19 9.500H9.500l-4 4V16M3.9 17.8a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "eraser" to "M4.500 14.500 11 8l5.500 5.500L11 19H9zM7.750 11.250l5.500 5.500M14 10.500l3.800-3.800M4 21.500h16M17.4 5.6a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0z",
    "move" to "M12 3v7.5M12 13.5V21M3 12h7.5M13.5 12H21M9.5 5.5 12 3l2.5 2.5M9.5 18.5 12 21l2.5-2.5M5.5 9.5 3 12l2.5 2.5M18.5 9.5 21 12l-2.5 2.5M10.5 12a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0z",
    "trash" to "M6.500 9h11l-1 11.500h-9zM4.500 9h15M12 9V5.500M10 13v3M14 13v3M10.8 4.3a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z",

    "settings" to "M18.67 9.89L20.96 9.93L20.96 14.07L18.67 14.11L18.21 15.23L19.80 16.88L16.88 19.80L15.23 18.21L14.11 18.67L14.07 20.96L9.93 20.96L9.89 18.67L8.77 18.21L7.12 19.80L4.20 16.88L5.79 15.23L5.33 14.11L3.04 14.07L3.04 9.93L5.33 9.89L5.79 8.77L4.20 7.12L7.12 4.20L8.77 5.79L9.89 5.33L9.93 3.04L14.07 3.04L14.11 5.33L15.23 5.79L16.88 4.20L19.80 7.12L18.21 8.77ZM9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0z",
    "fail" to "M6.5 6.5l11 11M17.5 6.5l-11 11",
    "check" to "M5 12.5l4.5 4.5L19 7.5",
    "more" to "M10.6 5.5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0zM10.6 12a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0zM10.6 18.5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
    "history" to "M4 18h4V12h4v-6h4v10h4M4 21h16",
    "lock" to "M6 11h12v9.5H6zM8.5 11V7.5L10 6h4l1.5 1.5V11M10.6 15.5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
    "export" to "M12 3.5v10M8.5 10l3.5 3.5 3.5-3.5M5 15v5h14v-5M10.7 3.3a1.3 1.3 0 1 0 2.6 0a1.3 1.3 0 1 0 -2.6 0z",
)

val ICONS: Map<String, ImageVector> = (PATHS + SKIN_ICONS).mapValues { (k, d) ->
    ImageVector.Builder(k, 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString(d).toNodes(), stroke = SolidColor(Color.White), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Square, strokeLineJoin = StrokeJoin.Miter)
        .build()
}
