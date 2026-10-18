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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp

// A day's progress over the slots its rows draw (a weekly habit's slots included), so Today, Habits, the week and
// the calendar all count the same thing.
fun slotProgress(s: Data, d: LocalDate) = orbitsFor(s, d).flatMap { it.slots }.let { a -> Progress(a.count { it.s == Outcome.Done }, a.count { it.s == Outcome.Fail }, a.size) }

private val MOOD = listOf("Low power", "Running steady", "Fully charged")

// The Lab: Rivet in its bay; beside it the habits running today and, right after them, the four attributes. When the
// list is too long for the room, it scrolls on its own and the attributes stay in view at the foot.
@Composable
fun LabScreen(store: Store, ui: Ui, lay: Lay) {
    val s = store.s
    val t = LocalDate.now()
    val run = s.habits.filter { active(it, t) }
    val orbits = derived(store) { orbitsFor(s, t) }
    val habits = @Composable {
        when {
            s.habits.isEmpty() -> Empty(store, ui)
            run.isEmpty() -> Note("No habits run today.")
            else -> run.forEach { HabitRow(store, ui, it, t, cue = false) }
        }
    }

    if (lay.wide) Column(Modifier.fillMaxSize()) {
        Header("Lab", t.format(dayFmt), store, ui, lay, Modifier.padding(horizontal = 28.dp))
        Row(Modifier.fillMaxSize()) {
            BayPanel(store, Modifier.weight(0.46f).fillMaxHeight(), orbits)
            Column(Modifier.weight(0.54f).fillMaxHeight().padding(start = 8.dp, end = 28.dp, top = 12.dp, bottom = 12.dp)) {
                // the list takes what it needs and scrolls only past the room there is; the attributes follow it
                Column(Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
                Attributes(s, Modifier.padding(top = 12.dp))
            }
        }
    } else if (!lay.phone) Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
        // a tablet held upright has the height to keep the attributes pinned too
        Header("Lab", null, store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(bayHeight(lay)), orbits)
        Column(Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
        Attributes(s, Modifier.padding(top = 12.dp))
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Header("Lab", null, store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(bayHeight(lay)), orbits)
        Attributes(s, Modifier.padding(vertical = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
    }
}

// The bay is the same picture on the Lab and Shop: same height, same framing, the nameplate in its top corner.
fun bayHeight(lay: Lay) = if (lay.phone) 400.dp else 600.dp

@Composable
fun BayPanel(store: Store, modifier: Modifier, orbits: List<Orbit>, equip: Map<String, String?> = store.s.equip) {
    val s = store.s
    var shown by remember { mutableStateOf(true) }
    val tm = clock(shown)
    Box(modifier.padding(12.dp).clip(RoundedCornerShape(20.dp)).onGloballyPositioned { shown = it.boundsInWindow().height > 0f }) {
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = "${s.name}, ${MOOD[mood(s)].lowercase()}" }) {
            room(s, tm.value, store.react, orbits, store.fresh, equip, band = 72.dp.toPx())
        }
        Column(Modifier.padding(20.dp).clickable(onClickLabel = "wave") { store.react("wave") }) {
            Text(s.name.uppercase(), style = Label.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize, color = W.steel))
            Text(MOOD[mood(s)], style = MaterialTheme.typography.bodyMedium, color = W.dim)
        }
    }
}

// The four attributes as cards: icon, name and level on one line over a gauge to the next level. A name is never cut:
// four across a card shows it in full (13sp) where there is room, else only the icon and level; two by two under
// 400dp. What each means for you (energy, mind, body, heart) is in the screen reader and a long-press tooltip.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Attributes(s: Data, modifier: Modifier = Modifier) = BoxWithConstraints(modifier.fillMaxWidth()) {
    val cols = if (maxWidth >= 400.dp) 4 else 2
    val named = cols == 2 || maxWidth >= 600.dp
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Attr.entries.chunked(cols).forEach { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                r.forEach { st ->
                    val p = s.stats.getValue(st)
                    val l = level(p)
                    Box(Modifier.weight(1f)) { TooltipBox(TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above), tooltip = { PlainTooltip { Text("${st.title}: ${st.gloss}") } },
                        state = rememberTooltipState()) {
                        Card(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                            contentDescription = "${st.title}, ${st.gloss}, level $l of 10" + if (l < 10) ", ${(levelShare(p) * 100).toInt()} percent to level ${l + 1}" else ""
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(ICONS.getValue(st.id), null, Modifier.size(16.dp), tint = W.steel)
                                if (named) Text(st.title, Modifier.padding(start = 6.dp).weight(1f), style = MaterialTheme.typography.labelMedium, color = W.text, maxLines = 1, softWrap = false)
                                else Spacer(Modifier.weight(1f))
                                Text("LV $l", style = Numeral.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize), color = W.text)
                            }
                            Gauge(levelShare(p), Modifier.fillMaxWidth().padding(top = 10.dp), 6.dp)
                        }
                    } }
                }
            }
        }
    }
}

// A bar charged with the reactor's light: a green-to-steel fill on an edge track, a soft halo round the fill and a
// bright spark at its leading edge. The halo spills past the bar's bounds on purpose.
@Composable
fun Gauge(v: Float, modifier: Modifier, h: Dp = 8.dp) = Canvas(modifier.height(h)) {
    val y = size.height / 2
    val w = size.height
    drawLine(W.edge, Offset(w / 2, y), Offset(size.width - w / 2, y), w, StrokeCap.Round)
    val x = w / 2 + (size.width - w) * v.coerceIn(0f, 1f)
    if (v <= 0f) return@Canvas
    for ((k, a) in listOf(2.6f to 0.06f, 1.8f to 0.12f)) drawLine(W.glow, Offset(w / 2, y), Offset(x, y), w * k, StrokeCap.Round, alpha = a)
    drawLine(Brush.horizontalGradient(listOf(W.glow, W.steel), 0f, x), Offset(w / 2, y), Offset(x, y), w, StrokeCap.Round)
    drawCircle(Brush.radialGradient(listOf(Color.White, W.glow.copy(alpha = 0f)), Offset(x, y), w * 1.2f), w * 1.2f, Offset(x, y))
}

private val SUGGEST = listOf(
    Habit("", "Drink water", per = Per.Day, target = 3, stat = Attr.Battery, weight = Heft.Light),
    Habit("", "Read 20 minutes", per = Per.Day, target = 1, stat = Attr.Processor, weight = Heft.Medium),
    Habit("", "Workout", per = Per.Week, target = 3, stat = Attr.Chassis, weight = Heft.Heavy),
    Habit("", "Call a friend", per = Per.Week, target = 1, stat = Attr.Core, weight = Heft.Medium),
    Habit("", "No phone in bed", kind = Kind.Break, per = Per.Day, target = 1, stat = Attr.Battery, weight = Heft.Medium),
)

fun newId() = "h" + System.currentTimeMillis().toString(36) + (0..999).random()

@Composable
fun Empty(store: Store, ui: Ui) = Column(Modifier.padding(vertical = 16.dp)) {
    Text("The bench is empty", style = MaterialTheme.typography.headlineSmall)
    Text("Each habit becomes a frame on ${store.s.name}'s bench. Every time you do it, a bolt goes in.",
        Modifier.padding(top = 8.dp, bottom = 16.dp), style = MaterialTheme.typography.bodyLarge, color = W.dim)
    Button(onClick = { ui.openForm("") }, Modifier.height(44.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct)) {
        Icon(ICONS.getValue("plus"), null, Modifier.size(20.dp)); Text("Add your first habit", Modifier.padding(start = 8.dp))
    }
    FlowRow(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SUGGEST.forEach { h ->
            SuggestionChip(onClick = {
                val id = newId()
                store.change { addSuggested(this, h, id, LocalDate.now()) }
                ui.tell("${h.name} added") { store.change { removeHabit(this, habits.first { it.id == id }) } }
            }, label = { Text("${h.name} · ${h.target} a ${h.per.id}") }, Modifier.height(40.dp),
                icon = { Icon(ICONS.getValue(h.stat.id), null, Modifier.size(18.dp), tint = W.steel) },
                border = SuggestionChipDefaults.suggestionChipBorder(true, borderColor = W.edge),
                colors = SuggestionChipDefaults.suggestionChipColors(labelColor = W.text))
        }
    }
}
