package com.luisvilaca.nutsandbolts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

// Every colour that sets text stays readable (WCAG AA, 4.5:1) on every surface it sits on.
class TokensTest {
    private fun ratio(a: Color, b: Color) = (maxOf(a.luminance(), b.luminance()) + 0.05f) / (minOf(a.luminance(), b.luminance()) + 0.05f)

    @Test fun textContrast() {
        val surfaces = mapOf("rail" to W.rail, "ground" to W.ground, "card" to W.card, "raised" to W.raised)
        val inks = mapOf("text" to W.text, "dim" to W.dim, "steel" to W.steel, "coral" to W.coral, "gold" to W.gold, "act" to W.act)
        for ((sn, s) in surfaces) for ((n, c) in inks) assertTrue("$n on $sn is ${ratio(c, s)}", ratio(c, s) >= 4.5f)
        assertTrue("on-ready on ready", ratio(W.onAct, W.act) >= 4.5f)
    }

    // the ladder steps up visibly: rail < ground < card < raised
    @Test fun surfaceLadder() {
        val l = listOf(W.rail, W.ground, W.card, W.raised).map { it.luminance() }
        assertTrue(l.zipWithNext().all { (a, b) -> b > a })
    }
}
