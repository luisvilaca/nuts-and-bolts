package com.luisvilaca.nutsandbolts

import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

// Habit rules. No Android in here so the unit tests run on the JVM.

// Codes are typed: each enum keeps the string it is saved under (id), so state files and backups read the same as
// before; Store.kt maps between the two at the JSON boundary and nowhere else.
interface Coded { val id: String }
inline fun <reified E> byId(id: String): E where E : Enum<E>, E : Coded = enumValues<E>().first { it.id == id }

enum class Kind(override val id: String) : Coded { Build("good"), Break("bad") }
enum class Per(override val id: String) : Coded { Day("day"), Week("week") }
enum class Outcome(override val id: String) : Coded { Done("done"), Skip("skip"), Fail("fail") }

// The four attributes. stages: the name of each of its ten levels, in the robot's own terms.
// title, gloss and stages read in the interface language (Lang.kt); en, enGloss and enStages are the keys.
enum class Attr(override val id: String, val en: String, val enGloss: String, val enStages: List<String>) : Coded {
    Battery("battery", "Battery", "energy", listOf("AA Cell", "Nickel Pack", "Lithium Cell", "Li-ion Bank", "Solid-State", "Supercapacitor", "Graphene Cell", "Fusion Cell", "Zero-Point Cell", "Antimatter Cell")),
    Processor("processor", "Processor", "mind", listOf("4-bit", "8-bit", "16-bit", "32-bit", "64-bit", "Multicore", "Neural Engine", "Quantum Core", "Photonic Mind", "Singularity")),
    Chassis("chassis", "Chassis", "body", listOf("Tin Can", "Aluminium", "Steel", "Titanium", "Carbon Fibre", "Tungsten", "Graphene", "Nanoweave", "Diamond Lattice", "Neutronium")),
    Core("core", "Core", "heart", listOf("Spark", "Dynamo", "Turbine", "Fission Pile", "Breeder", "Tokamak", "Fusion Heart", "Stellarator", "Star Core", "Supernova"));
    val title get() = t(en)
    val gloss get() = t(enGloss)
    val stages get() = enStages.map { t(it) }
}

// What a Done pays (and a fail takes back): bolts and attribute points.
enum class Heft(override val id: String, val en: String, val bolts: Int, val pts: Int) : Coded {
    Light("light", "Light", 5, 1), Medium("medium", "Medium", 10, 2), Heavy("heavy", "Heavy", 20, 4);
    val title get() = t(en)
}

// Ten levels per attribute: level n starts at 10 n (n - 1) points (0, 20, 60, 120 ... 900); points count on past the top.
val LEVEL_AT = List(10) { 10 * it * (it + 1) }
fun level(p: Int) = LEVEL_AT.indexOfLast { p >= it } + 1
fun levelShare(p: Int): Float = level(p).let { l -> if (l == LEVEL_AT.size) 1f else (p - LEVEL_AT[l - 1]).toFloat() / (LEVEL_AT[l] - LEVEL_AT[l - 1]) }
const val LEVEL_NUTS = 2 // for each level an attribute reaches for the first time

class Habit(
    var id: String,
    var name: String,
    var kind: Kind = Kind.Build,
    var per: Per = Per.Day,
    var target: Int = 1,
    var stat: Attr = Attr.Battery,
    var weight: Heft = Heft.Medium,
    var start: LocalDate? = null,
    var end: LocalDate? = null,
    var days: Set<Int>? = null, // 0 is Monday; null is every day
    var remind: String? = null, // "HH:mm": a notification at that time on the days it runs while it has open slots
)

// Each mark stores exactly what it changed (bolts, points, nuts, and l: new levels it reached) and a, the attribute its
// points went to, so undo is exact and the wallet can be counted again from the marks alone (see recount).
// note: what the owner wrote on the bolt ("8 glasses", "5 km"), if anything.
class Mark(val s: Outcome, var b: Int = 0, var p: Int = 0, var n: Int = 0, var l: Int = 0, var a: Attr? = null, var note: String? = null)

// nib and rub (the eraser) are widths in mm as drawn on this screen; stab is the stabilizer in percent, 0 to 100 by tens.
// palm: palm rejection; touches near the pen or as broad as a palm do nothing, and the pen's first stroke turns move on.
// move: one finger moves the page instead of writing (two fingers always zoom and move it); the pen always writes.
// b1, b2: what the pen's two side buttons do: "eraser" (pen and eraser in turn), "ink" (the next ink), "undo" or "none".
class Pen(var color: String = PEN_COLORS[0].second, var nib: Float = 0.6f, var stab: Int = 50, var rub: Float = 4f, var paper: Int = 0, var seen: Boolean = false, var palm: Boolean = true,
          var b1: String = "eraser", var b2: String = "ink", var move: Boolean = false, var tpl: String = "lined", var gap: Float = 55f) // tpl, gap: for new pages
class Remind(var on: Boolean = false, var time: String = "21:00")

class Data {
    var name = "Rivet"
    var bolts = 0
    var nuts = 0
    val stats = Attr.entries.associateWith { 30 }.toMutableMap()
    val peak = Attr.entries.associateWith { level(30) }.toMutableMap() // the highest level each attribute has reached
    val owned = mutableListOf("head-box", "body-crate", "arms-clamp", "legs-stub", "base-bay")
    val equip = mutableMapOf<String, String?>("head" to "head-box", "body" to "body-crate", "arms" to "arms-clamp", "legs" to "legs-stub", "acc" to null, "base" to "base-bay")
    val habits = mutableListOf<Habit>()
    val marks = mutableMapOf<LocalDate, MutableMap<String, MutableList<Mark>>>()
    val diary = mutableSetOf<LocalDate>()
    val off = mutableSetOf<LocalDate>() // days off: nothing runs, and no streak breaks or grows
    val won = mutableSetOf<String>() // achievements reached (FEATS), each with its part given
    val papers = PAPERS.toMutableList()
    var pen = Pen()
    var remind = Remind()
    var sound = true // the effects in Sfx
    var lang = "" // this device's interface language: "" follows the system, else "en", "pt" or "ru" (Lang.kt)
    var backedUp: LocalDate? = null
    var adj = Wallet() // what the marks and parts do not account for: the start (30 points, level 2), and older history
}

// Bolts, nuts, points and peak levels, as a sum or as an offset.
class Wallet(var bolts: Int = 0, var nuts: Int = 0, val stats: MutableMap<Attr, Int> = Attr.entries.associateWith { 30 }.toMutableMap(),
             val peak: MutableMap<Attr, Int> = Attr.entries.associateWith { level(30) }.toMutableMap())

// What the marks and the parts owned add up to on their own.
fun sums(s: Data) = Wallet(0, 0, Attr.entries.associateWith { 0 }.toMutableMap(), Attr.entries.associateWith { 0 }.toMutableMap()).apply {
    for (ms in s.marks.values) for (l in ms.values) for (m in l) {
        bolts += m.b; nuts += m.n + LEVEL_NUTS * m.l
        m.a?.let { stats[it] = stats.getValue(it) + m.p; peak[it] = peak.getValue(it) + m.l }
    }
    // a part given for an achievement was never paid for
    val given = FEATS.filter { it.id in s.won }.map { it.part }.toSet()
    for (id in s.owned) if (id !in given) PART[id]?.let { bolts -= it.bolts; nuts -= it.nuts }
}

// The offset that makes the marks and parts add up to the wallet as it stands (once, for a state from before adj).
fun adjFor(s: Data): Wallet = sums(s).let { x ->
    Wallet(s.bolts - x.bolts, s.nuts - x.nuts, Attr.entries.associateWith { s.stats.getValue(it) - x.stats.getValue(it) }.toMutableMap(),
        Attr.entries.associateWith { s.peak.getValue(it) - x.peak.getValue(it) }.toMutableMap())
}

// The wallet counted again from the marks and parts. Two devices that log offline each change their own marks; once
// the marks meet, both count the same wallet, where adding up each device's balance would count one of them twice.
fun recount(s: Data) {
    val x = sums(s)
    s.bolts = (s.adj.bolts + x.bolts).coerceAtLeast(0); s.nuts = (s.adj.nuts + x.nuts).coerceAtLeast(0)
    Attr.entries.forEach { a ->
        s.stats[a] = (s.adj.stats.getValue(a) + x.stats.getValue(a)).coerceAtLeast(0)
        s.peak[a] = (s.adj.peak.getValue(a) + x.peak.getValue(a)).coerceAtLeast(1)
    }
}

// A habit comes off the list at its place, which comes back so undo puts it where it was. Its marks stay.
fun removeHabit(s: Data, h: Habit): Int = s.habits.indexOf(h).also { if (it >= 0) s.habits.removeAt(it) }
fun addSuggested(s: Data, h: Habit, id: String, t: LocalDate) { s.habits += Habit(id, h.name, h.kind, h.per, h.target, h.stat, h.weight, start = t) }
fun restoreHabit(s: Data, h: Habit, at: Int) { if (h !in s.habits) s.habits.add(at.coerceIn(0, s.habits.size), h) }

fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value - 1).toLong())
fun periodDates(h: Habit, d: LocalDate) = if (h.per == Per.Week) (0..6L).map { weekStart(d).plusDays(it) } else listOf(d)
fun periodMarks(s: Data, h: Habit, d: LocalDate) = periodDates(h, d).flatMap { x -> s.marks[x]?.get(h.id).orEmpty().map { x to it } }
// The most slots a period can hold: a dozen in a day, one a day in a week.
fun maxTarget(per: Per) = if (per == Per.Week) 7 else 12

// off: the days off; a day off is like a day the habit is not scheduled.
fun active(h: Habit, d: LocalDate, off: Set<LocalDate> = emptySet()) = d !in off && (h.start == null || h.start!! <= d) && (h.end == null || d <= h.end!!) && (h.days == null || (d.dayOfWeek.value - 1) in h.days!!)
// Every slot of the period logged, whatever the outcome: nothing left to remind about.
fun logged(s: Data, h: Habit, d: LocalDate) = periodMarks(s, h, d).size >= h.target
fun locked(s: Data, h: Habit, d: LocalDate) = periodMarks(s, h, d).count { it.second.s == Outcome.Done } >= h.target

fun streak(s: Data, h: Habit, d: LocalDate): Int {
    val step = if (h.per == Per.Week) 7L else 1L
    var n = 0
    var x = d
    repeat(2000) {
        if (h.start != null && x < h.start) return n
        if (periodDates(h, x).any { active(h, it, s.off) }) { // a day off neither breaks nor extends the streak
            if (!locked(s, h, x)) return n
            n++
        }
        x = x.minusDays(step)
    }
    return n
}

// periods: how many periods the habit has run, the length of its lifetime in days or weeks
data class Record(val slots: Int, val done: Int, val fail: Int, val best: Int, val now: Int, val periods: Int = 0)

// A habit's lifetime up to day t: slots it asked for, how they went, and its longest run of full periods.
// The period holding t is still open, so missing it does not end the run.
fun record(s: Data, h: Habit, t: LocalDate): Record {
    val first = h.start ?: s.marks.keys.filter { s.marks[it]!!.containsKey(h.id) }.minOrNull() ?: t
    val last = if (h.end != null && h.end!! < t) h.end!! else t
    val step = if (h.per == Per.Week) 7L else 1L
    var slots = 0; var done = 0; var fail = 0; var best = 0; var run = 0; var periods = 0
    var d = if (h.per == Per.Week) weekStart(first) else first
    while (d <= last) {
        if (periodDates(h, d).any { active(h, it, s.off) }) {
            val ms = periodMarks(s, h, d)
            slots += h.target; periods++
            done += ms.count { it.second.s == Outcome.Done }
            fail += ms.count { it.second.s == Outcome.Fail }
            if (locked(s, h, d)) best = max(best, ++run)
            else if (t !in periodDates(h, d)) run = 0
        }
        d = d.plusDays(step)
    }
    return Record(slots, done, fail, best, streak(s, h, t), periods)
}

// Fills the next slot of the habit's period.
fun mark(s: Data, h: Habit, d: LocalDate, kind: Outcome): Mark? {
    if (periodMarks(s, h, d).size >= h.target) return null
    val w = h.weight
    val m = Mark(kind, a = h.stat)
    if (kind == Outcome.Done) { m.b = w.bolts; m.p = w.pts }
    if (kind == Outcome.Fail) { m.b = -min(w.bolts, s.bolts); m.p = -min(w.pts, s.stats.getValue(h.stat)) }
    s.marks.getOrPut(d) { mutableMapOf() }.getOrPut(h.id) { mutableListOf() }.add(m)
    // a nut for every finished week, or every third finished day in a row
    if (kind == Outcome.Done && locked(s, h, d) && (h.per == Per.Week || streak(s, h, d) % 3 == 0)) m.n = 1
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

// Buying never goes below zero and never buys twice.
fun buy(s: Data, p: Part): Boolean {
    if (p.id in s.owned || s.bolts < p.bolts || s.nuts < p.nuts) return false
    s.bolts -= p.bolts; s.nuts -= p.nuts
    s.owned += p.id
    s.equip[p.slot] = p.id
    return true
}

// A purchase taken back: the part leaves, the price comes back, and the slot wears what it wore before.
fun unbuy(s: Data, p: Part, before: String?) {
    if (p.id !in s.owned) return
    s.owned -= p.id; s.bolts += p.bolts; s.nuts += p.nuts; s.equip[p.slot] = before
}

fun mood(s: Data): Int { // 2 fully charged, 1 running steady, 0 low power
    val a = s.stats.values.sumOf { level(it) } / 4.0
    return if (a >= 4) 2 else if (a >= 2) 1 else 0
}

// ---------- achievements ----------

// An achievement and the part it gives: reaching it puts the part in the owned list for free (sums knows not to
// charge for it). en is the name's key for t().
class Feat(val id: String, val en: String, val part: String, val reached: (Data, LocalDate) -> Boolean)

val FEATS = listOf(
    Feat("first", "First bolt", "acc-scarf") { s, _ -> s.marks.values.any { d -> d.values.any { ms -> ms.any { it.s == Outcome.Done } } } },
    Feat("week", "Seven days running", "acc-bow") { s, t -> s.habits.any { it.per == Per.Day && record(s, it, t).best >= 7 } },
    Feat("critical", "A whole day critical", "acc-halo") { s, t -> s.habits.any { active(it, t, s.off) } && s.habits.filter { active(it, t, s.off) }.all { locked(s, it, t) } },
    Feat("bolts", "500 bolts earned", "acc-crown") { s, _ -> s.marks.values.sumOf { d -> d.values.sumOf { ms -> ms.sumOf { maxOf(0, it.b) } } } >= 500 },
    Feat("pages", "Seven diary pages", "acc-moon") { s, _ -> s.diary.size >= 7 },
    Feat("level", "An attribute at level 5", "acc-drone") { s, _ -> s.peak.values.any { it >= 5 } },
)

// The achievements reached now and not yet recorded; give records them and their parts.
fun newFeats(s: Data, t: LocalDate) = FEATS.filter { it.id !in s.won && it.reached(s, t) }
fun give(s: Data, f: Feat) { s.won += f.id; if (f.part !in s.owned) s.owned += f.part }

// ---------- the week in review ----------

// One week, Monday to Sunday up to today: bolts in of slots, nuts earned, pages written, and the habits that did best
// and worst by share of their bolts in.
class Review(val done: Int, val slots: Int, val nuts: Int, val pages: Int, val best: Habit?, val worst: Habit?)

fun review(s: Data, t: LocalDate): Review {
    val days = generateSequence(weekStart(t)) { it.plusDays(1) }.takeWhile { it <= t }.toList()
    var done = 0; var slots = 0; var nuts = 0
    val share = mutableMapOf<Habit, Pair<Int, Int>>()
    for (h in s.habits) {
        val ds = days.filter { active(h, it, s.off) }
        if (ds.isEmpty()) continue
        val ms = if (h.per == Per.Week) periodMarks(s, h, t).map { it.second } else ds.flatMap { s.marks[it]?.get(h.id).orEmpty() }
        val n = if (h.per == Per.Week) h.target else h.target * ds.size
        val d = ms.count { it.s == Outcome.Done }
        done += d; slots += n; nuts += ms.sumOf { it.n + LEVEL_NUTS * it.l }
        share[h] = d to n
    }
    val ranked = share.entries.sortedBy { it.value.first.toFloat() / it.value.second }
    return Review(done, slots, nuts, s.diary.count { it in days }, ranked.lastOrNull()?.key, ranked.firstOrNull()?.takeIf { ranked.size > 1 && it.value.first < it.value.second }?.key)
}
