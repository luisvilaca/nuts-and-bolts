package com.luisvilaca.nutsandbolts

import androidx.activity.compose.BackHandler
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

private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)
private val shortFmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK)

@Composable
fun HabitsScreen(store: Store, ui: Ui, lay: Lay) {
    var view by remember { mutableStateOf("day") }
    var month by remember { mutableStateOf(YearMonth.from(ui.date)) }
    if (ui.edit != null) {
        BackHandler { ui.edit = null }
        Form(store, ui, lay, store.s.habits.find { it.id == ui.edit })
        return
    }
    val s = store.s
    val d = ui.date
    val t = LocalDate.now()
    val p = slotProgress(s, d)

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
    var sel by remember { mutableStateOf<String?>(null) } // the habit in the side panel, on a split screen

    Box(Modifier.fillMaxSize()) {
        // only what overflows scrolls: the calendar always fits the screen, the day and the history scroll if they must
        Column(Modifier.fillMaxSize().padding(horizontal = if (lay.wide) 28.dp else 20.dp)) {
            Header("Habits", summary(s, d, p).takeIf { lay.wide }, store, ui, lay)
            if (!lay.wide) Text(summary(s, d, p), Modifier.padding(bottom = 4.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
            controls()
            if (view == "cal") Card(Modifier.weight(1f).padding(bottom = 8.dp), pad = 12.dp) { Month(month, { day, roomy -> DayMark(slotProgress(s, day), day > t, Modifier.size(if (roomy) 42.dp else 26.dp)) }, ahead = true, Modifier.weight(1f),
                say = { day -> boltsIn(slotProgress(s, day), day > t) }) { ui.date = it; view = "day" } }
            // wider than 720dp the day splits: the habits on the left, the one picked in detail on the right
            else BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val split = maxWidth >= 720.dp && view == "day" && run.isNotEmpty()
                val picked = run.find { it.id == sel } ?: run.firstOrNull()
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(if (split) 0.56f else 1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        when {
                            s.habits.isEmpty() -> Empty(store, ui)
                            view == "hist" -> History(s)
                            run.isEmpty() -> Note("No habits run on this day.")
                            split -> run.forEach { HabitRow(store, ui, it, d, selected = it === picked, select = { sel = it.id }) }
                            else -> run.forEach { HabitRow(store, ui, it, d) }
                        }
                    }
                    if (split && picked != null) Detail(s, ui, picked, Modifier.weight(0.44f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 88.dp))
                }
            }
        }
        if (view != "cal") ExtendedFloatingActionButton(text = { Text("Add habit") }, icon = { Icon(ICONS.getValue("plus"), null, Modifier.size(20.dp)) }, onClick = { ui.edit = "" },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).height(48.dp), containerColor = W.act, contentColor = W.onAct)
    }
}

private fun summary(s: Data, d: LocalDate, p: Progress) = when {
    s.habits.isEmpty() -> "No habits yet"
    else -> boltsIn(p, d > LocalDate.now())
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
// say: what a day holds, read out with its date; mark gets whether the cell has room for a large mark
fun Month(m: YearMonth, mark: @Composable (LocalDate, Boolean) -> Unit, ahead: Boolean, modifier: Modifier = Modifier, say: (LocalDate) -> String = { "" }, open: (LocalDate) -> Unit) {
    val t = LocalDate.now()
    val lead = m.atDay(1).dayOfWeek.value - 1
    val weeks = (lead + m.lengthOfMonth() + 6) / 7
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val compact = (maxHeight - 24.dp) / weeks < 60.dp
        val roomy = (maxHeight - 24.dp) / weeks >= 110.dp
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(bottom = 4.dp)) { DOW.forEach { Text(it.uppercase(), Modifier.weight(1f).padding(start = 6.dp), style = Label) } }
            (0 until weeks).forEach { w ->
                androidx.compose.material3.HorizontalDivider(color = W.line)
                Row(Modifier.weight(1f)) {
                    (0..6).forEach { i ->
                        val n = w * 7 + i - lead + 1
                        if (n < 1 || n > m.lengthOfMonth()) Spacer(Modifier.weight(1f)) else {
                            val d = m.atDay(n)
                            val off = d > t && !ahead
                            val cell = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !off) { open(d) }.semantics { contentDescription = d.format(dayFmt) + say(d).let { if (it.isEmpty()) "" else ", $it" } }.padding(4.dp)
                            val num = @Composable {
                                Box(Modifier.size(28.dp).border(2.dp, if (d == t) W.act else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                                    Text("$n", style = MaterialTheme.typography.labelLarge, color = if (off) W.edge else W.text)
                                }
                            }
                            if (compact) Row(cell, verticalAlignment = Alignment.CenterVertically) { num(); Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { mark(d, false) } }
                            else Column(cell) { num(); Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { mark(d, roomy) } }
                        }
                    }
                }
            }
        }
    }
}

// Every habit's lifetime. A run is drawn exactly its length: one step per day or week.
@Composable
private fun History(s: Data) {
    val t = LocalDate.now()
    s.habits.forEach { h ->
        val r = record(s, h, t)
        val pct = if (r.slots > 0) r.done * 100 / r.slots else 0
        val unit = { n: Int -> plural(n, h.per) }
        Card(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(ICONS.getValue(h.stat), null, Modifier.size(20.dp), tint = W.steel)
                Text(h.name, Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$pct%", style = Numeral, color = W.text)
            }
            Text("${r.done} of ${plural(r.slots, "bolt")} in" + (if (r.fail > 0) " · ${r.fail} sheared" else "") + " · best run ${unit(r.best)}" + (if (r.now > 0) " · ${unit(r.now)} running now" else ""),
                style = MaterialTheme.typography.bodyMedium, color = W.dim)
            Canvas(Modifier.fillMaxWidth().height(14.dp).padding(top = 6.dp).semantics { contentDescription = "best run ${unit(r.best)}, current ${unit(r.now)}" }) {
                val step = minOf(14f * density, size.width / maxOf(r.best, 1))
                drawRoundRect(W.line, Offset.Zero, Size(size.width, size.height), CornerRadius(size.height / 2))
                drawRoundRect(W.steel, Offset.Zero, Size(step * r.best, size.height), CornerRadius(size.height / 2), alpha = 0.55f)
                drawRoundRect(W.glow, Offset.Zero, Size(step * r.now, size.height), CornerRadius(size.height / 2))
            }
        }
    }
}

// The picked habit in full: its rate, runs and failures, the run bar, and every day it was logged, newest first.
@Composable
private fun Detail(s: Data, ui: Ui, h: Habit, modifier: Modifier) = Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    val t = LocalDate.now()
    val r = record(s, h, t)
    val unit = { n: Int -> plural(n, h.per) }
    Card(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(ICONS.getValue(h.stat), null, Modifier.size(22.dp), tint = W.steel)
            Text(h.name, Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            TextButton({ ui.edit = h.id }) { Text("Edit", color = W.act) }
        }
        Text(meta(s, h, t), Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        Row(Modifier.fillMaxWidth().padding(top = 16.dp)) {
            listOf("bolts in" to "${if (r.slots > 0) r.done * 100 / r.slots else 0}%", "running now" to unit(r.now), "best run" to unit(r.best), "sheared" to "${r.fail}")
                .forEach { (k, v) ->
                    Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
                        Text(v, style = Numeral, color = if (k == "sheared" && r.fail > 0) W.coral else W.text, maxLines = 1)
                        Text(k, style = MaterialTheme.typography.labelMedium, color = W.dim)
                    }
                }
        }
        Gauge(if (r.best > 0) r.now.toFloat() / r.best else 0f, Modifier.fillMaxWidth().padding(top = 16.dp))
        Text("The current run against the best", Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelMedium, color = W.dim)
    }
    Card(Modifier.fillMaxWidth()) {
        Text("LOG", Modifier.padding(bottom = 8.dp), style = Label)
        val days = s.marks.entries.filter { !it.value[h.id].isNullOrEmpty() }.sortedByDescending { it.key }.take(30)
        if (days.isEmpty()) Text("Nothing logged yet.", style = MaterialTheme.typography.bodyMedium, color = W.dim)
        days.forEach { (day, hs) ->
            val ms = hs.getValue(h.id)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { ui.date = day }.padding(vertical = 6.dp)
                .semantics(mergeDescendants = true) { contentDescription = "${day.format(dayFmt)}: " + ms.joinToString { it.s } }, verticalAlignment = Alignment.CenterVertically) {
                Text(day.format(shortFmt), Modifier.width(110.dp), style = MaterialTheme.typography.bodyMedium, color = if (day == t) W.text else W.dim)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { ms.forEach { SlotMark(Slot(it.s, false), 20.dp) } }
                Spacer(Modifier.weight(1f))
                Text(ms.groupingBy { it.s }.eachCount().entries.joinToString(" · ") { (k, n) -> "$n ${mapOf("done" to "in", "skip" to "skipped", "fail" to "sheared").getValue(k)}" },
                    style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
        }
    }
}

// What the habit as set up pays: each Done (or resist), each fail, the most a period can bring, its nuts, and how
// far the attribute it trains is from its next level.
@Composable
private fun Reward(s: Data, kind: String, per: String, target: Int, weight: String, stat: String) {
    val w = WEIGHTS.getValue(weight)
    val st = STATS.getValue(stat)
    val good = kind == "good"
    FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), itemVerticalAlignment = Alignment.CenterVertically) {
        Coin("bolts", w.bolts)
        Text("+${w.pts} ${st.name}", style = Numeral, color = W.text)
        Text(if (good) "each Done" else "each time you resist", style = MaterialTheme.typography.bodyMedium, color = W.dim)
    }
    Text("${if (good) "A fail" else "Giving in"} takes ${plural(w.bolts, "bolt")} and ${w.pts} ${st.name} back.", Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.bodyMedium, color = W.dim)
    Text("Up to ${plural(w.bolts * target, "bolt")} and ${w.pts * target} ${st.name} a $per.", Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(ICONS.getValue("nuts"), null, Modifier.size(16.dp), tint = W.gold)
        Text(if (per == "week") " A nut for every week completed." else " A nut for every third day in a row.", style = MaterialTheme.typography.bodyMedium, color = W.dim)
    }
    val p = s.stats.getValue(stat)
    val l = level(p)
    if (l < LEVEL_AT.size) {
        val n = (LEVEL_AT[l] - p + w.pts - 1) / w.pts
        Text("${st.name} is at ${st.stages[l - 1]}: $n more ${if (n == 1) "Done reaches" else "Dones reach"} ${st.stages[l]}.", Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium, color = W.text)
    }
}

private fun LocalDate.millis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.date() = java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Form(store: Store, ui: Ui, lay: Lay, h: Habit?) {
    val s = store.s
    var name by remember { mutableStateOf(h?.name ?: "") }
    var kind by remember { mutableStateOf(h?.kind ?: "good") }
    var target by remember { mutableStateOf(h?.target ?: 1) }
    var per by remember { mutableStateOf(h?.per ?: "day") }
    var weight by remember { mutableStateOf(h?.weight ?: "medium") }
    var stat by remember { mutableStateOf(h?.stat ?: "battery") }
    // an undated habit has run since its first mark
    var start by remember { mutableStateOf(h?.start ?: h?.let { x -> s.marks.keys.filter { s.marks[it]!!.containsKey(x.id) }.minOrNull() } ?: ui.date) }
    var end by remember { mutableStateOf(h?.end) }
    var ends by remember { mutableStateOf(h?.end != null) }
    var days by remember { mutableStateOf(h?.days ?: (0..6).toSet()) }
    var picking by remember { mutableStateOf<String?>(null) }
    var confirm by remember { mutableStateOf(false) }
    val problem = when {
        name.isBlank() -> "Give the habit a name."
        days.isEmpty() -> "Pick at least one day of the week."
        ends && end == null -> "Pick the day it ends, or let it run indefinitely."
        ends && end!! < start -> "The end day comes before the start day."
        else -> null
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
            seg(listOf("good" to "Build it", "bad" to "Break it"), kind) { kind = it }
            hint("Build it: you earn each time you do it. Break it: you earn each time you resist it.")
        }
        group("Attribute it trains") { seg(STATS.map { (k, v) -> k to "${v.name}\n${v.gloss}" }, stat) { stat = it } }
        group("Schedule") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton({ target = (target - 1).coerceAtLeast(1) }) { Icon(ICONS.getValue("minus"), "Fewer", tint = W.text) }
                Text("$target", Modifier.widthIn(min = 28.dp), style = Numeral, color = W.text)
                IconButton({ target = (target + 1).coerceAtMost(7) }) { Icon(ICONS.getValue("plus"), "More", tint = W.text) }
                Text("times a", Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodyLarge)
                Box(Modifier.widthIn(max = 240.dp)) { seg(listOf("day" to "Day", "week" to "Week"), per) { per = it } }
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
                    FilterChip(i in days, { days = if (i in days) days - i else days + i }, { Text(n) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = W.selFill, selectedLabelColor = W.text, labelColor = W.text),
                        border = FilterChipDefaults.filterChipBorder(true, i in days, borderColor = W.edge, selectedBorderColor = W.sel, selectedBorderWidth = 1.5.dp))
                }
            }
        }
    }
    val right = @Composable {
        group("Weight") {
            seg(WEIGHTS.map { (k, w) -> k to "${w.name}\n${plural(w.bolts, "bolt")}, ${plural(w.pts, "point")}" }, weight) { weight = it }
            hint("Done pays this much. Failed takes the same amount back.")
        }
        group("Reward") { Reward(s, kind, per, target, weight, stat) }
        // a hint while the name is still empty, an error once something is written
        if (problem != null) Text(problem, Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.bodyMedium, color = if (name.isEmpty()) W.dim else W.coral)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (h != null) TextButton({ confirm = true }) { Text("Delete habit", color = W.coral) }
            Spacer(Modifier.weight(1f))
            OutlinedButton({ ui.edit = null }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text("Cancel", color = W.text) }
            Button({
                store.change {
                    val x = h ?: Habit(newId(), "").also { habits += it }
                    x.name = name.trim(); x.kind = kind; x.per = per; x.target = target; x.weight = weight; x.stat = stat
                    x.start = start; x.end = if (ends) end else null; x.days = days.takeIf { it.size < 7 }
                }
                ui.edit = null
            }, Modifier.height(44.dp), enabled = problem == null, shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) {
                Text(if (h == null) "Add habit" else "Save changes")
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = if (lay.wide) 28.dp else 20.dp).padding(bottom = 32.dp)) {
        Header(if (h == null) "New habit" else "Edit habit", null, store, ui, lay)
        // wider than 720dp: what the habit is on the left, what it pays and the actions on the right
        BoxWithConstraints {
            if (maxWidth >= 720.dp) Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(12.dp)) { left() }
                Column(Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(12.dp)) { right() }
            } else Column(Modifier.widthIn(max = 720.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { left(); right() }
        }
    }

    picking?.let { which ->
        val st = rememberDatePickerState(initialSelectedDateMillis = (if (which == "start") start else end ?: start).millis())
        DatePickerDialog(onDismissRequest = { picking = null }, confirmButton = {
            TextButton({ st.selectedDateMillis?.date()?.let { if (which == "start") start = it else end = it }; picking = null }) { Text("OK", color = W.act) }
        }, dismissButton = { TextButton({ picking = null }) { Text("Cancel", color = W.text) } }) { DatePicker(st) }
    }
    if (confirm && h != null) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("Delete “${h.name}”?") }, text = { Text("Its frame comes off the bench. What it already earned stays.") },
        confirmButton = { TextButton({ store.change { habits.remove(h) }; confirm = false; ui.edit = null }) { Text("Delete", color = W.coral) } },
        dismissButton = { TextButton({ confirm = false }) { Text("Keep it", color = W.text) } })
}
