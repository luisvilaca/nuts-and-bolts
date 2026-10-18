package com.luisvilaca.nutsandbolts

import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

// Habit rules. No Android in here so the unit tests run on the JVM.

// stages: the name of each of the attribute's ten levels, in the robot's own terms
class Stat(val name: String, val gloss: String, val stages: List<String>)
class Weight(val name: String, val bolts: Int, val pts: Int)

val STATS = linkedMapOf(
    "battery" to Stat("Battery", "energy", listOf("AA Cell", "Nickel Pack", "Lithium Cell", "Li-ion Bank", "Solid-State", "Supercapacitor", "Graphene Cell", "Fusion Cell", "Zero-Point Cell", "Antimatter Cell")),
    "processor" to Stat("Processor", "mind", listOf("4-bit", "8-bit", "16-bit", "32-bit", "64-bit", "Multicore", "Neural Engine", "Quantum Core", "Photonic Mind", "Singularity")),
    "chassis" to Stat("Chassis", "body", listOf("Tin Can", "Aluminium", "Steel", "Titanium", "Carbon Fibre", "Tungsten", "Graphene", "Nanoweave", "Diamond Lattice", "Neutronium")),
    "core" to Stat("Core", "heart", listOf("Spark", "Dynamo", "Turbine", "Fission Pile", "Breeder", "Tokamak", "Fusion Heart", "Stellarator", "Star Core", "Supernova")),
)

// Ten levels per attribute: level n starts at 10 n (n - 1) points (0, 20, 60, 120 ... 900); points count on past the top.
val LEVEL_AT = List(10) { 10 * it * (it + 1) }
fun level(p: Int) = LEVEL_AT.indexOfLast { p >= it } + 1
fun levelShare(p: Int): Float = level(p).let { l -> if (l == LEVEL_AT.size) 1f else (p - LEVEL_AT[l - 1]).toFloat() / (LEVEL_AT[l] - LEVEL_AT[l - 1]) }
const val LEVEL_NUTS = 2
 // for each level an attribute reaches for the first time
val WEIGHTS = linkedMapOf(
    "light" to Weight("Light", 5, 1),
    "medium" to Weight("Medium", 10, 2),
    "heavy" to Weight("Heavy", 20, 4),
)

class Habit(
    var id: String,
    var name: String,
    var kind: String = "good", // good: build it; bad: break it
    var per: String = "day", // day or week
    var target: Int = 1,
    var stat: String = "battery",
    var weight: String = "medium",
    var start: LocalDate? = null,
    var end: LocalDate? = null,
    var days: Set<Int>? = null, // 0 is Monday; null is every day
)

// Each mark stores exactly what it changed (bolts, points, nuts, and l: new levels it reached), so undo is exact.
class Mark(val s: String, var b: Int = 0, var p: Int = 0, var n: Int = 0, var l: Int = 0)

// nib and rub (the eraser) are widths in mm as drawn on this screen; stab is the stabilizer in percent, 0 to 100 by tens.
// palm: palm rejection; once a pen has written, only the pen writes and a finger scrolls the page.
// b1, b2: what the pen's two side buttons do: "eraser" (pen and eraser in turn), "ink" (the next ink), "undo" or "none".
class Pen(var color: String = PEN_COLORS[0].second, var nib: Float = 0.6f, var stab: Int = 50, var rub: Float = 4f, var paper: Int = 0, var seen: Boolean = false, var palm: Boolean = true,
          var b1: String = "eraser", var b2: String = "ink")
class Remind(var on: Boolean = false, var time: String = "21:00")

class Data {
    var name = "Rivet"
    var bolts = 0
    var nuts = 0
    val stats = linkedMapOf("battery" to 30, "processor" to 30, "chassis" to 30, "core" to 30)
    val peak = STATS.keys.associateWith { level(30) }.toMutableMap() // the highest level each attribute has reached
    val owned = mutableListOf("head-box", "body-crate", "arms-clamp", "legs-stub", "base-bay")
    val equip = mutableMapOf<String, String?>("head" to "head-box", "body" to "body-crate", "arms" to "arms-clamp", "legs" to "legs-stub", "acc" to null, "base" to "base-bay")
    val habits = mutableListOf<Habit>()
    val marks = mutableMapOf<LocalDate, MutableMap<String, MutableList<Mark>>>()
    val diary = mutableSetOf<LocalDate>()
    val papers = PAPERS.toMutableList()
    var pen = Pen()
    var remind = Remind()
    var sound = true // the effects in Sfx
    var backedUp: LocalDate? = null
}

fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value - 1).toLong())
fun periodDates(h: Habit, d: LocalDate) = if (h.per == "week") (0..6L).map { weekStart(d).plusDays(it) } else listOf(d)
fun periodMarks(s: Data, h: Habit, d: LocalDate) = periodDates(h, d).flatMap { x -> s.marks[x]?.get(h.id).orEmpty().map { x to it } }
fun active(h: Habit, d: LocalDate) = (h.start == null || h.start!! <= d) && (h.end == null || d <= h.end!!) && (h.days == null || (d.dayOfWeek.value - 1) in h.days!!)
fun locked(s: Data, h: Habit, d: LocalDate) = periodMarks(s, h, d).count { it.second.s == "done" } >= h.target

fun streak(s: Data, h: Habit, d: LocalDate): Int {
    val step = if (h.per == "week") 7L else 1L
    var n = 0
    var x = d
    repeat(2000) {
        if (h.start != null && x < h.start) return n
        if (periodDates(h, x).any { active(h, it) }) { // a day off neither breaks nor extends the streak
            if (!locked(s, h, x)) return n
            n++
        }
        x = x.minusDays(step)
    }
    return n
}

data class Record(val slots: Int, val done: Int, val fail: Int, val best: Int, val now: Int)

// A habit's lifetime up to day t: slots it asked for, how they went, and its longest run of full periods.
// The period holding t is still open, so missing it does not end the run.
fun record(s: Data, h: Habit, t: LocalDate): Record {
    val first = h.start ?: s.marks.keys.filter { s.marks[it]!!.containsKey(h.id) }.minOrNull() ?: t
    val last = if (h.end != null && h.end!! < t) h.end!! else t
    val step = if (h.per == "week") 7L else 1L
    var slots = 0; var done = 0; var fail = 0; var best = 0; var run = 0
    var d = if (h.per == "week") weekStart(first) else first
    while (d <= last) {
        if (periodDates(h, d).any { active(h, it) }) {
            val ms = periodMarks(s, h, d)
            slots += h.target
            done += ms.count { it.second.s == "done" }
            fail += ms.count { it.second.s == "fail" }
            if (locked(s, h, d)) best = max(best, ++run)
            else if (t !in periodDates(h, d)) run = 0
        }
        d = d.plusDays(step)
    }
    return Record(slots, done, fail, best, streak(s, h, t))
}

// Fills the next slot of the habit's period.
fun mark(s: Data, h: Habit, d: LocalDate, kind: String): Mark? {
    if (periodMarks(s, h, d).size >= h.target) return null
    val w = WEIGHTS.getValue(h.weight)
    val m = Mark(kind)
    if (kind == "done") { m.b = w.bolts; m.p = w.pts }
    if (kind == "fail") { m.b = -min(w.bolts, s.bolts); m.p = -min(w.pts, s.stats.getValue(h.stat)) }
    s.marks.getOrPut(d) { mutableMapOf() }.getOrPut(h.id) { mutableListOf() }.add(m)
    // a nut for every finished week, or every third finished day in a row
    if (kind == "done" && locked(s, h, d) && (h.per == "week" || streak(s, h, d) % 3 == 0)) m.n = 1
    s.bolts += m.b; s.nuts += m.n; s.stats[h.stat] = s.stats.getValue(h.stat) + m.p
    // a level reached for the first time pays nuts; falling back and climbing again does not pay twice
    val l = level(s.stats.getValue(h.stat))
    if (l > s.peak.getValue(h.stat)) { m.l = l - s.peak.getValue(h.stat); s.peak[h.stat] = l; s.nuts += LEVEL_NUTS * m.l }
    return m
}

fun undo(s: Data, h: Habit, d: LocalDate): Mark? {
    val arr = s.marks[d]?.get(h.id)
    if (arr.isNullOrEmpty()) return null
    val m = arr.removeAt(arr.size - 1)
    var n = m.n
    if (!locked(s, h, d)) for ((_, x) in periodMarks(s, h, d)) { n += x.n; x.n = 0 }
    s.bolts = max(0, s.bolts - m.b)
    s.nuts = max(0, s.nuts - n - LEVEL_NUTS * m.l)
    s.stats[h.stat] = (s.stats.getValue(h.stat) - m.p).coerceAtLeast(0)
    s.peak[h.stat] = s.peak.getValue(h.stat) - m.l
    if (arr.isEmpty()) { s.marks[d]!!.remove(h.id); if (s.marks[d]!!.isEmpty()) s.marks.remove(d) }
    return m
}

data class Progress(val done: Int, val fail: Int, val slots: Int)

fun dayProgress(s: Data, d: LocalDate): Progress {
    var done = 0; var fail = 0; var slots = 0
    for (h in s.habits) {
        val ms = s.marks[d]?.get(h.id).orEmpty()
        done += ms.count { it.s == "done" }
        fail += ms.count { it.s == "fail" }
        slots += if (h.per == "day" && active(h, d)) h.target else ms.size
    }
    return Progress(done, fail, slots)
}

// Buying never goes below zero and never buys twice.
fun buy(s: Data, p: Part): Boolean {
    if (p.id in s.owned || s.bolts < p.bolts || s.nuts < p.nuts) return false
    s.bolts -= p.bolts; s.nuts -= p.nuts
    s.owned += p.id
    s.equip[p.slot] = p.id
    return true
}

fun mood(s: Data): Int { // 2 fully charged, 1 running steady, 0 low power
    val a = s.stats.values.sumOf { level(it) } / 4.0
    return if (a >= 4) 2 else if (a >= 2) 1 else 0
}
