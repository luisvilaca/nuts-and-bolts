package com.luisvilaca.nutsandbolts

import java.time.LocalDate

// One slot of a habit's period: how it went (null while open) and whether it was logged on another day of the week.
class Slot(val s: Outcome?, val other: Boolean)

fun slots(s: Data, h: Habit, d: LocalDate): List<Slot> {
    val ms = periodMarks(s, h, d)
    return List(h.target) { i -> ms.getOrNull(i)?.let { Slot(it.second.s, it.first != d) } ?: Slot(null, false) }
}

class Orbit(val h: Habit, val slots: List<Slot>)

fun orbitsFor(s: Data, d: LocalDate) = s.habits.filter { active(it, d, s.off) }.map { Orbit(it, slots(s, it, d)) }
