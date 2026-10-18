package com.luisvilaca.nutsandbolts

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Icons share one robot grammar: angular strokes, and a pivot joint (a small circle) wherever a part would hinge.
// Drawn as 24dp line art at 1.8 stroke with square caps; Icon() tints them.
// bolts: a bolt seen side on (hex head, threaded shank), the currency; a slot's bolt is drawn face on in Atom.kt.
// settings: a gear. habits: a frame on the bench, its top bolt driven. shop: a parts crate with a nut stencilled on.
private val PATHS = mapOf(
    "bolts" to "M6.5 3.5h11v4.5h-11zM10.2 3.5v4.5M13.8 3.5v4.5M9.5 8v10l2.5 2.5 2.5-2.5V8M9.5 11.2l5 1.6M9.5 14.2l5 1.6M9.5 17.2l5 1.6",
    "nuts" to "M12 2.8l8 4.6v9.2l-8 4.6-8-4.6V7.4zM8.4 12a3.6 3.6 0 1 0 7.2 0a3.6 3.6 0 1 0 -7.2 0z",
    "battery" to "M3 8h15v8H3zM20.5 10.5v3M6.5 11v2M10 11v2",
    "processor" to "M7 7h10v10H7zM10 3.5V7M14 3.5V7M10 17v3.5M14 17v3.5M3.5 10H7M3.5 14H7M17 10h3.5M17 14h3.5M10.4 12a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "chassis" to "M7 4h10l2.5 3.5v7L17 20H7l-2.5-5.5v-7zM9 15.5h6M10 10a2 2 0 1 0 4 0a2 2 0 1 0 -4 0z",
    "core" to "M12 20l-7.5-7.5V8L8 4.5h2L12 7l2-2.5h2L19.5 8v4.5zM10.3 11.5a1.7 1.7 0 1 0 3.4 0a1.7 1.7 0 1 0 -3.4 0z",
    "base" to "M7 8.5h10l2 2v7l-2 2H7l-2-2v-7zM12 8.5v-3M9.5 13v2M14.5 13v2M2.8 12.5v3M21.2 12.5v3M10.7 4.2a1.3 1.3 0 1 0 2.6 0a1.3 1.3 0 1 0 -2.6 0z",
    "shop" to "M4 9h16v11H4zM3 5.5h18V9H3zM12 11.4l2.4 1.4v2.8L12 17l-2.4-1.4v-2.8zM11 14.2a1 1 0 1 0 2 0a1 1 0 1 0 -2 0z",
    "habits" to "M8.5 2.8h7L17 4.3V19H7V4.3zM4 21.2h16M10.1 7.4a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0 -3.8 0zM11.1 7.4h1.8M12 6.5v1.8M10.6 11.6a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0zM10.6 15.6a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
    "diary" to "M15.5 6 18 8.5l-9 9-4.5 2 2-4.5zM6.5 15 9 17.5M13 8.500l2.500 2.500M17.200 6.800l1.300-1.300M18.1 4.4a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0z",
    "paper" to "M6 3h9l4 4v14H6zM15 3v4h4M9 11h4.400M9 14.500h2.200M9 18h4.400M13.6 11a1.1 1.1 0 1 0 2.2 0a1.1 1.1 0 1 0 -2.2 0zM11.4 14.5a1.1 1.1 0 1 0 2.2 0a1.1 1.1 0 1 0 -2.2 0zM13.6 18a1.1 1.1 0 1 0 2.2 0a1.1 1.1 0 1 0 -2.2 0z",
    "prev" to "M14.500 5.500 9.700 10.700M9.700 13.300l4.800 5.200M7 12a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "next" to "M9.500 5.500l4.800 5.200M14.300 13.300l-4.800 5.200M13.8 12a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "plus" to "M12 5v4.500M12 14.500V19M5 12h4.500M14.500 12H19M10.5 12a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0z",
    "minus" to "M5 12h14",
    "calendar" to "M4 6h16v14H4zM4 10.500h16M8.500 3.500v4.500M15.500 3.500v4.500M7.500 14h3M7.500 17h3M13.8 15.5a1.7 1.7 0 1 0 3.4 0a1.7 1.7 0 1 0 -3.4 0z",
    "undo" to "M9 5.500 5 9.500l4 4M5 9.500h9.500l4 4V16M16.9 17.8a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "redo" to "M15 5.500l4 4-4 4M19 9.500H9.500l-4 4V16M3.9 17.8a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0z",
    "eraser" to "M4.500 14.500 11 8l5.500 5.500L11 19H9zM7.750 11.250l5.500 5.500M14 10.500l3.800-3.800M4 21.500h16M17.4 5.6a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0z",
    "trash" to "M6.500 9h11l-1 11.500h-9zM4.500 9h15M12 9V5.500M10 13v3M14 13v3M10.8 4.3a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z",

    "settings" to "M18.67 9.89L20.96 9.93L20.96 14.07L18.67 14.11L18.21 15.23L19.80 16.88L16.88 19.80L15.23 18.21L14.11 18.67L14.07 20.96L9.93 20.96L9.89 18.67L8.77 18.21L7.12 19.80L4.20 16.88L5.79 15.23L5.33 14.11L3.04 14.07L3.04 9.93L5.33 9.89L5.79 8.77L4.20 7.12L7.12 4.20L8.77 5.79L9.89 5.33L9.93 3.04L14.07 3.04L14.11 5.33L15.23 5.79L16.88 4.20L19.80 7.12L18.21 8.77ZM9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0z",
    "fail" to "M6.5 6.5l11 11M17.5 6.5l-11 11",
    "check" to "M5 12.5l4.5 4.5L19 7.5",
    "more" to "M10.6 5.5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0zM10.6 12a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0zM10.6 18.5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
    "history" to "M4 18h4V12h4v-6h4v10h4M4 21h16",
)

val ICONS: Map<String, ImageVector> = PATHS.mapValues { (k, d) ->
    ImageVector.Builder(k, 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString(d).toNodes(), stroke = SolidColor(androidx.compose.ui.graphics.Color.White), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Square, strokeLineJoin = StrokeJoin.Miter)
        .build()
}
