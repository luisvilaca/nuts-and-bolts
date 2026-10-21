package com.luisvilaca.nutsandbolts

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncTest {
    private fun d(s: String) = LocalDate.parse(s)
    private fun wallet(s: Data) = listOf(s.bolts, s.nuts, s.stats.toMap(), s.peak.toMap())

    // a state with history: daily and weekly habits, a fail, a level reached, a purchase, an undo
    private fun busy() = Data().apply {
        val a = Habit("a", "Daily", target = 1, stat = Attr.Core, weight = Heft.Heavy)
        val b = Habit("b", "Weekly", per = Per.Week, target = 2, stat = Attr.Chassis, start = d("2026-10-01"), days = setOf(0, 2, 4), remind = "08:30")
        habits += listOf(a, b)
        for (n in 1..9) mark(this, a, d("2026-10-0$n"), Outcome.Done)
        mark(this, a, d("2026-10-10"), Outcome.Fail)
        mark(this, b, d("2026-10-05"), Outcome.Done); mark(this, b, d("2026-10-07"), Outcome.Skip)
        bolts += 300; adj.bolts += 300 // bolts from outside the marks (an older history), carried by adj
        buy(this, PARTS.first { it.id == "head-dome" })
        undo(this, a, d("2026-10-10"))
    }

    @Test fun recountMatchesTheWalletKeptStepByStep() {
        val s = busy()
        val kept = wallet(s)
        recount(s)
        assertEquals(kept, wallet(s))
    }

    @Test fun anOlderStateKeepsItsWalletThroughAdj() {
        val s = busy().apply { marks.values.forEach { hs -> hs.values.forEach { l -> l.forEach { it.a = null } } }; bolts = 777; stats[Attr.Battery] = 55 }
        s.adj = adjFor(s)
        val kept = wallet(s)
        recount(s)
        assertEquals(kept, wallet(s))
    }

    @Test fun theCloudCopyComesBackTheSame() {
        val s = busy()
        val f = flat(s)
        assertEquals(f, flatOf(nest(f))) // what is written reads back equal, so an echo is not taken for news
        val back = fromFlat(f, Data())
        assertEquals(f, flat(back))
        assertEquals(wallet(s), wallet(back))
        assertEquals("08:30", back.habits[1].remind)
    }

    @Test fun twoDevicesMergeTheirMarksAndCountOneWallet() {
        val base = busy()
        val cloud = flat(base).toMutableMap()
        val gone = Any()
        // each device logs a different habit offline, then both changes land in the cloud, field by field
        val tablet = fromFlat(cloud, Data()).also { mark(it, it.habits[0], d("2026-10-12"), Outcome.Done) }
        val phone = fromFlat(cloud, Data()).also { mark(it, it.habits[1], d("2026-10-12"), Outcome.Done) }
        for (dev in listOf(tablet, phone)) for ((k, v) in diff(flat(base), flat(dev), gone)) if (v === gone) cloud.remove(k) else cloud[k] = v
        val merged = fromFlat(cloud, Data())
        assertEquals(2, listOf("a", "b").count { !merged.marks[d("2026-10-12")]?.get(it).isNullOrEmpty() })
        assertEquals(base.bolts + 20 + 10, merged.bolts) // both Dones paid once each
    }

    @Test fun anUndoneBoltIsDeletedInTheCloud() {
        val s = busy(); val before = flat(s)
        undo(s, s.habits[0], d("2026-10-09"))
        val gone = Any()
        assertTrue(diff(before, flat(s), gone)["marks.2026-10-09.a"] === gone)
    }

    @Test fun pagesPackExactly() {
        val p = Page("#393939", mutableListOf(InkStroke("#00FF88", 2.5f, false, floatArrayOf(10.1f, 2259.9f, 0.37f, 1599.9f, 0f, 1f)), InkStroke("#FFFFFF", 9f, true, floatArrayOf(5f, 5f, 0.5f))))
        val q = pageOf(pageBytes(p))
        assertEquals(listOf("#393939", 2), listOf(q.paper, q.strokes.size))
        p.strokes.zip(q.strokes).forEach { (a, b) -> assertEquals(listOf(a.c, a.w, a.e), listOf(b.c, b.w, b.e)); assertArrayEquals(a.p, b.p, 0.0001f) }
    }

    @Test fun aHabitReminderSkipsDaysOff() {
        val now = LocalDateTime.parse("2026-10-21T09:00") // a Wednesday
        val mwf = Habit("m", "M", days = setOf(0, 2, 4))
        assertEquals(LocalDateTime.parse("2026-10-21T18:00"), Reminder.next("18:00", now) { active(mwf, it) })
        assertEquals(LocalDateTime.parse("2026-10-23T08:00"), Reminder.next("08:00", now) { active(mwf, it) }) // Friday
        assertEquals(null, Reminder.next("08:00", now) { false })
    }
}
