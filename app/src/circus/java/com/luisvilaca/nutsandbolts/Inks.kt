package com.luisvilaca.nutsandbolts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

// How each drawing role in Parts.kt is painted. The house finish is the starter bear's brown felt; every other suit
// carries its own (below). Eyes are the one light an animatronic gives off.

private fun felt(top: Long, bottom: Long) = Brush.linearGradient(listOf(Color(top), Color(bottom)), start = Offset(0f, 0f), end = Offset(150f, 500f))
private fun f(c: Color, a: Float = 1f) = Ink(fill = SolidColor(c), alpha = a)
private fun s(c: Color, w: Float, a: Float = 1f, dash: FloatArray? = null, butt: Boolean = false) = Ink(stroke = c, width = w, alpha = a, dash = dash, butt = butt)

private val INK = Color(0xFF0B0709) // the black of a top hat, a bow tie, an empty socket
private val CREAM = Color(0xFFF3E6D3)
private val RUBY = Color(0xFFC8263A)
private val STEEL = Color(0xFF8C8F99)

val ROLES: Map<String, Ink> = mapOf(
    "sh" to Ink(fill = felt(0xFF9A6440, 0xFF4E2C1A)), "sh2" to f(Color(0xFFD2A57A)), "hl" to s(Color.White, 5f, 0.22f),
    "gl" to f(Color(0xFF140A0C)), "zn" to f(Color(0xFF4A4C56)), "ey" to f(Color(0xFF7FD3FF)), "eyw" to f(CREAM),
    "teeth" to s(CREAM, 6f, butt = true), "ln" to s(Color(0xFF1A0E10), 4f), "blush" to f(Color(0xFFFF6FA3), 0.85f),
    "beak" to f(Color(0xFFF28C28)), "beak2" to f(Color(0xFFD06A12)), "wireR" to s(Color(0xFFE5383B), 3f), "wireB" to s(Color(0xFF4CC9FF), 3f),
    "cr" to f(RUBY), "black" to f(INK), "strap" to s(INK, 5f), "smile" to s(Color(0xFF1A0E10), 4f), "tear" to s(Color(0xFF8E4FD6), 5f),
    "white" to f(CREAM), "white2" to s(CREAM, 6f), "pinkline" to s(Color(0xFFFF6FA3), 3f), "br" to f(Color(0xFFF2C14E)),
    "rod" to s(STEEL, 7f), "trim" to s(Color(0xFFF2C14E), 4f), "claw" to s(STEEL, 6f), "hook" to s(Color(0xFFD9DCE4), 6f),
    "limb" to s(INK, 14f), "stripes" to s(CREAM, 14f, dash = floatArrayOf(7f, 11f), butt = true), "fingers" to s(INK, 4f),
    "pinstripe" to s(RUBY, 5f), "stocking" to s(RUBY, 8f, butt = true), "tire" to s(INK, 9f), "spoke" to s(STEEL, 2.5f),
    "stilt" to Ink(stroke = Color(0xFFF2C14E), width = 9f, dash = floatArrayOf(12f, 8f), butt = true),
    "hatcone" to f(Color(0xFF7A4FD6)), "hatstripe" to s(Color(0xFFF2C14E), 5f), "pom" to f(Color(0xFFFF6FA3)),
    "beam" to f(Color(0xFFFFE3A0), 0.18f), "band" to s(RUBY, 6f, butt = true), "bow" to f(Color(0xFFFF6FA3)), "bow2" to f(Color(0xFFE0457F)),
    "string" to s(CREAM, 2f, 0.7f), "beanie" to f(Color(0xFF7A4FD6)), "beanieline" to s(Color(0xFFF2C14E), 4f), "blue" to f(Color(0xFF3A7BF0)),
    "cape" to f(Color(0xFF4A1F6B)), "halo" to s(Color(0xFFFFC94A), 7f), "orbit" to s(Color.White, 2f, 0.25f),
    "glowL" to Ink(fill = Brush.radialGradient(listOf(Color(0xB3FFF4D6), Color.Transparent), Offset(172f, 142f), 13f)),
    "glowR" to Ink(fill = Brush.radialGradient(listOf(Color(0xB3FFF4D6), Color.Transparent), Offset(228f, 142f), 13f)),
    "cake" to f(Color(0xFFE8B86A)), "frost" to f(Color(0xFFFF8FB8)), "candle" to s(CREAM, 3f), "flame" to f(Color(0xFFFFC94A)),
    // the ring box (Parts.kt SCENE): red drum, cream stripes, a cream top, its rim of chasing bulbs
    "plat-side" to f(Color(0xFF7A1426)), "plat-stripe" to s(CREAM, 14f, 0.85f, butt = true),
    "plat-top" to Ink(SolidColor(Color(0xFF2A1426)), Color(0x40F3E6D3), 3f),
    "plat-ring" to s(Color(0xFFFFC94A), 6f, 0.9f, floatArrayOf(2f, 20f)),
)

// A suit's finish: its felt (top to bottom), the light muzzle and belly, the endoskeleton showing at the joints, its
// eye light and the dark of its sockets.
private fun finish(top: Long, bottom: Long, flat: Long, joint: Long, eye: Long, glass: Long): Map<String, Ink> = mapOf(
    "sh" to Ink(fill = felt(top, bottom)), "sh2" to f(Color(flat)), "zn" to f(Color(joint)), "gl" to f(Color(glass)), "ey" to f(Color(eye)),
)
val BROWN = finish(0xFF9A6440, 0xFF4E2C1A, 0xFFD2A57A, 0xFF4A4C56, 0xFF7FD3FF, 0xFF140A0C)
val PURPLE = finish(0xFF8A86E6, 0xFF3C3784, 0xFFCFC9F4, 0xFF4A4C56, 0xFFFF4DA6, 0xFF120A1C)
val YELLOW = finish(0xFFF6D35A, 0xFFC98E1A, 0xFFFFF1C8, 0xFF4A4C56, 0xFFB06BFF, 0xFF1A1006)
val RED = finish(0xFFD2553A, 0xFF7A2416, 0xFFEFC09A, 0xFF4A4C56, 0xFFFFD24A, 0xFF160806)
val CLOWN = finish(0xFFFBF3F2, 0xFFD9C3CB, 0xFFFFC2D2, 0xFF6A6C76, 0xFF5BE07A, 0xFF120A0E)
val GOLD = finish(0xFFF4D06A, 0xFF8E6A1C, 0xFFF0DDA6, 0xFF4A4C56, 0xFFFFFFFF, 0xFF0E0804)
val PUPPET = finish(0xFFFFFFFF, 0xFFCFC8D2, 0xFF1A1418, 0xFF2A2A30, 0xFFFFFFFF, 0xFF050305)
val RINGMASTER = finish(0xFFE04A55, 0xFF7A1426, 0xFFF2C14E, 0xFF4A4C56, 0xFFFFC94A, 0xFF140608)
val ENDO = finish(0xFFB4B8C2, 0xFF4D525C, 0xFF8C9099, 0xFF2E3138, 0xFFFF3B3B, 0xFF0A0B0E)
val WITHERED_BROWN = finish(0xFF6E5444, 0xFF2E2019, 0xFF8E7560, 0xFF5E606A, 0xFFFFFFFF, 0xFF080405)
val PUMPKIN = finish(0xFFF7A040, 0xFFB4501A, 0xFFFFD49A, 0xFF4A4C56, 0xFFFFB13B, 0xFF1A0A04) // the Halloween bear's (Halloween.kt)
val WITHERED_PURPLE =finish(0xFF5C5A86, 0xFF262448, 0xFF8A86B0, 0xFF5E606A, 0xFFFF3B3B, 0xFF060408)
