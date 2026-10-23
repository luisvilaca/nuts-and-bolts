package com.luisvilaca.nutsandbolts

import java.io.File
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LangTest {
    @After fun reset() { Lang.pick = "" }

    // Every phrase the code passes to t(), and every name it translates indirectly, is in both tables, so no screen
    // falls back to English halfway through.
    @Test fun everyPhraseIsTranslated() {
        val src = File(System.getProperty("user.dir"), "src/main/java/com/luisvilaca/nutsandbolts").listFiles()!!.filter { it.extension == "kt" && !it.name.startsWith("Lang") }
        val keys = linkedSetOf<String>()
        for (f in src) {
            val s = f.readText()
            Regex("""(?<![A-Za-z0-9_])t\(""").findAll(s).forEach { m ->
                // the literals before the call's first comma at depth 0: the key, or the keys of a ternary
                var j = m.range.last + 1; var depth = 1; val buf = StringBuilder()
                while (j < s.length && depth > 0) {
                    val c = s[j]
                    if (c == '"') { var e = j + 1; while (s[e] != '"' || s[e - 1] == '\\') e++; buf.append(s, j, e + 1); j = e + 1; continue }
                    if (c in "([{") depth++ else if (c in ")]}") depth-- else if (c == ',' && depth == 1) break
                    buf.append(c); j++
                }
                Regex(""""((?:[^"\\]|\\.)*)"""").findAll(buf).forEach { keys += it.groupValues[1] }
            }
        }
        keys -= setOf("base") // a slot code inside a condition, not a phrase
        keys += Attr.entries.flatMap { listOf(it.en, it.enGloss) + it.enStages } + Heft.entries.map { it.en } + PARTS.map { it.en } + FEATS.map { it.en }
        keys += listOf("Low power", "Running steady", "Fully charged", "Lab", "Shop", "Habits", "Diary", "Skip", "Fail", "Gave in", "Open", "Name", "Type",
            "Attribute it trains", "Schedule", "Reminder", "Weight", "Reward", "Robot", "Language", "System", "Sync", "Backup", "Daily reminder", "Sounds", "Pen buttons",
            "Head", "Body", "Arms", "Legs", "Accessories", "Base", "Drink water", "Workout", "Call a friend", "No phone in bed",
            "Give the habit a name.", "Pick at least one day of the week.", "Pick the day it ends, or let it run indefinitely.", "The end day comes before the start day.",
            "Build it: you earn each time you do it. Break it: you earn each time you resist it.", "Done pays this much. Failed takes the same amount back.",
            "Back to calendar", "Pen. Tap again for nib and stabilizer", "Eraser. Tap again for its size", "Move: a finger moves the page. Tap to write with a finger",
            "Move: off, a finger writes. Tap to move the page with a finger", "Ink colour", "Undo stroke", "Redo stroke", "Paper", "Bin this page",
            "Pen ↔ eraser", "Next ink", "Nothing", "Move in", "Equip", "Buddy", "bolts in", "this run", "best run", "sheared", "{0} a day", "{0} a week",
            "EEEE d MMMM", "EEE d MMM", "MMMM yyyy", "d MMM", "d MMMM", "d MMMM yyyy", "EEEE") + PEN_COLORS.map { it.first }
        for ((name, table) in listOf("pt" to PT, "ru" to RU)) {
            val missing = keys.filter { it !in table && it !in SKIN_WORDS[name].orEmpty() } // the skin may supply its own words
            assertTrue("$name is missing: $missing", missing.isEmpty())
        }
        assertEquals(PT.keys.toSet().size, PT.size) // no phrase written twice
    }

    @Test fun russianPluralsTakeThreeForms() {
        val (bolt, nut, en) = if (SKIN == "circus") Triple(listOf("билет", "билета", "билетов"), listOf("Faz-Token", "Faz-Tokens"), listOf("ticket", "tickets"))
            else Triple(listOf("болт", "болта", "болтов"), listOf("porca", "porcas"), listOf("bolt", "bolts"))
        Lang.pick = "ru"
        assertEquals(listOf(1 to 0, 2 to 1, 5 to 2, 11 to 2, 21 to 0, 22 to 1, 112 to 2).map { (n, f) -> "$n ${bolt[f]}" }, listOf(1, 2, 5, 11, 21, 22, 112).map { plural(it, "bolt") })
        Lang.pick = "pt"
        assertEquals(listOf("1 ${nut[0]}", "0 ${nut[1]}", "3 ${nut[1]}"), listOf(1, 0, 3).map { plural(it, "nut") })
        Lang.pick = "en"
        assertEquals(listOf("1 ${en[0]}", "4 ${en[1]}"), listOf(1, 4).map { plural(it, "bolt") })
    }

    @Test fun phrasesAndDatesFollowTheLanguage() {
        val d = LocalDate.of(2026, 10, 22)
        Lang.pick = "en"
        assertEquals("Thursday 22 October", fmt(d, "EEEE d MMMM")); assertEquals("Done", t("Done")); assertEquals("Day", t("per day"))
        Lang.pick = "pt"
        assertEquals("Quinta-feira, 22 de outubro", fmt(d, "EEEE d MMMM")); assertEquals("Feito", t("Done"))
        assertEquals(if (SKIN == "circus") "Até 20 bilhetes e 4 de Energia por dia." else "Até 20 parafusos e 4 de Bateria por dia.", t("Up to {0} and {1} {2} a day.", plural(20, "bolt"), 4, Attr.Battery.title))
        Lang.pick = "ru"
        assertEquals("Октябрь 2026", fmt(d, "MMMM yyyy")); assertEquals(if (SKIN == "circus") "Энергия" else "Батарея", Attr.Battery.title); assertEquals("Пн", DOW[0])
    }
}
