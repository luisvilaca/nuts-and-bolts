package com.luisvilaca.nutsandbolts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

// The skin: every interface colour and the typeface, in one file. Screens, cards and the bay only ever read W and the
// two faces, so a second look for the same app (same layouts, same functions) is this file written again, built as its
// own product flavour with its own app id. The robot's finishes and the rooms' art stay in Theme.kt and Atom.kt.

// Reactor Workshop: a robot's workbench after dark, powered by a small reactor. Deep greenhouse teal (the Grow Room's
// tint), uranium green as every glow (the reactor, its light, the gauges, a completed habit), ready green only where
// something can be pressed or is done, coral only for failed.
object W {
    // the surface ladder, each one visible step up: rail < ground < card < raised
    val rail = Color(0xFF081313)
    val ground = Color(0xFF0D1615)
    val raised = Color(0xFF1C3A34) // a control on a card
    val line = Color(0xFF1B3632)
    val edge = Color(0xFF24443E)
    val text = Color(0xFFE6F2EE)
    val dim = Color(0xFF9EB3AC) // 8:1 on ground, 7:1 on a card
    val steel = Color(0xFFCFE3DE)
    val act = Color(0xFF6EE787) // a ready lamp
    val onAct = Color(0xFF06281A)
    val gold = Color(0xFFF2C14E)
    val coral = Color(0xFFFF7457)
    val joint = Color(0xFF6C8F87)
    val glass = Color(0xFF081313)
    val card = Color(0xFF152220) // the surface of a card
    val border = Color.White.copy(alpha = 0.08f) // a card's edge
    val sel = Color(0xFFCFE3DE).copy(alpha = 0.55f) // a selection's edge: steel, quietened
    val selFill = Color(0xFFCFE3DE).copy(alpha = 0.06f) // and its faint fill
    val glow = Color(0xFFA6FF4D) // every glow: the reactor, its light, the gauges, a habit gone critical
}

private fun face(res: Int, vararg w: Int) = FontFamily(w.map { Font(res, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) })
// Lexend has no Cyrillic, so Russian is set in Manrope (OFL, licenses/), the nearest open geometric face that has it;
// without it Android falls back to the system face and loses the weights.
private val lexend = face(R.font.lexend, 600, 700) to face(R.font.lexend, 400, 500, 600)
private val manrope = face(R.font.manrope, 600, 700, 800) to face(R.font.manrope, 400, 500, 600)
val display get() = (if (Lang.code == "ru") manrope else lexend).first
val body get() = (if (Lang.code == "ru") manrope else lexend).second


// The skin's icons (Icons.kt): the currencies, the attributes and the tabs, in the robot grammar: angular strokes, and a
// pivot joint (a small circle) wherever a part would hinge.
val SKIN_ICONS = mapOf(
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
)

// The skin's words over the phrase tables (Lang.kt), per language; the workshop's are the tables themselves.
val SKIN_WORDS: Map<String, Map<String, String>> = emptyMap()
val SKIN_NOUNS: Map<String, Map<String, List<String>>> = emptyMap()

// which look this build wears (the buddy card tells the other app)
const val SKIN = "workshop"
