package com.luisvilaca.nutsandbolts

import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

// Habit rules. No Android in here so the unit tests run on the JVM.

class Stat(val name: String, val gloss: String)
class Weight(val name: String, val bolts: Int, val pts: Int)

val STATS = linkedMapOf(
    "battery" to Stat("Battery", "energy"),
    "processor" to Stat("Processor", "mind"),
    "chassis" to Stat("Chassis", "body"),
    "core" to Stat("Core", "heart"),
)
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

// Each mark stores exactly what it changed (bolts, points, nuts), so undo is exact.
class Mark(val s: String, var b: Int = 0, var p: Int = 0, var n: Int = 0)

// nib and rub (the eraser) are widths in mm as drawn on this screen; stab is the stabilizer in percent, 0 to 100 by tens.
class Pen(var color: String = PEN_COLORS[0].second, var nib: Float = 0.6f, var stab: Int = 50, var rub: Float = 4f, var paper: Int = 0, var seen: Boolean = false)
class Remind(var on: Boolean = false, var time: String = "21:00")

class Data {
    var name = "Rivet"
    var bolts = 0
    var nuts = 0
    val stats = linkedMapOf("battery" to 30, "processor" to 30, "chassis" to 30, "core" to 30)
    val owned = mutableListOf("head-box", "body-crate", "arms-clamp", "legs-stub", "base-bay")
    val equip = mutableMapOf<String, String?>("head" to "head-box", "body" to "body-crate", "arms" to "arms-clamp", "legs" to "legs-stub", "acc" to null, "base" to "base-bay")
    val habits = mutableListOf<Habit>()
    val marks = mutableMapOf<LocalDate, MutableMap<String, MutableList<Mark>>>()
    val diary = mutableSetOf<LocalDate>()
    val papers = PAPERS.toMutableList()
    var pen = Pen()
    var remind = Remind()
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
    if (kind == "done") { m.b = w.bolts; m.p = min(w.pts, 100 - s.stats.getValue(h.stat)) }
    if (kind == "fail") { m.b = -min(w.bolts, s.bolts); m.p = -min(w.pts, s.stats.getValue(h.stat)) }
    s.marks.getOrPut(d) { mutableMapOf() }.getOrPut(h.id) { mutableListOf() }.add(m)
    // a nut for every finished week, or every third finished day in a row
    if (kind == "done" && locked(s, h, d) && (h.per == "week" || streak(s, h, d) % 3 == 0)) m.n = 1
    s.bolts += m.b; s.nuts += m.n; s.stats[h.stat] = s.stats.getValue(h.stat) + m.p
    return m
}

fun undo(s: Data, h: Habit, d: LocalDate): Mark? {
    val arr = s.marks[d]?.get(h.id)
    if (arr.isNullOrEmpty()) return null
    val m = arr.removeAt(arr.size - 1)
    var n = m.n
    if (!locked(s, h, d)) for ((_, x) in periodMarks(s, h, d)) { n += x.n; x.n = 0 }
    s.bolts = max(0, s.bolts - m.b)
    s.nuts = max(0, s.nuts - n)
    s.stats[h.stat] = (s.stats.getValue(h.stat) - m.p).coerceIn(0, 100)
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
    val a = s.stats.values.sum() / 4.0
    return if (a >= 60) 2 else if (a >= 25) 1 else 0
}
