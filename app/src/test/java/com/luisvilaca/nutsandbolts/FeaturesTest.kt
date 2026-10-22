package com.luisvilaca.nutsandbolts

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Days off, notes on bolts, achievements and the week in review. Robolectric for org.json.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeaturesTest {
    private fun d(s: String) = LocalDate.parse(s)
    private val daily get() = Habit("a", "Daily", per = Per.Day, target = 1, stat = Attr.Battery, weight = Heft.Medium)

    @Test fun aDayOffNeitherBreaksNorGrowsAStreak() {
        val s = Data(); val h = daily; s.habits += h
        mark(s, h, d("2026-10-12"), Outcome.Done); mark(s, h, d("2026-10-14"), Outcome.Done)
        assertEquals(1, streak(s, h, d("2026-10-14"))) // the 13th, missed, breaks it
        s.off += d("2026-10-13")
        assertEquals(2, streak(s, h, d("2026-10-14"))) // off, the 13th is passed over
        assertFalse(active(h, d("2026-10-13"), s.off))
        assertEquals(2, record(s, h, d("2026-10-14")).slots)
    }

    @Test fun notesDaysOffAndAchievementsSurviveTheFileAndTheCloud() {
        val s = Data(); val h = daily; s.habits += h
        mark(s, h, d("2026-10-12"), Outcome.Done)!!.note = "8 glasses, cold, 100% done"
        s.off += d("2026-10-13"); give(s, FEATS[0])
        val file = dataFrom(json(s))
        assertEquals("8 glasses, cold, 100% done", file.marks[d("2026-10-12")]!!["a"]!![0].note)
        assertEquals(setOf(d("2026-10-13")), file.off); assertEquals(setOf("first"), file.won)
        val cloud = fromFlat(flat(s), Data())
        assertEquals("8 glasses, cold, 100% done", cloud.marks[d("2026-10-12")]!!["a"]!![0].note)
        assertEquals(setOf(d("2026-10-13")), cloud.off); assertEquals(setOf("first"), cloud.won)
        assertEquals(flat(s), flat(cloud))
    }

    @Test fun anAchievementGivesItsPartFreeOnce() {
        val s = Data(); val h = daily; s.habits += h; s.adj = adjFor(s)
        val t = d("2026-10-12")
        assertTrue(newFeats(s, t).none { it.id == "first" })
        mark(s, h, t, Outcome.Done)
        val f = newFeats(s, t).first { it.id == "first" }
        val bolts = s.bolts
        give(s, f)
        assertTrue(f.part in s.owned)
        recount(s)
        assertEquals(bolts, s.bolts) // the given part costs nothing when the wallet is counted again
        assertTrue(newFeats(s, t).none { it.id == "first" })
    }

    @Test fun theWeekInReview() {
        val s = Data(); val a = daily; val b = Habit("b", "Other", target = 1); s.habits += listOf(a, b)
        val sun = d("2026-10-18")
        for (i in 0..6L) mark(s, a, d("2026-10-12").plusDays(i), Outcome.Done)
        mark(s, b, d("2026-10-12"), Outcome.Done)
        s.diary += d("2026-10-15")
        val r = review(s, sun)
        assertEquals(listOf(8, 14, 1), listOf(r.done, r.slots, r.pages))
        assertEquals(a, r.best); assertEquals(b, r.worst)
    }
}
