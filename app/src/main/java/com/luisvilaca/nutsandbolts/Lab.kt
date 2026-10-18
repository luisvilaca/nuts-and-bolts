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
import androidx.compose.foundation.layout.Spacer
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

// The Lab: Rivet in its bay; beside it the habits running today, which scroll on their own when there are many, and
// under them the four attributes, compact and always in view.
@Composable
fun LabScreen(store: Store, ui: Ui, lay: Lay) {
    val s = store.s
    val t = LocalDate.now()
    val run = s.habits.filter { active(it, t) }
    val habits = @Composable {
        when {
            s.habits.isEmpty() -> Empty(store, ui)
            run.isEmpty() -> Note("No habits run today.")
            else -> run.forEach { HabitRow(store, ui, it, t) }
        }
    }

    if (lay.wide) Column(Modifier.fillMaxSize()) {
        Header("Lab", t.format(dayFmt), store, ui, lay, Modifier.padding(horizontal = 28.dp))
        Row(Modifier.fillMaxSize()) {
            BayPanel(store, Modifier.weight(0.46f).fillMaxHeight(), orbitsFor(s, t))
            Column(Modifier.weight(0.54f).fillMaxHeight().padding(start = 8.dp, end = 28.dp, top = 12.dp, bottom = 12.dp)) {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
                Attributes(s, Modifier.padding(top = 12.dp))
            }
        }
    } else if (!lay.phone) Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
        // a tablet held upright has the height to keep the attributes pinned too
        Header("Lab", null, store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(bayHeight(lay)), orbitsFor(s, t))
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
        Attributes(s, Modifier.padding(top = 12.dp))
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Header("Lab", null, store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(bayHeight(lay)), orbitsFor(s, t))
        Attributes(s, Modifier.padding(vertical = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
    }
}

// The bay is the same picture on the Lab and Shop: same height, same framing, the nameplate in its top corner.
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
            Text(MOOD[mood(s)], style = MaterialTheme.typography.bodyMedium, color = W.dim)
        }
    }
}

// The four attributes, compact: icon, name and level on one line, what it means for you (energy, mind, body, heart)
// beside a slim gauge to the next level. Four across; under 560dp the words give way to the icon and level alone
// (the screen reader still names them), and under 400dp they go two by two with their words back.
@Composable
fun Attributes(s: Data, modifier: Modifier = Modifier) = BoxWithConstraints(modifier.fillMaxWidth()) {
    val cols = if (maxWidth >= 400.dp) 4 else 2
    val named = maxWidth >= 560.dp || cols == 2
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        STATS.entries.chunked(cols).forEach { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                r.forEach { (k, st) ->
                    val p = s.stats.getValue(k)
                    val l = level(p)
                    Card(Modifier.weight(1f).semantics(mergeDescendants = true) {
                        contentDescription = "${st.name}, ${st.gloss}, level $l of 10" + if (l < 10) ", ${(levelShare(p) * 100).toInt()} percent to level ${l + 1}" else ""
                    }, pad = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(ICONS.getValue(k), null, Modifier.size(16.dp), tint = W.steel)
                            if (named) Text(st.name, Modifier.padding(start = 6.dp).weight(1f), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            else Spacer(Modifier.weight(1f))
                            Text("LV $l", style = Numeral.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize), color = W.text)
                        }
                        if (named) Text(st.gloss.replaceFirstChar { it.uppercase() }, Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelMedium, color = W.dim)
                        Gauge(levelShare(p), Modifier.fillMaxWidth().padding(top = 8.dp), 4.dp)
                    }
                }
            }
        }
    }
}

// A bar charged with the reactor's light: a green-to-steel fill on an edge track, a soft halo round the fill and a
// bright spark at its leading edge. The halo spills past the bar's bounds on purpose.
@Composable
fun Gauge(v: Float, modifier: Modifier, h: androidx.compose.ui.unit.Dp = 8.dp) = Canvas(modifier.height(h)) {
    val y = size.height / 2
    val w = size.height
    drawLine(W.edge, Offset(w / 2, y), Offset(size.width - w / 2, y), w, androidx.compose.ui.graphics.StrokeCap.Round)
    val x = w / 2 + (size.width - w) * v.coerceIn(0f, 1f)
    if (v <= 0f) return@Canvas
    for ((k, a) in listOf(2.6f to 0.06f, 1.8f to 0.12f)) drawLine(W.glow, Offset(w / 2, y), Offset(x, y), w * k, androidx.compose.ui.graphics.StrokeCap.Round, alpha = a)
    drawLine(Brush.horizontalGradient(listOf(W.glow, W.steel), 0f, x), Offset(w / 2, y), Offset(x, y), w, androidx.compose.ui.graphics.StrokeCap.Round)
    drawCircle(Brush.radialGradient(listOf(Color.White, W.glow.copy(alpha = 0f)), Offset(x, y), w * 1.2f), w * 1.2f, Offset(x, y))
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
