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

