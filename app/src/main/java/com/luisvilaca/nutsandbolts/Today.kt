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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
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
    val p = slotProgress(s, t)
    val lit = "Today · ${p.done} of ${p.slots} bolts fastened" + if (p.fail > 0) " · ${p.fail} failed" else ""

    // Today is the robot and how it is doing; the habits themselves are logged on Habits, one tap away.
    val tally = @Composable {
        Row(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).clickable(onClickLabel = "log on Habits") { ui.date = t; ui.tab = "habits" },
            verticalAlignment = Alignment.CenterVertically) {
            Text(lit.uppercase(), style = Label)
            Icon(ICONS.getValue("next"), null, Modifier.padding(start = 6.dp).size(16.dp), tint = W.act)
        }
    }
    // stacked: one attribute per row, where the side has the height for it
    val gauges = @Composable { stacked: Boolean ->
        SectionLabel("Attributes")
        Attributes(s, stacked)
        SectionLabel("This week")
        Week(s, ui)
    }

    if (lay.wide) Column(Modifier.fillMaxSize()) {
        Header("Today", t.format(dayFmt), store, ui, lay, Modifier.padding(horizontal = 28.dp))
        Row(Modifier.fillMaxSize()) {
            BayPanel(store, Modifier.weight(0.46f).fillMaxHeight(), orbitsFor(s, t))
            // the next level-up at the top; the tally, the attributes and the week down at the bay's floor line
            BoxWithConstraints(Modifier.weight(0.54f).fillMaxHeight()) {
                val tall = maxHeight >= 560.dp
                Column(Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(start = 8.dp, end = 32.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween) {
                    if (s.habits.isEmpty()) Empty(store, ui)
                    else {
                        NextLevel(s, t, Modifier.padding(top = 8.dp, bottom = 24.dp))
                        Column { tally(); gauges(tall) }
                    }
                }
            }
        }
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Header("Today", null, store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(bayHeight(lay)), orbitsFor(s, t))
        if (s.habits.isEmpty()) Empty(store, ui)
        else { tally(); NextLevel(s, t, Modifier.padding(top = 16.dp)); gauges(false) }
    }
}

// The level-up closest at hand and the habit that gets there fastest, so the space beside the bay says what to do next.
@Composable
private fun NextLevel(s: Data, t: LocalDate, modifier: Modifier) = nextUp(s, t)?.let { n ->
    val st = STATS.getValue(n.stat)
    Column(modifier.semantics(mergeDescendants = true) {}) {
        Text("${st.name} reaches ${st.stages[level(s.stats.getValue(n.stat))]} next", style = MaterialTheme.typography.headlineSmall)
        Text("${n.dones} more ${if (n.dones == 1) "Done" else "Dones"} on ${n.habit.name} gets it there.", Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyLarge, color = W.dim)
    }
}

// The bay is the same picture on Today and Shop: same height, same framing, the nameplate in its top corner.
fun bayHeight(lay: Lay) = if (lay.phone) 400.dp else 600.dp

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

// The four attributes by name, each with the level it has reached and how far it is to the next.
@Composable
fun Attributes(s: Data, stacked: Boolean = false) = BoxWithConstraints {
    val twoCols = !stacked && maxWidth >= 600.dp
    val rows = STATS.entries.toList()
    val one = @Composable { e: Map.Entry<String, Stat>, m: Modifier ->
        val p = s.stats.getValue(e.key)
        val l = level(p)
        Row(m.padding(vertical = 6.dp).semantics(mergeDescendants = true) { contentDescription = "${e.value.name}, level $l of 10, ${e.value.stages[l - 1]}" + if (l < 10) ", ${(levelShare(p) * 100).toInt()} percent to level ${l + 1}" else "" }, verticalAlignment = Alignment.CenterVertically) {
            Icon(ICONS.getValue(e.key), null, Modifier.size(20.dp), tint = W.steel)
            Column(Modifier.padding(start = 10.dp).weight(1.1f)) {
                Text(e.value.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                Text(e.value.stages[l - 1], style = MaterialTheme.typography.labelMedium, color = W.dim, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            Gauge(levelShare(p), Modifier.weight(1.4f))
            Text("LV $l", Modifier.padding(start = 12.dp).size(width = 48.dp, height = 22.dp), style = Numeral.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize), color = W.text)
        }
    }
    if (twoCols) Column { rows.chunked(2).forEach { r -> Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) { r.forEach { one(it, Modifier.weight(1f)) } } } }
    else Column { rows.forEach { one(it, Modifier.fillMaxWidth()) } }
}

// A bar charged with the reactor's light: a cyan-to-steel fill on an edge track, a soft halo round the fill and a
// bright spark at its leading edge. The halo spills past the bar's bounds on purpose.
@Composable
fun Gauge(v: Float, modifier: Modifier, h: androidx.compose.ui.unit.Dp = 8.dp) = Canvas(modifier.height(h)) {
    val y = size.height / 2
    val w = size.height
    drawLine(W.edge, Offset(w / 2, y), Offset(size.width - w / 2, y), w, androidx.compose.ui.graphics.StrokeCap.Round)
    val x = w / 2 + (size.width - w) * v.coerceIn(0f, 1f)
    if (v <= 0f) return@Canvas
    for ((k, a) in listOf(3.4f to 0.08f, 2.2f to 0.16f)) drawLine(W.glow, Offset(w / 2, y), Offset(x, y), w * k, androidx.compose.ui.graphics.StrokeCap.Round, alpha = a)
    drawLine(Brush.horizontalGradient(listOf(W.glow, W.steel), 0f, x), Offset(w / 2, y), Offset(x, y), w, androidx.compose.ui.graphics.StrokeCap.Round)
    drawCircle(Brush.radialGradient(listOf(Color.White, W.glow.copy(alpha = 0f)), Offset(x, y), w * 1.6f), w * 1.6f, Offset(x, y))
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
    Button(onClick = { ui.tab = "habits"; ui.edit = "" }, Modifier.height(44.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct)) {
        Icon(ICONS.getValue("plus"), null, Modifier.size(20.dp)); Text("Add your first habit", Modifier.padding(start = 8.dp))
    }
    Text("Or start from one of these:", Modifier.padding(top = 20.dp, bottom = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SUGGEST.forEach { h ->
            androidx.compose.material3.SuggestionChip(onClick = {
                val id = newId()
                store.change { habits += Habit(id, h.name, h.kind, h.per, h.target, h.stat, h.weight, start = LocalDate.now()) }
                ui.tell("${h.name} added") { store.change { habits.removeAll { it.id == id } } }
            }, label = { Text("${h.name} · ${h.target} a ${h.per}") }, Modifier.height(40.dp),
                border = androidx.compose.material3.SuggestionChipDefaults.suggestionChipBorder(true, borderColor = W.edge),
                colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(labelColor = W.text))
        }
    }
}
