package com.luisvilaca.nutsandbolts

import androidx.compose.material3.ripple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.activity.compose.BackHandler
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.os.Build
import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import kotlin.math.min
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.HorizontalDivider

private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)
private val shortFmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK)

@Composable
fun HabitsScreen(store: Store, ui: Ui, lay: Lay) {
    var view by remember { mutableStateOf("day") }
    var month by remember { mutableStateOf(YearMonth.from(ui.date)) }
    val s = store.s
    val d = ui.date
    val t = LocalDate.now()

    val controls = @Composable {
        val views = @Composable { m: Modifier ->
            Segments(listOf("day" to "Day", "cal" to "Calendar", "hist" to "History"), view, m.widthIn(max = 420.dp)) { view = it; month = YearMonth.from(d) }
        }
        // a narrow column gives the view switch its own line
        BoxWithConstraints { val narrow = maxWidth < 560.dp; Column {
            if (narrow) views(Modifier.fillMaxWidth().padding(bottom = 4.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                when (view) {
                    "day" -> Stepper(d.format(shortFmt), { ui.date = d.minusDays(1) }, { ui.date = d.plusDays(1) })
                    "cal" -> Stepper(month.format(monthFmt), { month = month.minusMonths(1) }, { month = month.plusMonths(1) })
                }
                if (view == "day" && d != t) TextButton({ ui.date = t }) { Text("Back to today", color = W.act) }
                if (!narrow) { Spacer(Modifier.weight(1f)); views(Modifier) }
            }
        } }
    }
    val run = s.habits.filter { active(it, d) }
    val records = derived(store) { s.habits.associateWith { record(s, it, t) } }
    val monthP = derived(store, month) { (1..month.lengthOfMonth()).associate { n -> month.atDay(n).let { it to slotProgress(s, it) } } }
    var sel by remember { mutableStateOf<String?>(null) } // the habit in the side panel, on a split screen

    Box(Modifier.fillMaxSize()) {
        // only what overflows scrolls: the calendar always fits the screen, the day and the history scroll if they must
        Column(Modifier.fillMaxSize().padding(horizontal = if (lay.wide) 28.dp else 20.dp)) {
            Header("Habits", store, ui, lay)
            controls()
            if (view == "cal") Column(Modifier.weight(1f).padding(bottom = 8.dp)) {
                Card(Modifier.weight(1f), pad = 12.dp) {
                    Month(month, { day, room -> CalendarMark(monthP.getValue(day), day > t, room) }, ahead = true, Modifier.weight(1f),
                        say = { day -> monthP.getValue(day).let { if (it.slots == 0) "nothing ran" else boltsIn(it, day > t) } }) { ui.date = it; view = "day" }
                }
                Legend(Modifier.padding(top = 8.dp, start = 4.dp))
            }
            // wider than 720dp the day splits: the habits on the left, the one picked in detail on the right
            else BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val split = maxWidth >= 720.dp && view == "day" && run.isNotEmpty()
                val picked = run.find { it.id == sel } ?: run.firstOrNull()
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(if (split) 0.56f else 1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        when {
                            s.habits.isEmpty() -> Empty(store, ui)
                            view == "hist" -> History(s, records)
                            run.isEmpty() -> Note("No habits run on this day.")
                            split -> run.forEach { HabitRow(store, ui, it, d, selected = it === picked, select = { sel = it.id }) }
                            else -> run.forEach { HabitRow(store, ui, it, d) }
                        }
                    }
                    if (split && picked != null) Detail(s, ui, picked, records.getValue(picked), Modifier.weight(0.44f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 88.dp))
                }
            }
        }
        // reachable from every view; a new habit starts on the day picked last
        ExtendedFloatingActionButton(text = { Text("Add habit") }, icon = { Icon(ICONS.getValue("plus"), null, Modifier.size(20.dp)) }, onClick = { ui.openForm("") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp, if (view == "cal") 48.dp else 20.dp).height(48.dp), containerColor = W.act, contentColor = W.onAct)
    }
}

// The one way a count of slots is said: in bolts, driven in or sheared, or planned for a day ahead.
fun boltsIn(p: Progress, ahead: Boolean) =
    if (ahead) "${plural(p.slots, "bolt")} planned" else "${p.done} of ${plural(p.slots, "bolt")} in" + if (p.fail > 0) ", ${p.fail} sheared" else ""

@Composable
fun Stepper(label: String, prev: () -> Unit, next: (() -> Unit)?) = Row(verticalAlignment = Alignment.CenterVertically) {
    IconButton(prev, Modifier.size(40.dp)) { Icon(ICONS.getValue("prev"), "Previous", tint = W.text) }
    Text(label, Modifier.widthIn(min = 120.dp), style = MaterialTheme.typography.titleMedium, maxLines = 1)
    IconButton({ next?.invoke() }, Modifier.size(40.dp), enabled = next != null) { Icon(ICONS.getValue("next"), "Next", tint = if (next != null) W.text else W.edge) }
}

// A month of day tiles that always fills the space it is given, so it never scrolls: each week a ruled row of equal
// height, the day's mark under its number, or beside it when the rows are short. ahead: future days open too.
@Composable
// say: what a day holds, read out with its date; mark gets how much room its cell has: 0 a short row (the mark sits
// beside the number), 1 a cell of 60dp or more, 2 one of 110dp or more
fun Month(m: YearMonth, mark: @Composable (LocalDate, Int) -> Unit, ahead: Boolean, modifier: Modifier = Modifier, say: (LocalDate) -> String = { "" }, open: (LocalDate) -> Unit) {
    val t = LocalDate.now()
    val lead = m.atDay(1).dayOfWeek.value - 1
    val weeks = (lead + m.lengthOfMonth() + 6) / 7
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val compact = (maxHeight - 24.dp) / weeks < 60.dp
        val room = if ((maxHeight - 24.dp) / weeks >= 110.dp) 2 else 1
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(bottom = 4.dp)) { DOW.forEach { Text(it.uppercase(), Modifier.weight(1f).padding(start = 6.dp), style = Label) } }
            (0 until weeks).forEach { w ->
                HorizontalDivider(color = W.line)
                Row(Modifier.weight(1f)) {
                    (0..6).forEach { i ->
                        val n = w * 7 + i - lead + 1
                        if (n < 1 || n > m.lengthOfMonth()) Spacer(Modifier.weight(1f)) else {
                            val d = m.atDay(n)
                            val off = d > t && !ahead
                            // the whole cell takes the tap; the press lights the day's number ring, the cell has no shape of its own
                            val press = remember { MutableInteractionSource() }
                            val cell = Modifier.weight(1f).fillMaxHeight()
                                .clickable(press, null, enabled = !off) { open(d) }.semantics { contentDescription = d.format(dayFmt) + say(d).let { if (it.isEmpty()) "" else ", $it" } }.padding(4.dp)
                            val num = @Composable {
                                Box(Modifier.size(28.dp).clip(CircleShape).indication(press, ripple()).border(2.dp, if (d == t) W.act else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                                    Text("$n", style = MaterialTheme.typography.labelLarge, color = if (off) W.edge else W.text)
                                }
                            }
                            if (compact) Row(cell, verticalAlignment = Alignment.CenterVertically) { num(); Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { mark(d, 0) } }
                            else Column(cell) { num(); Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { mark(d, room) } }
                        }
                    }
                }
            }
        }
    }
}

// A day in the habits calendar: its plate, 42dp in a roomy cell and 26dp otherwise, with "2/3" under it where there is
// room; nothing at all on a day when no habit ran.
@Composable
private fun CalendarMark(p: Progress, future: Boolean, room: Int) {
    if (p.slots == 0) return
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DayMark(p, future, Modifier.size(if (room == 2) 42.dp else 26.dp))
        if (room > 0 && !future) Text("${p.done}/${p.slots}", style = Numeral.copy(fontSize = 12.sp), color = W.dim)
    }
}

// What the plates' holes mean, drawn with the real bolts.
@Composable
private fun Legend(modifier: Modifier) = Row(modifier.semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
    listOf(Outcome.Done to "driven = done", null to "ring = open", Outcome.Fail to "split = sheared").forEach { (st, words) ->
        SlotMark(Slot(st, false), 18.dp)
        Text(words, Modifier.padding(start = 6.dp, end = 18.dp), style = MaterialTheme.typography.labelMedium, color = W.dim)
    }
}

// Every habit's lifetime: its rate and runs, the run bar across its whole life, and its last twelve weeks.
@Composable
private fun History(s: Data, records: Map<Habit, Record>) {
    s.habits.forEach { h ->
        val r = records.getValue(h)
        val pct = if (r.slots > 0) r.done * 100 / r.slots else 0
        val unit = { n: Int -> plural(n, h.per.id) }
        Card(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(ICONS.getValue(h.stat.id), null, Modifier.size(20.dp), tint = W.steel)
                Text(h.name, Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$pct%", style = Numeral, color = W.text)
            }
            Text("${r.done} of ${plural(r.slots, "bolt")} in" + (if (r.fail > 0) " · ${r.fail} sheared" else "") + " · best run ${unit(r.best)}" + (if (r.now > 0) " · ${unit(r.now)} running now" else ""),
                style = MaterialTheme.typography.bodyMedium, color = W.dim)
            RunBar(h, r, Modifier.padding(top = 10.dp))
            Strip(s, h, Modifier.padding(top = 12.dp))
        }
    }
}

// A habit's whole life as a track, one step per day or week since it began: the best run in steel and the current run
// in glow, both reaching to today at the right, so a one-day run reads as one day of a long life, not an empty bar.
@Composable
private fun RunBar(h: Habit, r: Record, modifier: Modifier) = Column(modifier) {
    val unit = { n: Int -> plural(n, h.per.id) }
    Canvas(Modifier.fillMaxWidth().height(8.dp).semantics { contentDescription = "best run ${unit(r.best)}, current ${unit(r.now)}, over ${unit(r.periods)}" }) {
        val step = size.width / maxOf(r.periods, 1)
        val cr = CornerRadius(size.height / 2)
        drawRoundRect(W.edge, Offset.Zero, size, cr)
        drawRoundRect(W.steel, Offset(size.width - step * r.best, 0f), Size(step * r.best, size.height), cr, alpha = 0.55f)
        drawRoundRect(W.glow, Offset(size.width - step * r.now, 0f), Size(step * r.now, size.height), cr)
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text("${unit(r.periods)} ago", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = W.dim)
        Text("today", style = MaterialTheme.typography.labelMedium, color = W.dim)
    }
}

// The last twelve weeks of a habit: a weekly habit as twelve week plates, a daily one as a field of day pips, seven to
// a week, oldest at the left. A pip is steel when the day was full, half steel when partly done, coral with a sheared
// bolt, and an empty ring when nothing went in; days the habit did not run are left blank.
@Composable
private fun Strip(s: Data, h: Habit, modifier: Modifier) {
    val t = LocalDate.now()
    val first = weekStart(t).minusWeeks(11)
    if (h.per == Per.Week) Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (0 until 12L).forEach { w ->
            val d = first.plusWeeks(w)
            val ms = periodMarks(s, h, d)
            // a week before the habit began stays empty
            if (!periodDates(h, d).any { active(h, it) }) Spacer(Modifier.size(28.dp))
            else DayMark(Progress(ms.count { it.second.s == Outcome.Done }, ms.count { it.second.s == Outcome.Fail }, h.target), d > t, Modifier.size(28.dp))
        }
    } else Canvas(modifier.size(14.dp * 12, 14.dp * 7).semantics { contentDescription = "the last twelve weeks of ${h.name}" }) {
        val cw = size.width / 12f; val ch = size.height / 7f
        val rr = min(cw, ch) * 0.34f
        for (w in 0 until 12) for (i in 0 until 7) {
            val d = first.plusDays((w * 7 + i).toLong())
            if (d > t || !active(h, d)) continue
            val c = Offset(cw * (w + 0.5f), ch * (i + 0.5f))
            val ms = s.marks[d]?.get(h.id).orEmpty()
            val done = ms.count { it.s == Outcome.Done }
            when {
                ms.any { it.s == Outcome.Fail } -> drawCircle(W.coral, rr, c)
                done >= h.target -> drawCircle(W.steel, rr, c)
                done > 0 -> { drawCircle(W.edge, rr, c); drawArc(W.steel, 90f, 180f, true, c - Offset(rr, rr), Size(rr * 2, rr * 2)) }
                else -> drawCircle(W.edge, rr - 1f, c, style = Stroke(1.5f))
            }
        }
    }
}

// The picked habit in full: its rate, runs and failures, the run bar, and every day it was logged, newest first.
@Composable
private fun Detail(s: Data, ui: Ui, h: Habit, r: Record, modifier: Modifier) = Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    val t = LocalDate.now()
    val unit = { n: Int -> plural(n, h.per.id) }
    Card(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(ICONS.getValue(h.stat.id), null, Modifier.size(22.dp), tint = W.steel)
            Text(h.name, Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            TextButton({ ui.openForm(h.id) }) { Text("Edit", color = W.act) }
        }
        Text(meta(s, h, t), Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("bolts in" to "${if (r.slots > 0) r.done * 100 / r.slots else 0}%", "running now" to unit(r.now), "best run" to unit(r.best), "sheared" to "${r.fail}")
                .forEach { (k, v) ->
                    Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
                        Text(v, style = Numeral, color = if (k == "sheared" && r.fail > 0) W.coral else W.text, maxLines = 1)
                        Text(k, style = MaterialTheme.typography.labelMedium, color = W.dim)
                    }
                }
        }
        RunBar(h, r, Modifier.padding(top = 16.dp))
    }
    Card(Modifier.fillMaxWidth()) {
        Text("LOG", Modifier.padding(bottom = 8.dp), style = Label)
        val days = s.marks.entries.filter { !it.value[h.id].isNullOrEmpty() }.sortedByDescending { it.key }.take(30)
        if (days.isEmpty()) Text("Nothing logged yet.", style = MaterialTheme.typography.bodyMedium, color = W.dim)
        days.forEach { (day, hs) ->
            val ms = hs.getValue(h.id)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { ui.date = day }.padding(vertical = 6.dp)
                .semantics(mergeDescendants = true) { contentDescription = "${day.format(dayFmt)}: " + ms.joinToString { it.s.id } }, verticalAlignment = Alignment.CenterVertically) {
                Text(day.format(shortFmt), Modifier.width(110.dp), style = MaterialTheme.typography.bodyMedium, color = if (day == t) W.text else W.dim)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { ms.forEach { SlotMark(Slot(it.s, false), 20.dp) } }
                Spacer(Modifier.weight(1f))
                Text(ms.groupingBy { it.s }.eachCount().entries.joinToString(" · ") { (k, n) -> "$n ${mapOf(Outcome.Done to "in", Outcome.Skip to "skipped", Outcome.Fail to "sheared").getValue(k)}" },
                    style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
        }
    }
}

// What the habit as set up pays: each Done (or resist), each fail, the most a period can bring, its nuts, and how
// far the attribute it trains is from its next level.
@Composable
private fun Reward(s: Data, kind: Kind, per: Per, target: Int, weight: Heft, stat: Attr) {
    val w = weight
    val st = stat
    val good = kind == Kind.Build
    FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), itemVerticalAlignment = Alignment.CenterVertically) {
        Coin("bolts", w.bolts)
        Text("+${w.pts} ${st.title}", style = Numeral, color = W.text)
        Text(if (good) "each Done" else "each time you resist", style = MaterialTheme.typography.bodyMedium, color = W.dim)
    }
    Text("${if (good) "A fail" else "Giving in"} takes ${plural(w.bolts, "bolt")} and ${w.pts} ${st.title} back.", Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.bodyMedium, color = W.dim)
    Text("Up to ${plural(w.bolts * target, "bolt")} and ${w.pts * target} ${st.title} a ${per.id}.", Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(ICONS.getValue("nuts"), null, Modifier.size(16.dp), tint = W.gold)
        Text(if (per == Per.Week) " A nut for every week completed." else " A nut for every third day in a row.", style = MaterialTheme.typography.bodyMedium, color = W.dim)
    }
    val p = s.stats.getValue(stat)
    val l = level(p)
    if (l < LEVEL_AT.size) {
        val n = (LEVEL_AT[l] - p + w.pts - 1) / w.pts
        Text("${st.title} is at ${st.stages[l - 1]}: $n more ${if (n == 1) "Done reaches" else "Dones reach"} ${st.stages[l]}.", Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium, color = W.text)
    }
}

private fun LocalDate.millis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.date() = java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitForm(store: Store, ui: Ui, lay: Lay, h: Habit?) {
    val s = store.s
    // every field survives the system ending the app in the background (dates and day sets are serializable)
    var name by rememberSaveable { mutableStateOf(h?.name ?: "") }
    var kind by rememberSaveable { mutableStateOf(h?.kind ?: Kind.Build) }
    var target by rememberSaveable { mutableStateOf(h?.target ?: 1) }
    var per by rememberSaveable { mutableStateOf(h?.per ?: Per.Day) }
    var weight by rememberSaveable { mutableStateOf(h?.weight ?: Heft.Medium) }
    var stat by rememberSaveable { mutableStateOf(h?.stat ?: Attr.Battery) }
    // an undated habit has run since its first mark
    var start by rememberSaveable { mutableStateOf(h?.start ?: h?.let { x -> s.marks.keys.filter { s.marks[it]!!.containsKey(x.id) }.minOrNull() } ?: ui.date) }
    var end by rememberSaveable { mutableStateOf(h?.end) }
    var ends by rememberSaveable { mutableStateOf(h?.end != null) }
    var days by rememberSaveable { mutableStateOf(HashSet(h?.days ?: (0..6).toSet())) }
    var remind by rememberSaveable { mutableStateOf(h?.remind) }
    var clock by rememberSaveable { mutableStateOf(false) }
    val notify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!ok) ui.tell("Notifications are off for Nuts And Bolts, so the reminder cannot show.")
    }
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    val problem = when {
        name.isBlank() -> "Give the habit a name."
        days.isEmpty() -> "Pick at least one day of the week."
        ends && end == null -> "Pick the day it ends, or let it run indefinitely."
        ends && end!! < start -> "The end day comes before the start day."
        else -> null
    }
    val save = {
        Sfx.play(if (h == null) "add" else "equip")
        store.change {
            val x = h ?: Habit(newId(), "").also { habits += it }
            x.name = name.trim(); x.kind = kind; x.per = per; x.target = target; x.weight = weight; x.stat = stat
            x.start = start; x.end = if (ends) end else null; x.days = days.takeIf { it.size < 7 }?.toSet(); x.remind = remind
        }
        ui.edit = null
    }
    val seg = @Composable { opts: List<Pair<String, String>>, cur: String, set: (String) -> Unit ->
        // the choices slide; past two options a narrow column lays them out two to a row instead
        BoxWithConstraints {
            if (opts.size > 2 && maxWidth < 420.dp) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opts.chunked(2).forEach { r ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.forEach { (k, v) ->
                            FilterChip(cur == k, { set(k) }, { Text(v, Modifier.padding(vertical = 8.dp)) }, Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = W.selFill, selectedLabelColor = W.text, labelColor = W.text),
                                border = FilterChipDefaults.filterChipBorder(true, cur == k, borderColor = W.edge, selectedBorderColor = W.sel, selectedBorderWidth = 1.5.dp))
                        }
                        if (r.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else Segments(opts, cur) { set(it) }
        }
    }
    val group = @Composable { label: String, content: @Composable () -> Unit ->
        Card(Modifier.fillMaxWidth()) {
            Text(label.uppercase(), Modifier.padding(bottom = 10.dp), style = Label)
            content()
        }
    }
    val hint = @Composable { text: String -> Text(text, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim) }

    val left = @Composable {
        group("Name") { Field(name, { name = it.take(40) }, "Read 20 minutes") }
        group("Type") {
            seg(listOf(Kind.Build.id to "Build it", Kind.Break.id to "Break it"), kind.id) { kind = byId(it) }
            hint("Build it: you earn each time you do it. Break it: you earn each time you resist it.")
        }
        group("Attribute it trains") { seg(Attr.entries.map { it.id to "${it.title}\n${it.gloss}" }, stat.id) { stat = byId(it) } }
        group("Schedule") {
            // wraps rather than squeezes, so at large text Day / Week drops to its own line instead of breaking a word
            FlowRow(itemVerticalAlignment = Alignment.CenterVertically, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton({ target = (target - 1).coerceAtLeast(1) }) { Icon(ICONS.getValue("minus"), "Fewer", tint = W.text) }
                Text("$target", Modifier.widthIn(min = 28.dp), style = Numeral, color = W.text)
                IconButton({ target = (target + 1).coerceAtMost(maxTarget(per)) }, enabled = target < maxTarget(per)) {
                    Icon(ICONS.getValue("plus"), "More", tint = if (target < maxTarget(per)) W.text else W.edge)
                }
                Text("times a", Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodyLarge)
                Box(Modifier.width(220.dp)) { seg(listOf(Per.Day.id to "Day", Per.Week.id to "Week"), per.id) { per = byId(it); target = target.coerceAtMost(maxTarget(per)) } }
            }
            FlowRow(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                Text("From", style = MaterialTheme.typography.bodyLarge)
                OutlinedButton({ picking = "start" }, border = BorderStroke(1.dp, W.edge)) { Text(start.format(shortFmt), color = W.text) }
                Text("until", style = MaterialTheme.typography.bodyLarge)
                Box(Modifier.widthIn(max = 320.dp)) { seg(listOf("never" to "Indefinitely", "on" to "A set day"), if (ends) "on" else "never") { ends = it == "on" } }
                if (ends) OutlinedButton({ picking = "end" }, border = BorderStroke(1.dp, W.edge)) { Text(end?.format(shortFmt) ?: "Pick the day", color = W.text) }
            }
            FlowRow(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DOW.forEachIndexed { i, n ->
                    FilterChip(i in days, { days = HashSet(if (i in days) days - i else days + i) }, { Text(n) },
                        leadingIcon = if (i in days) ({ Icon(ICONS.getValue("check"), null, Modifier.size(16.dp), tint = W.steel) }) else null,
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = W.selFill, selectedLabelColor = W.text, labelColor = W.text),
                        border = FilterChipDefaults.filterChipBorder(true, i in days, borderColor = W.edge, selectedBorderColor = W.sel, selectedBorderWidth = 1.5.dp))
                }
            }
        }
        group("Reminder") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(remind != null, { on ->
                    remind = if (on) remind ?: "09:00" else null
                    if (on && Build.VERSION.SDK_INT >= 33) notify.launch(Manifest.permission.POST_NOTIFICATIONS)
                }, colors = SwitchDefaults.colors(checkedTrackColor = W.act, checkedThumbColor = W.onAct, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.edge))
                Text(if (remind != null) "At" else "Off", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
                OutlinedButton({ clock = true }, enabled = remind != null, border = BorderStroke(1.dp, W.edge)) { Text(remind ?: "09:00", style = Numeral, color = if (remind != null) W.text else W.dim) }
            }
            hint("A notification at this time on the days it runs, while it still has open bolts. Its ${if (kind == Kind.Break) "Resisted" else "Done"} button logs it without opening the app.")
        }
    }
    val actions = @Composable { m: Modifier ->
        Row(m.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (h != null) TextButton({ confirm = true }) { Text("Delete habit", color = W.coral) }
            Spacer(Modifier.weight(1f))
            OutlinedButton({ ui.edit = null }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text("Cancel", color = W.text) }
            Button(save, Modifier.height(44.dp), enabled = problem == null, shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) {
                Text(if (h == null) "Add habit" else "Save changes")
            }
        }
    }
    val right = @Composable { pinned: Boolean ->
        group("Weight") {
            seg(Heft.entries.map { w -> w.id to "${w.title}\n${plural(w.bolts, "bolt")}, ${plural(w.pts, "point")}" }, weight.id) { weight = byId(it) }
            hint("Done pays this much. Failed takes the same amount back.")
        }
        group("Reward") { Reward(s, kind, per, target, weight, stat) }
        // a hint while the name is still empty, an error once something is written
        if (problem != null) Text(problem, Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.bodyMedium, color = if (name.isEmpty()) W.dim else W.coral)
        if (!pinned) actions(Modifier)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = if (lay.wide) 28.dp else 20.dp)) {
        // the form's own bar: back, what it is, and Save, so saving never needs a scroll to the end
        Row(Modifier.fillMaxWidth().heightIn(min = 60.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ ui.edit = null }, Modifier.size(40.dp)) { Icon(ICONS.getValue("prev"), "Back", tint = W.text) }
            Text(if (h == null) "New habit" else "Edit habit", Modifier.padding(start = 8.dp).weight(1f), style = MaterialTheme.typography.displaySmall, maxLines = 1)
            TextButton(save, enabled = problem == null) { Text("Save", color = if (problem == null) W.act else W.dim, style = MaterialTheme.typography.labelLarge) }
        }
        BoxWithConstraints(Modifier.weight(1f)) {
            // wider than 720dp: what the habit is on the left, scrolling; what it pays and the actions on the right, held still
            if (maxWidth >= 720.dp) Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(1.1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { left() }
                Column(Modifier.weight(0.9f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { right(false) }
            } else Column(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f).widthIn(max = 720.dp).verticalScroll(rememberScrollState()).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { left(); right(true) }
                // on a phone Cancel and Save stay pinned above the keyboard
                actions(Modifier.background(W.ground).imePadding().padding(vertical = 10.dp))
            }
        }
    }

    picking?.let { which ->
        val st = rememberDatePickerState(initialSelectedDateMillis = (if (which == "start") start else end ?: start).millis())
        DatePickerDialog(onDismissRequest = { picking = null }, confirmButton = {
            TextButton({ st.selectedDateMillis?.date()?.let { if (which == "start") start = it else end = it }; picking = null }) { Text("OK", color = W.act) }
        }, dismissButton = { TextButton({ picking = null }) { Text("Cancel", color = W.text) } }) { DatePicker(st) }
    }
    if (clock) {
        val (hh, mm) = (remind ?: "09:00").split(":").map { it.toInt() }
        val st = rememberTimePickerState(hh, mm, is24Hour = true)
        AlertDialog(onDismissRequest = { clock = false }, text = {
            TimePicker(st, colors = TimePickerDefaults.colors(selectorColor = W.act, timeSelectorSelectedContainerColor = W.raised, clockDialColor = W.rail))
        }, confirmButton = { TextButton({ remind = "%02d:%02d".format(st.hour, st.minute); clock = false }) { Text("OK", color = W.act) } },
            dismissButton = { TextButton({ clock = false }) { Text("Cancel", color = W.text) } })
    }
    if (confirm && h != null) ConfirmDeleteHabit(store, ui, h) { gone -> confirm = false; if (gone) ui.edit = null }
}
