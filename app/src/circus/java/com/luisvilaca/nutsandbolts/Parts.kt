package com.luisvilaca.nutsandbolts

import androidx.compose.ui.geometry.Offset

// The animatronic's parts, drawn for this flavour in the same 400x520 robot space as the workshop's (feet at y=468,
// the head turning on (200, 212), the arms on (120, 264) and its mirror). Each keeps the id and price of the workshop
// part it stands in for, so the starter kit, the achievements and saved states work unchanged. The classic suits are
// felt with a muzzle and a jaw; the withered ones are torn open to the endoskeleton, wires hanging.

private fun c(x: Number, y: Number, r: Number) = e(x, y, r, r)
private fun e(x: Number, y: Number, rx: Number, ry: Number): String {
    val a = x.toFloat(); val b = y.toFloat(); val w = rx.toFloat(); val h = ry.toFloat()
    return "M${a - w} ${b}a$w $h 0 1 0 ${2 * w} 0a$w $h 0 1 0 ${-2 * w} 0z"
}
private fun rr(x: Number, y: Number, w: Number, h: Number, r: Number): String {
    val a = x.toFloat(); val b = y.toFloat(); val ww = w.toFloat(); val hh = h.toFloat(); val k = minOf(r.toFloat(), ww / 2, hh / 2)
    return "M${a + k} ${b}h${ww - 2 * k}a$k $k 0 0 1 $k ${k}v${hh - 2 * k}a$k $k 0 0 1 ${-k} ${k}h${-(ww - 2 * k)}a$k $k 0 0 1 ${-k} ${-k}v${-(hh - 2 * k)}a$k $k 0 0 1 $k ${-k}z"
}
private val M = floatArrayOf(-1f, 0f, 0f, 1f, 400f, 0f) // mirrored across x = 200

private fun s(d: String, vararg roles: String, layer: String = "", pulse: Boolean = false) = Shape(roles.toList(), d, layer = layer, pulse = pulse)
// a shape and its mirror image
private fun two(d: String, vararg roles: String, layer: String = "") = listOf(Shape(roles.toList(), d, layer = layer), Shape(roles.toList(), d, M, layer))
// the left arm as drawn, and the right one mirrored
private fun arm(d: String, vararg roles: String) = listOf(Shape(roles.toList(), d, layer = "armL"), Shape(roles.toList(), d, M, "armR"))
private fun armL(d: String, vararg roles: String) = Shape(roles.toList(), d, layer = "armL")
private fun armR(d: String, vararg roles: String) = Shape(roles.toList(), d, M, "armR") // drawn as a left arm, shown on the right
private fun eyes(lx: Number, y: Number, r: Number, rx: Number = 400 - lx.toFloat()) = listOf(s(c(lx, y, r), "ey", layer = "eyes"), s(c(rx, y, r), "ey", layer = "eyes"))

private val HEAD = floatArrayOf(96f, 50f, 208f, 172f)
private val BODY = floatArrayOf(106f, 222f, 188f, 158f)
private val ARMS = floatArrayOf(70f, 240f, 260f, 136f)
private val LEGS = floatArrayOf(100f, 350f, 200f, 126f)

// the classic head: brow and muzzle, a hinged jaw with teeth, a black nose and two eyes in deep sockets
private fun face(eyeY: Float = 142f): List<Shape> = listOf(
    s(rr(124, 92, 152, 98, 48), "sh"),
    s("M140 108q22-14 50-14", "hl"),
    s(rr(150, 182, 100, 30, 14), "sh"),
    s("M156 184h88", "gl"),
    s("M162 186v8M174 186v8M186 186v8M198 186v8M210 186v8M222 186v8M234 186v8", "teeth"),
    s(e(200, 166, 44, 24), "sh2"),
    s(e(200, 156, 10, 7), "gl"),
    s(e(170, eyeY, 16, 14), "gl"), s(e(230, eyeY, 16, 14), "gl"),
    s("M152 120q18-9 34 0M214 120q18-9 34 0", "ln"),
    // eyes glowing out of the dark: a soft light in each socket round a pinprick
    s(c(172, eyeY, 13), "glowL", layer = "eyes"), s(c(228, eyeY, 13), "glowR", layer = "eyes"),
) + eyes(172, eyeY, 4.5f)

// a withered face: the classic one with the jaw hanging loose and a tear down one cheek to the endoskeleton
private fun witheredFace(): List<Shape> = listOf(
    s(rr(124, 92, 152, 98, 48), "sh"),
    s(rr(150, 190, 100, 26, 12), "zn"),
    s("M160 196v10M176 196v10M192 196v10M208 196v10M224 196v10M240 196v10", "teeth"),
    s(e(200, 166, 44, 24), "sh2"),
    s(e(200, 156, 10, 7), "gl"),
    s("M226 122l18-6 14 12-4 20 8 18-14 18-26 2-8-22z", "gl"), // the tear
    s("M234 150c4 18-6 30-2 48M244 148c8 16 2 34 8 52", "wireR"),
    s("M238 152c-2 20 6 32 0 50", "wireB"),
    s(e(170, 142, 16, 14), "gl"), s(e(236, 142, 12, 11), "zn"), s(e(236, 142, 8, 7), "gl"),
    s("M152 120q18-6 34 4", "ln"),
    s(c(172, 143, 3.5f), "ey", layer = "eyes"), s(c(236, 143, 3.5f), "ey", layer = "eyes"),
)

val PARTS = listOf(
    // heads
    Part("head-box", "head", "Bear Head", "FC-H01", box = HEAD, pivots = mapOf("eyes" to Offset(200f, 142f)), shapes =
        two(c(140, 96, 22), "sh") + two(c(140, 96, 11), "sh2") + face()),
    Part("head-dome", "head", "Bunny Head", "FC-H02", pal = PURPLE, bolts = 120, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 142f)), shapes =
        two("M150 104C136 62 136 26 156 22c18-3 24 40 22 82z", "sh") + two("M156 96c-10-30-10-56 0-60c9-2 13 30 12 60z", "sh2") +
            face() + listOf(s(e(150, 172, 10, 6), "blush"), s(e(250, 172, 10, 6), "blush"))),
    Part("head-hex", "head", "Withered Bunny", "FC-H04", pal = WITHERED_PURPLE, bolts = 150, nuts = 3, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 150f)), shapes = listOf(
        s("M150 104C136 62 136 26 156 22c18-3 24 40 22 82z", "sh"),
        s("M156 96c-10-30-10-56 0-60c9-2 13 30 12 60z", "sh2"),
        s("M226 104c-2-14 2-30 8-34l6 8 4-10 6 6c2 10 0 22-4 30z", "sh"), // the torn ear
        s(rr(124, 92, 152, 98, 48), "sh"),
        // no face: the endoskeleton shows through, two pinpoints in the dark
        s("M140 128c10-14 34-20 60-20s50 6 60 20c6 22 4 46-8 62H148c-12-16-14-40-8-62z", "gl"),
        s(rr(166, 136, 68, 22, 10), "zn"),
        s("M160 176h80M172 170v12M188 170v12M204 170v12M220 170v12", "zn"),
        s("M150 130c4 20-4 38 2 60M252 128c-6 22 4 40-2 62", "wireR"),
        s("M162 126c6 16 0 30 4 44", "wireB"),
        s(rr(150, 190, 100, 24, 10), "zn"),
        s("M160 196v10M176 196v10M192 196v10M208 196v10M224 196v10M240 196v10", "teeth"),
        s(c(182, 147, 4), "ey", layer = "eyes"), s(c(218, 147, 4), "ey", layer = "eyes"),
    )),
    Part("head-binoc", "head", "Chicken Head", "FC-H03", pal = YELLOW, bolts = 200, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 140f)), shapes = listOf(
        s("M184 96l-8-26 14 14 8-22 8 22 14-14-8 26z", "sh"),
        s(rr(124, 92, 152, 98, 48), "sh"),
        s("M140 108q22-14 50-14", "hl"),
        s(rr(150, 182, 100, 30, 14), "sh"),
        s("M162 186v8M174 186v8M186 186v8M198 186v8M210 186v8M222 186v8M234 186v8", "teeth"),
        s(e(170, 138, 16, 15), "eyw"), s(e(230, 138, 16, 15), "eyw"),
        s("M154 122l30 6M246 122l-30 6", "ln"),
        s("M162 160c10-12 66-12 76 0c-10 14-66 14-76 0z", "beak"),
        s("M168 168c10 12 54 12 64 0c-8 18-56 18-64 0z", "beak2"),
    ) + eyes(172, 140, 7)),
    Part("head-weld", "head", "Withered Bear", "FC-H09", pal = WITHERED_BROWN, bolts = 230, nuts = 3, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 142f)), shapes = listOf(
        s(c(140, 96, 22), "sh"), s(c(140, 96, 11), "sh2"),
        s("M248 104c-6-12-2-26 10-30l10 4 6-6c8 8 8 22 0 30z", "sh"), s(c(262, 92, 6), "gl"), // a ragged ear
    ) + witheredFace()),
    Part("head-cyclops", "head", "Pirate Fox", "FC-H05", pal = RED, bolts = 260, nuts = 2, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 140f)), shapes =
        two("M134 110l-4-58 40 40z", "sh") + two("M140 100l-2-32 22 22z", "sh2") + listOf(
            s(rr(124, 92, 152, 92, 46), "sh"),
            s("M140 108q22-14 50-14", "hl"),
            // the long snout, open on its fangs
            s("M156 156c14-10 74-10 88 0l-6 44c-24 10-52 10-76 0z", "sh2"),
            s("M166 178h68l-6 18c-18 6-38 6-56 0z", "gl"),
            s("M170 178l5 9 5-9M186 178l5 9 5-9M204 178l5 9 5-9M220 178l5 9 5-9", "teeth"),
            s(e(200, 158, 12, 8), "gl"),
            s(e(170, 138, 15, 13), "gl"),
            s(c(172, 139, 6), "ey", layer = "eyes"),
            s(e(230, 138, 19, 16), "black"), s("M211 130L132 104M249 132l26-18", "strap"),
            s("M152 116l34 10M248 116l-34 10", "ln"),
        )),
    Part("head-monitor", "head", "Clown Doll", "FC-H06", pal = CLOWN, bolts = 340, nuts = 5, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 144f)), shapes =
        two(c(122, 128, 22), "cr") + two(c(116, 152, 16), "cr") + listOf(
            s(rr(128, 92, 144, 118, 60), "sh"),
            s("M132 128c4-30 34-40 68-40s64 10 68 40c-16-12-38-16-68-16s-52 4-68 16z", "cr"),
            s("M136 176h128", "ln"), // the faceplate's seam
            s(e(170, 144, 14, 15), "eyw"), s(e(230, 144, 14, 15), "eyw"),
            s("M156 132l-6-6M164 128l-2-8M244 132l6-6M236 128l2-8", "ln"),
            s(c(156, 168, 10), "blush"), s(c(244, 168, 10), "blush"),
            s("M186 184q14 10 28 0", "smile"),
            s(e(200, 162, 5, 4), "cr"),
        ) + eyes(172, 145, 7)),
    Part("head-bowl", "head", "Golden Bear", "FC-H07", pal = GOLD, bolts = 300, nuts = 6, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 142f)), shapes =
        two(c(140, 96, 22), "sh") + two(c(140, 96, 11), "sh2") + listOf(
            s(rr(124, 92, 152, 98, 48), "sh"),
            s("M140 108q22-14 50-14", "hl"),
            s(rr(150, 184, 100, 28, 14), "sh"),
            s("M156 186h88", "gl"),
            s(e(200, 166, 44, 24), "sh2"),
            s(e(200, 156, 10, 7), "gl"),
            s(e(170, 142, 17, 16), "gl"), s(e(230, 142, 17, 16), "gl"), // empty sockets
            s("M152 116q18-6 34 4M214 120q18-10 34-4", "ln"),
            s(c(172, 144, 2.5f), "ey", layer = "eyes"), s(c(228, 144, 2.5f), "ey", layer = "eyes"),
        )),
    Part("head-saucer", "head", "Puppet Mask", "FC-H08", pal = PUPPET, bolts = 420, nuts = 8, box = HEAD, pivots = mapOf("eyes" to Offset(200f, 138f)), shapes = listOf(
        s(rr(126, 80, 148, 136, 68), "black"),
        s("M138 140c0-36 26-56 62-56s62 20 62 56c0 40-26 68-62 68s-62-28-62-68z", "sh"),
        s(e(172, 136, 13, 16), "gl"), s(e(228, 136, 13, 16), "gl"),
        s("M168 152c-4 14 2 28-2 44M232 152c4 14-2 28 2 44", "tear"),
        s(c(160, 168, 10), "blush"), s(c(240, 168, 10), "blush"),
        s("M180 182c8 10 32 10 40 0c-4 14-36 14-40 0z", "cr"),
        s(c(172, 138, 3), "ey", layer = "eyes"), s(c(228, 138, 3), "ey", layer = "eyes"),
    )),

    // bodies
    Part("body-crate", "body", "Felt Suit", "FC-B01", box = BODY, shapes = listOf(
        s(rr(124, 228, 152, 148, 60), "sh"),
        s(e(200, 318, 48, 44), "sh2"),
        s("M146 250q20-14 46-14", "hl"),
        s("M168 360v8M232 360v8", "ln"),
    )),
    Part("body-capsule", "body", "Party Bib", "FC-B02", pal = YELLOW, bolts = 140, box = BODY, shapes = listOf(
        s(rr(124, 228, 152, 148, 60), "sh"),
        s("M150 236h100v84c-8 8-16 8-25 0c-8 8-17 8-25 0c-8 8-17 8-25 0c-8 8-17 8-25 0z", "white"),
        s("M150 236h100v84c-8 8-16 8-25 0c-8 8-17 8-25 0c-8 8-17 8-25 0c-8 8-17 8-25 0z", "pinkline"),
        s("M182 296h36l-4 14h-28z", "cr"), s("M180 296c0-14 40-14 40 0z", "blush"), s(c(200, 280, 3), "br"), // a cupcake on the bib
    )),
    Part("body-reactor", "body", "Withered Torso", "FC-B03", pal = WITHERED_BROWN, bolts = 180, nuts = 4, box = BODY, shapes = listOf(
        s(rr(124, 228, 152, 148, 60), "sh"),
        s(e(200, 318, 48, 44), "sh2"),
        s("M168 252l26-6 22 10 6 30-14 26-30 4-14-26z", "gl"), // torn open over the chest
        s("M182 262h36M178 276h44M180 290h40M186 304h28", "zn"),
        s("M200 256v54", "rod"),
        s("M174 300c-6 16 4 30-2 44M222 296c8 18-2 34 6 50", "wireR"),
        s("M196 310c2 14-6 26 0 40", "wireB"),
        s("M244 340l14 8-4 14-12-4z", "gl"),
    )),
    Part("body-gumball", "body", "Clown Dress", "FC-B04", pal = CLOWN, bolts = 380, nuts = 8, box = BODY, shapes = listOf(
        s(rr(136, 228, 128, 80, 40), "cr"),
        s("M138 296h124l30 76c-60 14-124 14-184 0z", "cr"),
        s("M138 296h124l6 14H132z", "sh"),
        s("M150 330l-10 38M178 334l-4 40M222 334l4 40M250 330l10 38", "ln"),
        s(c(200, 256, 7), "br"), s(c(200, 280, 7), "br"),
        s("M146 246q18-12 40-12", "hl"),
    )),
    Part("body-rocket", "body", "Ringmaster Coat", "FC-B05", pal = RINGMASTER, bolts = 400, nuts = 9, box = floatArrayOf(96f, 222f, 208f, 162f), shapes = listOf(
        s("M130 230h140l18 140-40 8-48-70-48 70-40-8z", "sh"),
        s("M178 230h44l-22 76z", "white"),
        s("M200 232l-22 0 22 76 22-76z", "ln"),
        s("M150 236l28 0-14 40zM250 236l-28 0 14 40z", "sh2"),
        s(c(176, 300, 6), "br"), s(c(224, 300, 6), "br"), s(c(170, 328, 6), "br"), s(c(230, 328, 6), "br"),
        s("M130 230h140", "trim"),
    )),
    Part("body-tank", "body", "Endoskeleton", "FC-B06", pal = ENDO, bolts = 450, nuts = 10, box = BODY, shapes = listOf(
        s(rr(150, 236, 100, 70, 22), "sh"),
        s(rr(164, 248, 72, 44, 14), "gl"),
        s("M174 260h52M174 270h52M174 280h52", "zn"),
        s("M200 306v56", "rod"),
        s("M160 318h80M168 334h64M176 350h48", "rod"),
        s(rr(168, 356, 64, 20, 8), "sh"),
        s("M156 300c-10 20 0 40-4 60M246 300c8 20 -2 40 4 60", "wireR"),
        s(c(200, 270, 6), "ey", pulse = true),
    )),
    Part("body-tokamak", "body", "Golden Suit", "FC-B07", pal = GOLD, bolts = 520, nuts = 12, box = BODY, shapes = listOf(
        s(rr(124, 228, 152, 148, 60), "sh"),
        s(e(200, 318, 48, 44), "sh2"),
        s("M146 250q20-14 46-14", "hl"),
        s(c(200, 262, 6), "black"), s(c(200, 286, 6), "black"),
        s("M140 340l12 16M262 336l-10 18", "ln"),
    )),

    // arms (drawn left, mirrored right)
    Part("arms-clamp", "arms", "Felt Arms", "FC-A01", box = ARMS, shapes =
        arm(c(120, 264, 14), "zn") + arm(rr(100, 252, 30, 58, 15), "sh") + arm(rr(98, 304, 30, 42, 15), "sh") + arm(c(112, 350, 16), "sh2")),
    Part("arms-flex", "arms", "Withered Arms", "FC-A02", pal = WITHERED_BROWN, bolts = 80, box = ARMS, shapes =
        arm(c(120, 264, 14), "zn") + arm(rr(100, 252, 30, 52, 15), "sh") + arm("M115 300v40", "rod") + arm(c(115, 304, 6), "zn") +
            arm("M106 300c-6 14 2 24-4 36", "wireR") + arm("M115 340l-12 18M115 340l0 20M115 340l12 18", "claw")),
    Part("arms-mitt", "arms", "Paws", "FC-A03", pal = PURPLE, bolts = 110, box = ARMS, shapes =
        arm(c(120, 264, 14), "zn") + arm(rr(100, 252, 30, 58, 15), "sh") + arm(rr(98, 304, 30, 40, 15), "sh") + arm(c(112, 350, 20), "sh") +
            arm(c(112, 354, 8), "sh2") + arm(c(100, 340, 4), "sh2") + arm(c(112, 336, 4), "sh2") + arm(c(124, 340, 4), "sh2")),
    Part("arms-wrench", "arms", "Pirate Hook", "FC-A09", pal = RED, bolts = 130, box = ARMS, shapes = listOf(
        armL(c(120, 264, 14), "zn"), armL(rr(100, 252, 30, 58, 15), "sh"), armL(rr(98, 304, 30, 42, 15), "sh"), armL(c(112, 350, 16), "sh2"),
        armR(c(120, 264, 14), "zn"), armR(rr(100, 252, 30, 58, 15), "sh"), armR("M104 320c-8 10 0 20 6 24h12v-24z", "sh"),
        armR(rr(102, 338, 22, 10, 4), "zn"), armR("M113 348v14c0 14-20 16-22 2", "hook"),
    )),
    Part("arms-fin", "arms", "Endo Arms", "FC-A04", pal = ENDO, bolts = 100, nuts = 2, box = ARMS, shapes =
        arm(c(120, 264, 14), "zn") + arm("M118 268L110 306L112 340", "rod") + arm(c(110, 306, 7), "zn") + arm(rr(100, 336, 24, 14, 6), "sh") +
            arm("M104 350l-6 14M112 350v16M120 350l6 14", "claw") + arm("M124 276c6 12-2 22 2 32", "wireR")),
    Part("arms-magnet", "arms", "Microphone", "FC-A05", pal = BROWN, bolts = 220, nuts = 4, box = ARMS, shapes = listOf(
        armL(c(120, 264, 14), "zn"), armL(rr(100, 252, 30, 58, 15), "sh"), armL(rr(98, 304, 30, 42, 15), "sh"), armL(c(112, 350, 16), "sh2"),
        armR(c(120, 264, 14), "zn"), armR(rr(100, 252, 30, 58, 15), "sh"), armR(rr(98, 304, 30, 42, 15), "sh"),
        armR(rr(106, 326, 12, 40, 5), "black"), armR(c(112, 324, 12), "zn"), armR("M104 318h16M102 324h20M104 330h16", "ln"), armR(c(112, 350, 16), "sh2"),
    )),
    Part("arms-glove", "arms", "White Gloves", "FC-A06", pal = RINGMASTER, bolts = 240, nuts = 4, box = ARMS, shapes =
        arm(c(120, 264, 14), "zn") + arm(rr(100, 252, 30, 62, 15), "sh") + arm(rr(96, 312, 34, 10, 4), "br") + arm(rr(98, 318, 30, 26, 12), "sh") +
            arm(c(112, 352, 19), "white") + arm("M100 346h24M100 354h24", "ln")),
    Part("arms-tentacle", "arms", "Puppet Strings", "FC-A07", pal = PUPPET, bolts = 280, nuts = 5, box = floatArrayOf(50f, 240f, 300f, 140f), shapes =
        arm("M120 262C104 290 92 320 86 368", "limb") + arm("M120 262C104 290 92 320 86 368", "stripes") + arm("M86 368l-8 12M86 368l-2 14M86 368l6 12", "fingers")),
    Part("arms-welder", "arms", "Claw Hands", "FC-A10", pal = CLOWN, bolts = 320, nuts = 6, box = ARMS, shapes = listOf(
        armL(c(120, 264, 14), "zn"), armL(rr(100, 252, 30, 58, 15), "sh"), armL(rr(98, 304, 30, 42, 15), "cr"), armL(c(112, 350, 16), "white"),
        armR(c(120, 264, 14), "zn"), armR(rr(100, 252, 30, 58, 15), "sh"), armR(rr(98, 304, 30, 34, 15), "cr"),
        armR(c(112, 340, 9), "zn"), armR("M112 344l-16 22M112 344v26M112 344l16 22", "claw"),
    )),
    Part("arms-wing", "arms", "Juggling Pins", "FC-A08", pal = BROWN, bolts = 500, nuts = 12, box = floatArrayOf(40f, 200f, 320f, 180f), shapes =
        arm(c(120, 264, 14), "zn") + arm(rr(100, 252, 30, 58, 15), "sh") + arm(rr(98, 304, 30, 42, 15), "sh") + arm(c(112, 350, 16), "sh2") +
            arm("M104 352c-10-30-10-60 0-90c4-4 8-4 10 0c8 30 8 60-2 90z", "white") + arm("M100 296h16M98 306h18", "pinstripe") + arm(c(108, 254, 6), "cr")),

    // legs
    Part("legs-stub", "legs", "Felt Legs", "FC-L01", box = LEGS, shapes =
        two(rr(150, 356, 40, 62, 18), "sh") + two(rr(152, 410, 36, 40, 16), "sh") + two(e(166, 460, 30, 13), "sh2")),
    Part("legs-spring", "legs", "Endo Legs", "FC-L02", pal = ENDO, bolts = 90, box = LEGS, shapes =
        two("M170 360L166 410L170 448", "rod") + two(c(166, 410, 8), "zn") + two(rr(146, 448, 46, 16, 6), "sh") + two("M180 372c6 14-2 26 2 38", "wireR")),
    Part("legs-tread", "legs", "Withered Legs", "FC-L03", pal = WITHERED_BROWN, bolts = 160, box = LEGS, shapes =
        listOf(s(rr(150, 356, 40, 62, 18), "sh"), s(rr(152, 410, 36, 40, 16), "sh"), s(e(166, 460, 30, 13), "sh2"),
            s(rr(210, 356, 40, 46, 18), "sh"), s("M230 400L232 450", "rod"), s(c(230, 402, 7), "zn"), s(rr(208, 448, 46, 16, 6), "zn"),
            s("M216 398c-4 14 4 22 0 34", "wireR"), s("M244 396c4 12-2 20 2 30", "wireB"), s("M160 380l12 6-2 12-10-4z", "gl"))),
    Part("legs-piston", "legs", "Striped Stockings", "FC-L08", pal = CLOWN, bolts = 190, nuts = 2, box = LEGS, shapes =
        two(rr(152, 356, 36, 92, 16), "white") + two("M152 372h36M152 390h36M152 408h36M152 426h36", "stocking") + two(e(166, 460, 30, 13), "cr") +
            two(c(156, 456, 5), "br")),
    Part("legs-hover", "legs", "Circus Ball", "FC-L04", pal = RED, bolts = 220, nuts = 5, box = LEGS, shapes =
        two(rr(156, 356, 32, 34, 14), "sh") + listOf(
            s(c(200, 426, 44), "cr"),
            s("M158 412c28 10 56 10 84 0M158 440c28 10 56 10 84 0", "white2"),
            s("M200 410l4 9 10 1-8 6 3 10-9-5-9 5 3-10-8-6 10-1z", "br"),
            s("M172 396q14-8 30-8", "hl"),
        )),
    Part("legs-wheel", "legs", "Unicycle", "FC-L05", pal = RED, bolts = 260, nuts = 5, box = LEGS, shapes = listOf(
        s(rr(168, 354, 64, 12, 6), "black"),
        s("M200 366v58", "rod"),
        s(c(200, 426, 40), "tire"),
        s("M200 390v72M164 426h72M175 401l50 50M225 401l-50 50", "spoke"),
        s(c(200, 426, 6), "br"),
    ) + two(rr(162, 362, 24, 40, 10), "sh") + two(e(176, 410, 14, 8), "sh2")),
    Part("legs-spider", "legs", "Stilts", "FC-L06", pal = RINGMASTER, bolts = 340, nuts = 7, box = floatArrayOf(76f, 350f, 248f, 126f), shapes =
        two(rr(150, 356, 40, 40, 16), "sh") + two("M170 392L162 466", "stilt") + two("M164 410h-14M164 410h14", "rod") + two("M160 466h10", "rod")),
    Part("legs-boots", "legs", "Clown Shoes", "FC-L07", pal = CLOWN, bolts = 420, nuts = 9, box = floatArrayOf(84f, 350f, 232f, 126f), shapes =
        two(rr(152, 356, 36, 82, 16), "sh") + two("M190 448c0-10-10-16-24-16h-40c-26 0-36 16-36 26s10 12 22 12h70c8 0 8-10 8-22z", "cr") +
            two(c(170, 440, 6), "white")),

    // accessories
    Part("acc-antenna", "acc", "Party Hat", "FC-X01", bolts = 40, box = floatArrayOf(160f, 18f, 90f, 90f), at = "head-front", shapes = listOf(
        s("M182 92l26-64 26 64z", "hatcone"),
        s("M190 74h36M198 54h20", "hatstripe"),
        s(c(208, 26, 8), "pom"),
    )),
    Part("acc-scarf", "acc", "Ruffle Collar", "FC-X02", bolts = 50, box = floatArrayOf(130f, 200f, 140f, 50f), at = "front", shapes = listOf(
        s("M138 222c4-12 16-12 20 0c4-12 16-12 20 0c4-12 16-12 22 0c4-12 16-12 20 0c4-12 16-12 20 0c4-12 16-12 22 0c-10 12-26 12-34 4c-8 8-24 8-30 0c-8 8-24 8-32 0c-8 8-24 8-30 0c-8 8-24 8-30 0z", "white"),
        s("M138 222c4-12 16-12 20 0c4-12 16-12 20 0c4-12 16-12 22 0c4-12 16-12 20 0c4-12 16-12 20 0c4-12 16-12 22 0", "pinkline"),
    )),
    Part("acc-dosimeter", "acc", "Flashlight", "FC-X13", pal = ENDO, bolts = 90, box = floatArrayOf(222f, 286f, 80f, 60f), at = "front", shapes = listOf(
        s("M262 310l60-26v56z", "beam"),
        s(rr(226, 304, 32, 14, 5), "sh"),
        s(rr(252, 300, 12, 22, 4), "zn"),
        s(c(264, 311, 5), "ey", pulse = true),
    )),
    Part("acc-hardhat", "acc", "Top Hat", "FC-X14", bolts = 120, box = floatArrayOf(150f, 30f, 100f, 72f), at = "head-front", shapes = listOf(
        s(rr(178, 40, 44, 46, 4), "black"),
        s("M178 74h44", "band"),
        s(rr(164, 84, 72, 8, 4), "black"),
        s("M184 46v30", "hl"),
    )),
    Part("acc-sprout", "acc", "Ears Bow", "FC-X03", bolts = 60, box = floatArrayOf(150f, 40f, 100f, 60f), at = "head-front", shapes = listOf(
        s("M200 76l-40-24c-6 14-6 34 0 46zM200 76l40-24c6 14 6 34 0 46z", "bow"),
        s(c(200, 76, 9), "bow2"),
    )),
    Part("acc-bow", "acc", "Bow Tie", "FC-X06", bolts = 70, box = floatArrayOf(160f, 200f, 80f, 50f), at = "front", shapes = listOf(
        s("M200 226l-28-14v28zM200 226l28-14v28z", "black"),
        s(c(200, 226, 6), "black"),
    )),
    Part("acc-pack", "acc", "Balloon", "FC-X04", pal = RED, bolts = 150, nuts = 2, box = floatArrayOf(250f, 60f, 110f, 220f), at = "body-back", shapes = listOf(
        s("M282 270c10-40-8-80 14-130", "string"),
        s("M298 60c-28 0-44 24-44 48s20 42 44 44c24-2 44-20 44-44s-16-48-44-48z", "cr"),
        s("M292 152l6 8 6-8z", "cr"),
        s("M276 84q10-12 22-12", "hl"),
    )),
    Part("acc-prop", "acc", "Pinwheel Cap", "FC-X07", pal = CLOWN, bolts = 180, nuts = 3, box = floatArrayOf(140f, 30f, 120f, 70f), at = "head-front", pivots = mapOf("prop" to Offset(200f, 54f)), shapes = listOf(
        s("M150 96c0-28 22-40 50-40s50 12 50 40z", "beanie"),
        s("M150 96c0-28 22-40 50-40s50 12 50 40", "beanieline"),
        s("M200 92V56", "rod"),
        s(e(174, 54, 26, 7), "cr", layer = "prop"),
        s(e(226, 54, 26, 7), "blue", layer = "prop"),
        s(c(200, 54, 6), "br"),
    )),
    Part("acc-cape", "acc", "Circus Cape", "FC-X08", bolts = 260, nuts = 5, box = floatArrayOf(60f, 226f, 280f, 170f), at = "body-back", shapes = listOf(
        s("M152 238h96l84 124q-132 28-264 0z", "cape"),
        s("M120 330l4 9 10 1-8 6 3 10-9-5-9 5 3-10-8-6 10-1zM270 310l4 9 10 1-8 6 3 10-9-5-9 5 3-10-8-6 10-1zM200 340l4 9 10 1-8 6 3 10-9-5-9 5 3-10-8-6 10-1z", "br"),
    )),
    Part("acc-hat", "acc", "Eyepatch", "FC-X09", bolts = 300, nuts = 6, box = floatArrayOf(120f, 100f, 170f, 70f), at = "head-front", shapes = listOf(
        s("M211 130L126 104M249 132l30-18", "strap"),
        s(e(230, 140, 20, 17), "black"),
        s("M218 134l24 12M242 134l-24 12", "ln"),
    )),
    Part("acc-halo", "acc", "Halo", "FC-X05", nuts = 6, box = floatArrayOf(140f, 36f, 120f, 54f), at = "head-front", shapes = listOf(
        s(e(200, 62, 48, 11), "halo"),
    )),
    Part("acc-crown", "acc", "Ringmaster Crown", "FC-X10", nuts = 12, box = floatArrayOf(146f, 30f, 108f, 70f), at = "head-front", shapes = listOf(
        s("M162 94l-8-40 24 20 22-30 22 30 24-20-8 40z", "br"),
        s(c(200, 78, 6), "cr"), s(c(172, 84, 4), "blue"), s(c(228, 84, 4), "blue"),
        s("M162 94h76", "trim"),
    )),
    Part("acc-moon", "acc", "Cupcake Pal", "FC-X11", nuts = 15, box = floatArrayOf(80f, 80f, 240f, 140f), at = "front", shapes = listOf(
        Shape(listOf("orbit"), e(200, 150, 112, 34), floatArrayOf(0.97f, -0.24f, 0.24f, 0.97f, -30.35f, 52.84f)),
        Shape(listOf("cake"), "M-11 0h22l-3 12h-16z", floatArrayOf(0.97f, -0.24f, 0.24f, 0.97f, -30.35f, 52.84f), layer = "moon"),
        Shape(listOf("frost"), "M-13 1c0-12 26-12 26 0z", floatArrayOf(0.97f, -0.24f, 0.24f, 0.97f, -30.35f, 52.84f), layer = "moon"),
        Shape(listOf("candle"), "M0-8v-8", floatArrayOf(0.97f, -0.24f, 0.24f, 0.97f, -30.35f, 52.84f), layer = "moon"),
        Shape(listOf("flame"), c(0, -19, 2.5f), floatArrayOf(0.97f, -0.24f, 0.24f, 0.97f, -30.35f, 52.84f), layer = "moon"),
    )),
    Part("acc-drone", "acc", "Lucky Balloon", "FC-X12", pal = CLOWN, nuts = 20, box = floatArrayOf(292f, 90f, 80f, 140f), at = "front", pivots = mapOf("hover" to Offset(332f, 145f)), shapes = listOf(
        s("M332 162c-6 20 8 40-4 64", "string", layer = "hover"),
        s("M332 100c-18 0-28 14-28 30s14 30 28 32c14-2 28-16 28-32s-10-30-28-30z", "blue", layer = "hover"),
        s(c(322, 126, 3), "black", layer = "hover"), s(c(342, 126, 3), "black", layer = "hover"),
        s("M320 140q12 10 24 0", "smile", layer = "hover"),
        s("M316 112q8-8 16-8", "hl", layer = "hover"),
    )),

    // the rooms (drawn in Art.kt)
    Part("base-bay", "base", "Center Ring", "FC-Z01", tier = 1),
    Part("base-shop", "base", "Parts & Service", "FC-Z02", nuts = 4, tier = 2),
    Part("base-dome", "base", "Pirate Cove", "FC-Z03", nuts = 10, tier = 3),
    Part("base-deck", "base", "Show Stage", "FC-Z04", nuts = 25, tier = 4),
    Part("base-hall", "base", "Circus Gallery", "FC-Z05", nuts = 40, tier = 5),
)

// The ring box the animatronic stands on: a striped drum, its rim bulbs chasing round; the rest of each room is
// drawn in Art.kt.
val SCENE = listOf(
    Shape(listOf("plat-side"), e(500, 780, 250, 52)),
    // cream stripes down the drum's side, each running from the top's rim to the drum's foot
    Shape(listOf("plat-stripe"), (-4..4).joinToString("") { i ->
        val x = 500 + i * 52; val k = kotlin.math.sqrt(1 - ((x - 500) / 250f).let { it * it }) * 52
        "M$x ${760 + k + 2}V${780 + k - 2}"
    }),
    Shape(listOf("plat-top"), e(500, 760, 250, 52)),
    Shape(listOf("plat-ring"), e(500, 760, 222, 44)),
)
