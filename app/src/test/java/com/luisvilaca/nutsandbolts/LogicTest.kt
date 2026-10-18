package com.luisvilaca.nutsandbolts

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LogicTest {
    private fun d(s: String) = LocalDate.parse(s)
    private fun fresh() = Data().apply { stats.keys.forEach { stats[it] = 20 } }
    private val daily get() = Habit("a", "Daily", per = "day", target = 1, stat = "battery", weight = "medium")
    private val weekly get() = Habit("b", "Weekly", per = "week", target = 2, stat = "chassis", weight = "heavy")
    private fun Mark.v() = listOf(s, b, p, n)

    @Test fun doneAndStreakNuts() {
        val s = fresh(); val h = daily; s.habits += h
        assertEquals(listOf("done", 10, 2, 0), mark(s, h, d("2026-10-12"), "done")!!.v())
        assertNull(mark(s, h, d("2026-10-12"), "done")) // a full period refuses more marks
        assertEquals(10, s.bolts); assertEquals(22, s.stats["battery"])
        mark(s, h, d("2026-10-13"), "done")
        assertEquals(1, mark(s, h, d("2026-10-14"), "done")!!.n) // third day in a row earns a nut
        assertEquals(3, streak(s, h, d("2026-10-14")))
        undo(s, h, d("2026-10-14"))
        assertEquals(listOf(0, 20, 24), listOf(s.nuts, s.bolts, s.stats["battery"]))
        assertFalse(locked(s, h, d("2026-10-14")))
    }

    @Test fun failNeverBelowZeroAndUndoIsExact() {
        val s = fresh(); val h = daily; s.habits += h; s.bolts = 4
        assertEquals(listOf("fail", -4, -2, 0), mark(s, h, d("2026-10-12"), "fail")!!.v())
        assertEquals(0, s.bolts)
        undo(s, h, d("2026-10-12"))
        assertEquals(listOf(4, 20, true), listOf(s.bolts, s.stats["battery"], s.marks.isEmpty()))
    }

    @Test fun weeklySlotsSpanTheWeek() {
        val s = fresh(); val w = weekly; s.habits += listOf(daily, w)
        assertEquals(d("2026-10-12"), weekStart(d("2026-10-18")))
        mark(s, w, d("2026-10-12"), "done")
        assertEquals(1, mark(s, w, d("2026-10-15"), "done")!!.n) // finishing the week earns a nut
        assertNull(mark(s, w, d("2026-10-17"), "done"))
        undo(s, w, d("2026-10-12")) // undoing the earlier mark still takes the nut back
        assertEquals(0, s.nuts)
        assertEquals(Progress(1, 0, 2), dayProgress(s, d("2026-10-15")))
    }

    @Test fun datesAndWeekdays() {
        val dated = daily.apply { id = "c"; start = d("2026-10-10"); end = d("2026-10-20") }
        assertEquals(listOf(false, true, true, false), listOf("2026-10-09", "2026-10-10", "2026-10-20", "2026-10-21").map { active(dated, d(it)) })
        val s = fresh(); s.habits += dated
        assertEquals(0, dayProgress(s, d("2026-10-21")).slots)
        assertEquals(1, dayProgress(s, d("2026-10-15")).slots)

        val mwf = daily.apply { id = "d"; start = d("2026-10-12"); days = setOf(0, 2, 4) }
        assertEquals(listOf(true, false, true), listOf("2026-10-12", "2026-10-13", "2026-10-14").map { active(mwf, d(it)) })
        val t = fresh(); t.habits += mwf
        mark(t, mwf, d("2026-10-12"), "done"); mark(t, mwf, d("2026-10-14"), "done")
        assertEquals(2, streak(t, mwf, d("2026-10-15"))) // days off neither break nor extend
        assertEquals(1, mark(t, mwf, d("2026-10-16"), "done")!!.n)
        mark(t, mwf, d("2026-10-19"), "fail")
        assertEquals(Record(4, 3, 1, 3, 0), record(t, mwf, d("2026-10-19")))
        assertEquals(Record(3, 3, 0, 3, 3), record(t, mwf, d("2026-10-16"))) // today's open slot does not end the run
    }

    @Test fun buyingNeedsBothCurrencies() {
        val s = fresh(); val p = PARTS.first { it.id == "head-hex" } // 150 bolts, 3 nuts
        s.bolts = 200
        assertFalse(buy(s, p))
        s.nuts = 3
        assertEquals(true, buy(s, p))
        assertEquals(listOf(50, 0, "head-hex"), listOf(s.bolts, s.nuts, s.equip["head"]))
        assertFalse(buy(s, p))
        unbuy(s, p, "head-box") // undo is exact: price back, part gone, the old head on again
        assertEquals(listOf(200, 3, "head-box", false), listOf(s.bolts, s.nuts, s.equip["head"], p.id in s.owned))
    }

    @Test fun levelsPayNutsOnceAndUndoIsExact() {
        val s = fresh().apply { stats["core"] = 58 } // level 2, two points short of level 3
        val h = Habit("x", "X", per = "day", target = 3, stat = "core", weight = "heavy", start = d("2026-10-01"))
        val day = d("2026-10-05")
        val m = mark(s, h, day, "done")!!
        assertEquals(listOf(62, 3, 1, 2), listOf(s.stats["core"], s.peak["core"], m.l, s.nuts))
        undo(s, h, day)
        assertEquals(listOf(58, 2, 0), listOf(s.stats["core"], s.peak["core"], s.nuts))
        mark(s, h, day, "done"); mark(s, h, day, "fail") // up to level 3 and its nuts, then back down
        val again = mark(s, h, day, "done")!!
        assertEquals(listOf(62, 0, 2), listOf(s.stats["core"], again.l, s.nuts)) // level 3 again pays nothing
        assertEquals(listOf(1, 2, 3, 10, 10), listOf(level(0), level(20), level(60), level(900), level(5000)))
    }

    @Test fun deleteAndUndoKeepPlaceAndMarks() {
        val s = fresh(); val a = daily; val b = weekly; s.habits += listOf(a, b)
        mark(s, a, d("2026-10-12"), "done")
        val at = removeHabit(s, a)
        assertEquals(listOf(0, listOf(b)), listOf(at, s.habits))
        restoreHabit(s, a, at)
        assertEquals(listOf(a, b), s.habits)
        assertEquals(1, periodMarks(s, a, d("2026-10-12")).size) // its marks were never touched
    }
}
