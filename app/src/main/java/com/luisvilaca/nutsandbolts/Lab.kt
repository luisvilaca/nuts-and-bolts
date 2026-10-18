package com.luisvilaca.nutsandbolts

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.IntrinsicSize
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

// The Lab: Rivet in its bay; beside it the habits running today, scrolling on their own, and the four attributes pinned
// at the bottom right, always in view.
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
        Header("Lab", store, ui, lay, Modifier.padding(horizontal = 28.dp))
        Row(Modifier.fillMaxSize()) {
            BayPanel(store, Modifier.weight(0.46f).fillMaxHeight(), orbits)
            Column(Modifier.weight(0.54f).fillMaxHeight().padding(start = 8.dp, end = 28.dp, top = 12.dp, bottom = 12.dp)) {
                // the list takes the room there is and scrolls; the attributes stay at the bottom right (owner, 2026-10-18)
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
                Attributes(s, Modifier.padding(top = 12.dp))
            }
        }
    } else if (!lay.phone) Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
        // a tablet held upright has the height to keep the attributes pinned too
        Header("Lab", store, ui, lay)
        BayPanel(store, Modifier.fillMaxWidth().height(bayHeight(lay)), orbits)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { habits() }
        Attributes(s, Modifier.padding(top = 12.dp))
    } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Header("Lab", store, ui, lay)
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

// The four attributes as one instrument panel, pinned at the bottom right of the Lab. Where the side is 560dp or
// wider (scaled with the text size), a single row of four dials, each a small pressure gauge like the reactor's: ten
// level ticks round a 270° arc, the way to the next level filled in uranium glow, the level in the middle and the
// attribute's icon in the arc's open foot, with the name and what it means for you (energy, mind, body, heart) beside it. Narrower, one compact line per attribute: icon, name and meaning, a slim gauge
// and the level. A long press repeats the name and meaning in a tooltip.
@Composable
fun Attributes(s: Data, modifier: Modifier = Modifier) = BoxWithConstraints(modifier.fillMaxWidth()) {
    val row = maxWidth >= 560.dp * LocalDensity.current.fontScale // large text gets the lines instead of cut names
    Card(Modifier.fillMaxWidth(), pad = 12.dp) {
        if (row) Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Attr.entries.forEachIndexed { i, st ->
                if (i > 0) Box(Modifier.padding(horizontal = 8.dp).fillMaxHeight(0.7f).width(1.dp).background(W.line))
                AttrTip(st, Modifier.weight(1f)) { Dial(s, st) }
            }
        } else Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Attr.entries.forEach { st -> AttrTip(st, Modifier.fillMaxWidth()) { AttrLine(s, st) } }
        }
    }
}

private fun say(s: Data, st: Attr): String {
    val p = s.stats.getValue(st); val l = level(p)
    return "${st.title}, ${st.gloss}, level $l of 10" + if (l < 10) ", ${(levelShare(p) * 100).toInt()} percent to level ${l + 1}" else ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttrTip(st: Attr, modifier: Modifier, content: @Composable () -> Unit) = Box(modifier) {
    TooltipBox(TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above), tooltip = { PlainTooltip { Text("${st.title}: ${st.gloss}") } },
        state = rememberTooltipState()) { content() }
}

@Composable
private fun Dial(s: Data, st: Attr) = Row(Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = say(s, st) },
    verticalAlignment = Alignment.CenterVertically) {
    val p = s.stats.getValue(st); val l = level(p)
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = 3.5.dp.toPx(); val r = size.minDimension / 2 - w
            val tl = center - Offset(r, r); val box = Size(r * 2, r * 2)
            // the dial's face, its ten level ticks (steel up to the level reached), the track and the glowing way on
            drawCircle(W.rail, r + w, center)
            for (k in 0 until 10) {
                val ang = Math.toRadians(135.0 + 270.0 * k / 9).toFloat()
                val dir = Offset(kotlin.math.cos(ang), kotlin.math.sin(ang))
                drawLine(if (k < l) W.steel else W.edge, center + dir * (r - w * 1.6f), center + dir * (r - w * 2.6f), 1.5.dp.toPx(), StrokeCap.Round)
            }
            drawArc(W.edge, 135f, 270f, false, tl, box, style = Stroke(w, cap = StrokeCap.Round))
            val sweep = 270f * levelShare(p)
            if (sweep > 0f) {
                drawArc(W.glow, 135f, sweep, false, tl, box, alpha = 0.18f, style = Stroke(w * 2.6f, cap = StrokeCap.Round))
                drawArc(W.glow, 135f, sweep, false, tl, box, style = Stroke(w, cap = StrokeCap.Round))
            }
        }
        Text("$l", style = Numeral, color = W.text)
        // the attribute's icon sits in the open quarter at the foot of the arc
        Icon(ICONS.getValue(st.id), null, Modifier.align(Alignment.BottomCenter).size(12.dp), tint = W.steel)
    }
    Column(Modifier.padding(start = 8.dp)) {
        Text(st.title, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp), color = W.text, maxLines = 1, softWrap = false)
        Text(st.gloss.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = W.dim, maxLines = 1, softWrap = false)
    }
}

@Composable
private fun AttrLine(s: Data, st: Attr) = Row(Modifier.fillMaxWidth().heightIn(min = 28.dp).semantics(mergeDescendants = true) { contentDescription = say(s, st) },
    verticalAlignment = Alignment.CenterVertically) {
    val p = s.stats.getValue(st); val l = level(p)
    val k = LocalDensity.current.fontScale // the name and meaning columns grow with the text, so they line up and never cut
    Icon(ICONS.getValue(st.id), null, Modifier.size(16.dp), tint = W.steel)
    Text(st.title, Modifier.padding(start = 8.dp).width(86.dp * k), style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp), color = W.text, maxLines = 1, softWrap = false)
    Text(st.gloss.replaceFirstChar { it.uppercase() }, Modifier.width(56.dp * k), style = MaterialTheme.typography.labelMedium, color = W.dim, maxLines = 1, softWrap = false)
    Gauge(levelShare(p), Modifier.weight(1f).padding(horizontal = 8.dp), 5.dp)
    Text("LV $l", style = Numeral.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize), color = W.text, maxLines = 1, softWrap = false)
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
