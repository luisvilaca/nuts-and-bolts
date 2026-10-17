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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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

    val body = @Composable {
        val views = @Composable { m: Modifier ->
            SingleChoiceSegmentedButtonRow(m) {
                listOf("day" to "Day", "cal" to "Calendar", "hist" to "History").forEachIndexed { i, (k, v) ->
                    SegmentedButton(view == k, { view = k; month = YearMonth.from(d) }, SegmentedButtonDefaults.itemShape(i, 3), icon = {},
                        colors = segColors()) { Text(v, maxLines = 1) }
                }
            }
        }
        // a narrow column gives the view switch its own line
        BoxWithConstraints { val narrow = maxWidth < 560.dp; Column {
        if (narrow) views(Modifier.fillMaxWidth().padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            when (view) {
                "day" -> Stepper(d.format(shortFmt), { ui.date = d.minusDays(1) }, { ui.date = d.plusDays(1) })
                "cal" -> Stepper(month.format(monthFmt), { month = month.minusMonths(1) }, { month = month.plusMonths(1) })
            }
            if (view == "day" && d != t) TextButton({ ui.date = t }) { Text("Back to today", color = W.act) }
            if (!narrow) { Spacer(Modifier.weight(1f)); views(Modifier) }
        }
        } }
        val run = s.habits.filter { active(it, d) }
        when {
            view == "cal" -> Month(month, { DayMark(slotProgress(s, it), it > t, Modifier.size(26.dp)) }, ahead = true) { ui.date = it; view = "day" }
            s.habits.isEmpty() -> Empty(store, ui)
            view == "hist" -> History(s)
            run.isEmpty() -> Note("No habits run on this day.")
            else -> run.forEach { HabitRow(store, ui, it, d) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // the bay lives on Today and Shop; here the robot rides small in the header and the planner takes the width
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = if (lay.wide) 28.dp else 20.dp).padding(bottom = 96.dp)) {
            Header("Habits", summary(s, d, p).takeIf { lay.wide }, store, ui, lay, badge = true)
            if (!lay.wide) Text(summary(s, d, p), Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
            body()
        }
        ExtendedFloatingActionButton(text = { Text("Add habit") }, icon = { Icon(ICONS.getValue("plus"), null) }, onClick = { ui.edit = "" },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = W.act, contentColor = W.onAct)
    }
}

private fun summary(s: Data, d: LocalDate, p: Progress) = when {
    s.habits.isEmpty() -> "No habits yet"
    d > LocalDate.now() -> "${plural(p.slots, "slot")} planned"
    else -> "${p.done} of ${p.slots} bolts fastened" + if (p.fail > 0) ", ${p.fail} failed" else ""
}

@Composable
fun segColors() = SegmentedButtonDefaults.colors(activeContainerColor = W.raised, activeContentColor = W.text, inactiveContainerColor = W.ground,
    inactiveContentColor = W.text, activeBorderColor = W.steel, inactiveBorderColor = W.edge)

@Composable
fun Stepper(label: String, prev: () -> Unit, next: (() -> Unit)?) = Row(verticalAlignment = Alignment.CenterVertically) {
    IconButton(prev) { Icon(ICONS.getValue("prev"), "Previous", tint = W.text) }
    Text(label, Modifier.widthIn(min = 120.dp), style = MaterialTheme.typography.titleMedium, maxLines = 1)
    IconButton({ next?.invoke() }, enabled = next != null) { Icon(ICONS.getValue("next"), "Next", tint = if (next != null) W.text else W.edge) }
}

// A month of day tiles; mark draws each day's sign. ahead: future days open too.
@Composable
fun Month(m: YearMonth, mark: @Composable (LocalDate) -> Unit, ahead: Boolean, open: (LocalDate) -> Unit) {
    val t = LocalDate.now()
    val lead = m.atDay(1).dayOfWeek.value - 1
    // a hairline grid: each week a ruled row, the day's mark under its number
    BoxWithConstraints { val cellH = maxOf(64.dp, maxWidth / 7 / 1.25f); Column {
        Row(Modifier.padding(bottom = 6.dp)) { DOW.forEach { Text(it.uppercase(), Modifier.weight(1f).padding(start = 6.dp), style = Label) } }
        (0 until (lead + m.lengthOfMonth() + 6) / 7).forEach { w ->
            androidx.compose.material3.HorizontalDivider(color = W.line)
            Row {
                (0..6).forEach { i ->
                    val n = w * 7 + i - lead + 1
                    if (n < 1 || n > m.lengthOfMonth()) Spacer(Modifier.weight(1f)) else {
                        val d = m.atDay(n)
                        val off = d > t && !ahead
                        Column(Modifier.weight(1f).height(cellH).clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !off) { open(d) }.semantics { contentDescription = d.format(dayFmt) }.padding(6.dp)) {
                            Box(Modifier.size(30.dp).border(2.dp, if (d == t) W.act else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                                Text("$n", style = MaterialTheme.typography.labelLarge, color = if (off) W.edge else W.text)
                            }
                            Box(Modifier.width(30.dp).weight(1f), contentAlignment = Alignment.Center) { mark(d) }
                        }
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
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(ICONS.getValue(h.stat), null, Modifier.size(20.dp), tint = W.steel)
                Text(h.name, Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$pct%", style = Numeral, color = W.text)
            }
            Text("${r.done} of ${plural(r.slots, "slot")} done" + (if (r.fail > 0) " · ${r.fail} failed" else "") + " · best run ${unit(r.best)}" + (if (r.now > 0) " · ${unit(r.now)} running now" else ""),
                style = MaterialTheme.typography.bodyMedium, color = W.dim)
            Canvas(Modifier.fillMaxWidth().height(14.dp).padding(top = 6.dp).semantics { contentDescription = "best run ${unit(r.best)}, current ${unit(r.now)}" }) {
                val step = minOf(14f * density, size.width / maxOf(r.best, 1))
                drawRoundRect(W.line, Offset.Zero, Size(size.width, size.height), CornerRadius(size.height / 2))
                drawRoundRect(W.steel, Offset.Zero, Size(step * r.best, size.height), CornerRadius(size.height / 2), alpha = 0.55f)
                drawRoundRect(W.act, Offset.Zero, Size(step * r.now, size.height), CornerRadius(size.height / 2))
            }
        }
        androidx.compose.material3.HorizontalDivider(color = W.line)
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
        // past two options a narrow form lays the choices out two to a row
        BoxWithConstraints {
            if (opts.size > 2 && maxWidth < 560.dp) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opts.chunked(2).forEach { r ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.forEach { (k, v) ->
                            FilterChip(cur == k, { set(k) }, { Text(v, Modifier.padding(vertical = 8.dp)) }, Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = W.raised, selectedLabelColor = W.text, labelColor = W.text),
                                border = FilterChipDefaults.filterChipBorder(true, cur == k, borderColor = W.edge, selectedBorderColor = W.steel, selectedBorderWidth = 1.5.dp))
                        }
                        if (r.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                opts.forEachIndexed { i, (k, v) -> SegmentedButton(cur == k, { set(k) }, SegmentedButtonDefaults.itemShape(i, opts.size), icon = {}, colors = segColors()) { Text(v, maxLines = 2) } }
            }
        }
    }
    val field = @Composable { label: String, content: @Composable () -> Unit ->
        Text(label.uppercase(), Modifier.padding(top = 22.dp, bottom = 8.dp), style = Label)
        content()
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = if (lay.wide) 40.dp else 20.dp).padding(bottom = 32.dp)) {
        Header(if (h == null) "New habit" else "Edit habit", null, store, ui, lay, badge = true) // the form has no bay at any size
        Column(Modifier.widthIn(max = 720.dp)) {
            OutlinedTextField(name, { name = it.take(40) }, Modifier.fillMaxWidth(), label = { Text("Name") }, placeholder = { Text("Read 20 minutes") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = W.act, unfocusedBorderColor = W.edge, cursorColor = W.act, focusedLabelColor = W.act))
            field("Type") {
                seg(listOf("good" to "Build it", "bad" to "Break it"), kind) { kind = it }
                Text("Build it: you earn each time you do it. Break it: you earn each time you resist it.", Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
            field("How often") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton({ target = (target - 1).coerceAtLeast(1) }) { Icon(ICONS.getValue("minus"), "Fewer", tint = W.text) }
                    Text("$target", Modifier.widthIn(min = 28.dp), style = Numeral, color = W.text)
                    IconButton({ target = (target + 1).coerceAtMost(7) }) { Icon(ICONS.getValue("plus"), "More", tint = W.text) }
                    Text("times a", Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodyLarge)
                    Box(Modifier.widthIn(max = 240.dp)) { seg(listOf("day" to "Day", "week" to "Week"), per) { per = it } }
                }
            }
            field("Weight") {
                seg(WEIGHTS.map { (k, w) -> k to "${w.name}\n${plural(w.bolts, "bolt")}, ${plural(w.pts, "point")}" }, weight) { weight = it }
                Text("Done pays this much. Failed takes the same amount back.", Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
            field("Attribute it trains") { seg(STATS.map { (k, v) -> k to "${v.name}\n${v.gloss}" }, stat) { stat = it } }
            field("Runs") {
                FlowRow(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                    Text("From", style = MaterialTheme.typography.bodyLarge)
                    OutlinedButton({ picking = "start" }, border = BorderStroke(1.dp, W.edge)) { Text(start.format(shortFmt), color = W.text) }
                    Text("until", style = MaterialTheme.typography.bodyLarge)
                    Box(Modifier.widthIn(max = 320.dp)) { seg(listOf("never" to "Indefinitely", "on" to "A set day"), if (ends) "on" else "never") { ends = it == "on" } }
                    if (ends) OutlinedButton({ picking = "end" }, border = BorderStroke(1.dp, W.edge)) { Text(end?.format(shortFmt) ?: "Pick the day", color = W.text) }
                }
                FlowRow(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DOW.forEachIndexed { i, n ->
                        FilterChip(i in days, { days = if (i in days) days - i else days + i }, { Text(n) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = W.raised, selectedLabelColor = W.text, labelColor = W.text),
                            border = FilterChipDefaults.filterChipBorder(true, i in days, borderColor = W.edge, selectedBorderColor = W.steel, selectedBorderWidth = 1.5.dp))
                    }
                }
            }
            if (problem != null && name.isNotEmpty()) Text(problem, Modifier.padding(top = 16.dp), style = MaterialTheme.typography.bodyMedium, color = W.coral)
            Row(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (h != null) TextButton({ confirm = true }) { Text("Delete habit", color = W.coral) }
                Spacer(Modifier.weight(1f))
                OutlinedButton({ ui.edit = null }, Modifier.height(48.dp), border = BorderStroke(1.dp, W.edge)) { Text("Cancel", color = W.text) }
                Button({
                    store.change {
                        val x = h ?: Habit(newId(), "").also { habits += it }
                        x.name = name.trim(); x.kind = kind; x.per = per; x.target = target; x.weight = weight; x.stat = stat
                        x.start = start; x.end = if (ends) end else null; x.days = days.takeIf { it.size < 7 }
                    }
                    ui.edit = null
                }, Modifier.height(48.dp), enabled = problem == null, shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) {
                    Text(if (h == null) "Add habit" else "Save changes")
                }
            }
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
