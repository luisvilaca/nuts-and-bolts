package com.luisvilaca.nutsandbolts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAccessor
import java.util.Locale
import kotlin.math.abs

// The interface in English, Portuguese (Portugal) or Russian. English is the key: t("Done") reads "Feito" in
// Portuguese, and {0}, {1}... take the arguments. A phrase missing from a table stays English. No Android in here, so
// the unit tests check the tables on the JVM.
object Lang {
    // "" follows the device's language (Android's own per-app language included); "en", "pt" or "ru" is the owner's pick.
    // A State, so every screen recomposes in the new language the moment it changes.
    var pick by mutableStateOf("")
    val code: String get() = pick.ifEmpty { Locale.getDefault().language }.let { if (it == "pt" || it == "ru") it else "en" }
    val locale: Locale get() = when (code) { "pt" -> Locale.forLanguageTag("pt-PT"); "ru" -> Locale.forLanguageTag("ru-RU"); else -> Locale.UK }
    // the choices in Settings, each named in its own language
    val CHOICES = listOf("" to "System", "en" to "English", "pt" to "Português", "ru" to "Русский")
}

fun t(en: String, vararg a: Any?): String {
    var s = (when (Lang.code) { "pt" -> PT; "ru" -> RU; else -> EN })[en] ?: en
    a.forEachIndexed { i, v -> s = s.replace("{$i}", v.toString()) }
    return s
}

// "3 bolts": a count and its noun in the right plural form (Russian has three: 1 болт, 2 болта, 5 болтов).
fun plural(n: Int, w: String): String {
    val f = NOUNS[w]?.get(Lang.code) ?: return "$n $w${if (abs(n) == 1) "" else "s"}"
    val i = if (Lang.code == "ru") { val a = abs(n) % 100; val b = a % 10; if (b == 1 && a != 11) 0 else if (b in 2..4 && a !in 12..14) 1 else 2 }
        else if (abs(n) == 1) 0 else 1
    return "$n ${f[minOf(i, f.size - 1)]}"
}

// A date in the language's own pattern; the pattern itself is a phrase in the tables ("d 'de' MMMM" in Portuguese).
fun fmt(d: TemporalAccessor, pattern: String): String =
    DateTimeFormatter.ofPattern(t(pattern), Lang.locale).format(d).replaceFirstChar { it.titlecase(Lang.locale) }

// "3 a day", "1 a week"
fun often(n: Int, per: Per) = t(if (per == Per.Day) "{0} a day" else "{0} a week", n)

// Monday first, short: Mon, Seg, Пн
val DOW: List<String> get() = DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, Lang.locale).trimEnd('.').replaceFirstChar { c -> c.titlecase(Lang.locale) } }

private val NOUNS = mapOf(
    "bolt" to mapOf("en" to listOf("bolt", "bolts"), "pt" to listOf("parafuso", "parafusos"), "ru" to listOf("болт", "болта", "болтов")),
    "nut" to mapOf("en" to listOf("nut", "nuts"), "pt" to listOf("porca", "porcas"), "ru" to listOf("гайка", "гайки", "гаек")),
    "point" to mapOf("en" to listOf("point", "points"), "pt" to listOf("ponto", "pontos"), "ru" to listOf("очко", "очка", "очков")),
    "day" to mapOf("en" to listOf("day", "days"), "pt" to listOf("dia", "dias"), "ru" to listOf("день", "дня", "дней")),
    "week" to mapOf("en" to listOf("week", "weeks"), "pt" to listOf("semana", "semanas"), "ru" to listOf("неделя", "недели", "недель")),
    "sheared" to mapOf("en" to listOf("sheared"), "pt" to listOf("partido", "partidos"), "ru" to listOf("срезан", "срезано", "срезано")),
    "page" to mapOf("en" to listOf("page", "pages"), "pt" to listOf("página", "páginas"), "ru" to listOf("страница", "страницы", "страниц")),
    "Done" to mapOf("en" to listOf("Done", "Dones"), "pt" to listOf("Feito", "Feitos"), "ru" to listOf("отметка", "отметки", "отметок")),
    "habit" to mapOf("en" to listOf("habit", "habits"), "pt" to listOf("hábito", "hábitos"), "ru" to listOf("привычка", "привычки", "привычек")),
)

// The robot's name as shown: the default placeholder reads in the language (Rebite, Ривет); a name the owner gave stays.
val Data.robot: String get() = if (name == "Rivet") t("Rivet") else name

