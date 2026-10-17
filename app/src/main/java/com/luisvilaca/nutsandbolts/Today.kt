package com.luisvilaca.nutsandbolts

import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.min
import java.time.LocalDate

// A day's progress over the slots its rows draw (a weekly habit's slots included), so Today, Habits, the week and
// the calendar all count the same thing.
fun slotProgress(s: Data, d: LocalDate) = orbitsFor(s, d).flatMap { it.slots }.let { a -> Progress(a.count { it.s == "done" }, a.count { it.s == "fail" }, a.size) }

private val MOOD = listOf("Low power", "Running steady", "Fully charged")

@Composable
fun TodayScreen(store: Store, ui: Ui, lay: Lay) {
    val s = store.s
    val t = LocalDate.now()
    val run = s.habits.filter { active(it, t) }
    val p = slotProgress(s, t)
    val lit = "Today · ${p.done} of ${p.slots} bolts fastened" + if (p.fail > 0) " · ${p.fail} failed" else ""

    val list = @Composable { lead: Boolean ->
        when {
            s.habits.isEmpty() -> Empty(store, ui)
            run.isEmpty() -> Note("No habits run today.")
            else -> {
                // on a phone the next habit to log leads, set large
                val next = if (lead) run.firstOrNull { periodMarks(s, it, t).size < it.target } else null
                if (next != null) HabitRow(store, ui, next, t, big = true)
                run.filter { it !== next }.forEach { HabitRow(store, ui, it, t) }
            }
        }
        if (s.habits.isNotEmpty()) {
            SectionLabel("Attributes")
            Attributes(s)
            SectionLabel("This week")
            Week(s, ui)
        }
    }

    if (lay.wide) Column(Modifier.fillMaxSize()) {
        Header("Today", t.format(dayFmt), store, ui, lay, Modifier.padding(horizontal = 28.dp))
        Row(Modifier.fillMaxSize()) {
            BayPanel(store, Modifier.weight(0.46f).fillMaxHeight(), orbitsFor(s, t))
            Column(Modifier.weight(0.54f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(start = 8.dp, end = 32.dp, bottom = 24.dp)) {
                if (s.habits.isNotEmpty()) Text(lit.uppercase(), Modifier.padding(bottom = 4.dp), style = Label)
                list(false)
            }
        }
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Header("Today", null, store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(bayHeight(lay)), orbitsFor(s, t))
        if (s.habits.isNotEmpty()) Text(lit.uppercase(), Modifier.padding(top = 8.dp, bottom = 4.dp), style = Label)
        list(lay.phone)
    }
}

// The bay is the same picture on Today and Shop: same height, same framing, the nameplate in its top corner.
fun bayHeight(lay: Lay) = if (lay.phone) 360.dp else 560.dp

@Composable
fun BayPanel(store: Store, modifier: Modifier, orbits: List<Orbit>, equip: Map<String, String?> = store.s.equip) {
    val s = store.s
    val tm = clock()
    Box(modifier.padding(12.dp).clip(RoundedCornerShape(20.dp))) {
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = "${s.name}, ${MOOD[mood(s)].lowercase()}" }) {
            room(s, tm, store.react, orbits, store.fresh, equip, band = 72.dp.toPx())
        }
        Column(Modifier.padding(20.dp).clickable(onClickLabel = "wave") { store.react("wave") }) {
            Text(s.name.uppercase(), style = Label.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize, color = W.steel))
            Text(MOOD[mood(s)] + if (orbits.isNotEmpty()) " · building ${plural(min(orbits.size, 6), "frame")}" else "", style = MaterialTheme.typography.bodyMedium, color = W.dim)
        }
    }
}

@Composable
fun Attributes(s: Data) = androidx.compose.foundation.layout.BoxWithConstraints {
    val twoCols = maxWidth >= 600.dp
    val rows = STATS.entries.toList()
    val one = @Composable { e: Map.Entry<String, Stat>, m: Modifier ->
        val v = s.stats.getValue(e.key)
        Row(m.padding(vertical = 6.dp).semantics(mergeDescendants = true) { contentDescription = "${e.value.name}, $v of 100" }, verticalAlignment = Alignment.CenterVertically) {
            Icon(ICONS.getValue(e.key), null, Modifier.size(20.dp), tint = W.steel)
            Column(Modifier.padding(start = 10.dp).weight(1.1f)) {
                Text(e.value.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                Text(e.value.gloss, style = MaterialTheme.typography.labelMedium, color = W.dim)
            }
            Gauge(v / 100f, Modifier.weight(1.4f))
            Text("$v", Modifier.padding(start = 12.dp).size(width = 40.dp, height = 22.dp), style = Numeral.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize), color = W.text)
        }
    }
    if (twoCols) Column { rows.chunked(2).forEach { r -> Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) { r.forEach { one(it, Modifier.weight(1f)) } } } }
    else Column { rows.forEach { one(it, Modifier.fillMaxWidth()) } }
}

// A steel bar on a dark track.
@Composable
fun Gauge(v: Float, modifier: Modifier) = Box(modifier.height(8.dp).clip(CircleShape).background(W.line)) {
    Box(Modifier.fillMaxWidth(v.coerceIn(0f, 1f)).fillMaxHeight().background(W.steel))
}

@Composable
fun Week(s: Data, ui: Ui) {
    val t = LocalDate.now()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (0..6L).map { weekStart(t).plusDays(it) }.forEach { d ->
            val p = slotProgress(s, d)
            Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable { ui.date = d; ui.tab = "habits" }.padding(6.dp)
                .semantics(mergeDescendants = true) { contentDescription = "${d.format(dayFmt)}, ${p.done} of ${p.slots} done" + if (p.fail > 0) ", ${p.fail} failed" else "" },
                horizontalAlignment = Alignment.CenterHorizontally) {
                DayMark(p, d > t, Modifier.size(42.dp), today = d == t)
                Text(DOW[d.dayOfWeek.value - 1], style = MaterialTheme.typography.labelMedium, color = if (d == t) W.text else W.dim)
            }
        }
    }
}

private val SUGGEST = listOf(
    Habit("", "Drink water", per = "day", target = 3, stat = "battery", weight = "light"),
    Habit("", "Read 20 minutes", per = "day", target = 1, stat = "processor", weight = "medium"),
    Habit("", "Workout", per = "week", target = 3, stat = "chassis", weight = "heavy"),
    Habit("", "Call a friend", per = "week", target = 1, stat = "core", weight = "medium"),
    Habit("", "No phone in bed", kind = "bad", per = "day", target = 1, stat = "battery", weight = "medium"),
)

fun newId() = "h" + System.currentTimeMillis().toString(36) + (0..999).random()

@Composable
fun Empty(store: Store, ui: Ui) = Column(Modifier.padding(vertical = 16.dp)) {
    Text("The bench is empty", style = MaterialTheme.typography.headlineSmall)
    Text("A habit is something you want to do, or stop doing, a set number of times a day or a week. Each one becomes a frame on ${store.s.name}'s workbench, and every time you do it a bolt goes in.",
        Modifier.padding(top = 8.dp, bottom = 16.dp), style = MaterialTheme.typography.bodyLarge, color = W.dim)
    Button(onClick = { ui.tab = "habits"; ui.edit = "" }, Modifier.height(48.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct)) {
        Icon(ICONS.getValue("plus"), null, Modifier.size(20.dp)); Text("Add your first habit", Modifier.padding(start = 8.dp))
    }
    Text("Or start from one of these:", Modifier.padding(top = 20.dp, bottom = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SUGGEST.forEach { h ->
            androidx.compose.material3.SuggestionChip(onClick = {
                store.change { habits += Habit(newId(), h.name, h.kind, h.per, h.target, h.stat, h.weight, start = LocalDate.now()) }
            }, label = { Text("${h.name} · ${h.target} a ${h.per}") }, Modifier.height(40.dp),
                border = androidx.compose.material3.SuggestionChipDefaults.suggestionChipBorder(true, borderColor = W.edge),
                colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(labelColor = W.text))
        }
    }
}
