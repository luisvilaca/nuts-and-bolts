package com.luisvilaca.nutsandbolts

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// What the screens share: where the owner is (kept across the system ending the app in the background, see saver) and
// the message bar.
class Ui(val scope: CoroutineScope) {
    var tab by mutableStateOf("today")
    var settings by mutableStateOf(false)
    var edit by mutableStateOf<String?>(null) // habit id in the form; "" is a new habit
    fun openForm(id: String) { edit = id; settings = false } // the form opens over the current tab, which it returns to
    var page by mutableStateOf<LocalDate?>(null) // the open diary page
    var date by mutableStateOf(LocalDate.now()) // the day Habits shows
    val snack = SnackbarHostState()

    fun tell(msg: String, undo: (() -> Unit)? = null) = scope.launch {
        snack.currentSnackbarData?.dismiss()
        if (snack.showSnackbar(msg, if (undo != null) "Undo" else null, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) { Sfx.play("undo"); undo?.invoke() }
    }

    companion object {
        fun saver(scope: CoroutineScope) = Saver<Ui, List<Any?>>(
            save = { listOf(it.tab, it.edit, it.page?.toString(), it.date.toString(), it.settings) },
            restore = { l -> Ui(scope).apply { tab = l[0] as String; edit = l[1] as String?; page = (l[2] as String?)?.let(LocalDate::parse); date = LocalDate.parse(l[3] as String); settings = l[4] as Boolean } },
        )
    }
}

// wide: a landscape window with room for two panes side by side. phone: a narrow window.
class Lay(val wide: Boolean, val phone: Boolean)

private val TABS = listOf("today" to "Lab", "shop" to "Shop", "habits" to "Habits", "diary" to "Diary")
private val TAB_ICON = mapOf("today" to "base", "shop" to "shop", "habits" to "habits", "diary" to "diary")

@Composable
fun App(store: Store, go: androidx.compose.runtime.MutableState<String?> = remember { mutableStateOf(null) }) {
    val scope = rememberCoroutineScope()
    val ui = rememberSaveable(saver = Ui.saver(scope)) { Ui(scope) }
    LaunchedEffect(store) { snapshotFlow { store.s.sound }.collect { Sfx.on = it } }
    androidx.compose.runtime.LaunchedEffect(go.value) {
        go.value?.let { ui.tab = it; ui.date = LocalDate.now(); ui.page = null; ui.edit = null; ui.settings = false; go.value = null }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(W.ground)) {
        val lay = Lay(maxWidth > maxHeight && maxWidth >= 600.dp, minOf(maxWidth, maxHeight) < 600.dp)
        // system Back: close what is open, then return to the Lab, then leave
        BackHandler(enabled = ui.tab != "today" && !ui.settings && ui.edit == null && ui.page == null) { ui.tab = "today" }
        Scaffold(
            containerColor = W.ground,
            snackbarHost = { SnackbarHost(ui.snack) { Snackbar(it, containerColor = W.raised, contentColor = W.text, actionColor = W.act) } },
            // the owner wants the tabs at the bottom at every size, except under an open diary page, which needs the height
            bottomBar = { if (ui.page == null) BottomBar(ui) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                when {
                    ui.settings -> SettingsScreen(store, ui, lay)
                    ui.edit != null -> { BackHandler { ui.edit = null }; HabitForm(store, ui, lay, store.s.habits.find { it.id == ui.edit }) }
                    ui.tab == "today" -> LabScreen(store, ui, lay)
                    ui.tab == "shop" -> ShopScreen(store, ui, lay)
                    ui.tab == "habits" -> HabitsScreen(store, ui, lay)
                    else -> DiaryScreen(store, ui, lay)
                }
            }
        }
    }
}

private fun go(ui: Ui, tab: String) { ui.tab = tab; ui.settings = false; ui.edit = null; ui.page = null }

@Composable
private fun BottomBar(ui: Ui) {
    // a compact bar: 60dp of tabs above the system's own navigation area
    Row(Modifier.fillMaxWidth().background(W.rail).navigationBarsPadding().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
        TABS.forEach { (k, v) ->
            val on = ui.tab == k && !ui.settings
            Column(Modifier.weight(1f).fillMaxHeight().selectable(on, role = Role.Tab) { go(ui, k) },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(52.dp, 28.dp).clip(CircleShape).background(if (on) W.raised else Color.Transparent), contentAlignment = Alignment.Center) {
                    Icon(ICONS.getValue(TAB_ICON.getValue(k)), null, Modifier.size(24.dp), tint = if (on) W.act else W.dim)
                }
                Text(v, Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelMedium, color = if (on) W.text else W.dim)
            }
        }
    }
}

// ---------- shared pieces ----------

val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.UK)
fun plural(n: Int, w: String) = "$n $w${if (n == 1) "" else "s"}"

@Composable
fun Header(title: String, sub: String?, store: Store, ui: Ui, lay: Lay, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 60.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.displaySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (sub != null) Text(sub, Modifier.padding(start = 16.dp).weight(1f, fill = false), style = MaterialTheme.typography.bodyLarge, color = W.dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Wallet(store.s)
        IconButton(onClick = { ui.settings = true }, Modifier.padding(start = 4.dp)) { Icon(ICONS.getValue("settings"), "Settings", tint = if (ui.settings) W.act else W.dim) }
    }
}

// A value worked out from the data once per change (and per key), not on every recomposition: a month's progress,
// each habit's record, today's bench.
@Composable
fun <T> derived(store: Store, vararg keys: Any?, calc: () -> T): T = remember(store.tick, *keys) { calc() }

@Composable
fun Wallet(s: Data) = Row(Modifier.semantics(mergeDescendants = true) { contentDescription = "${plural(s.bolts, "bolt")}, ${plural(s.nuts, "nut")}" },
    horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
    Coin("bolts", s.bolts); Coin("nuts", s.nuts)
}

@Composable
// short: the balance does not cover it, so the price reads in dim instead of its metal
fun Coin(k: String, n: Int, small: Boolean = false, short: Boolean = false) = Row(verticalAlignment = Alignment.CenterVertically) {
    val c = if (short) W.dim else if (k == "nuts") W.gold else W.steel
    Icon(ICONS.getValue(k), null, Modifier.size(if (small) 18.dp else 22.dp), tint = c)
    Text("$n", Modifier.padding(start = 6.dp), style = Numeral.copy(fontSize = if (small) MaterialTheme.typography.bodyMedium.fontSize else Numeral.fontSize), color = c)
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) = Text(text.uppercase(), modifier.padding(top = 28.dp, bottom = 10.dp), style = Label)

private val BTN = mapOf("good" to listOf("Done", "Skip", "Fail"), "bad" to listOf("Resisted", "Skip", "Gave in"))

fun log(store: Store, ui: Ui, h: Habit, d: LocalDate, kind: String) {
    val st = STATS.getValue(h.stat)
    val before = level(store.s.stats.getValue(h.stat))
    var m: Mark? = null
    store.change { m = mark(this, h, d, kind) }
    val k = m ?: return
    val after = level(store.s.stats.getValue(h.stat))
    Sfx.play(if (after > before) "level" else kind)
    when (kind) { "done" -> { store.fresh = h.id to now(); store.react("cheer") }; "fail" -> store.react("sad") }
    // crossing a level is the news; the points come second
    ui.tell(when {
        after > before -> "Level up! ${st.name} is now ${st.stages[after - 1]}, level $after" + if (k.l > 0) ", +${plural(LEVEL_NUTS * k.l, "nut")}" else ""
        after < before -> "${st.name} fell back to ${st.stages[after - 1]}, level $after. Lost ${plural(-k.b, "bolt")}."
        kind == "done" -> "+${plural(k.b, "bolt")} · +${k.p} ${st.name}" + if (k.n > 0) " · +1 nut for the streak" else ""
        kind == "fail" -> "Sheared · −${plural(-k.b, "bolt")} · −${-k.p} ${st.name}"
        else -> "Skipped. Nothing gained, nothing lost."
    }) { store.change { undo(this, h, d) } }
}

fun meta(s: Data, h: Habit, d: LocalDate): String {
    val st = streak(s, h, d)
    return listOfNotNull(
        if (h.kind == "bad") "Break it" else null, "${h.target} a ${h.per}", STATS.getValue(h.stat).name,
        h.days?.let { ds -> ds.sorted().joinToString(" ") { DOW[it] } },
        h.end?.let { "until ${it.format(DateTimeFormatter.ofPattern("d MMM", Locale.UK))}" },
        if (st > 1) "$st ${h.per}s running" else null,
    ).joinToString(" · ")
}
val DOW = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// One habit as a card, read left to right: name and meta, the counter over its tracker, one clear Done, and a menu for
// the rest (Skip, Fail, take back, Edit, Delete), which a long press on Done also opens. A period with every bolt in
// shows a finished chip instead of a button and a glowing edge. select: the card picks the habit for the side panel
// (split Habits); otherwise the name opens the form. cue: a chevron after the name, where the name opens the form.
@Composable
fun HabitRow(store: Store, ui: Ui, h: Habit, d: LocalDate, big: Boolean = false, selected: Boolean = false, cue: Boolean = true, select: (() -> Unit)? = null) = BoxWithConstraints {
    val stacked = big || maxWidth < 480.dp
    val s = store.s
    val sl = slots(s, h, d)
    val logged = sl.count { it.s != "" }
    val ahead = d > LocalDate.now()
    val full = logged >= h.target || ahead
    val complete = !ahead && sl.all { it.s == "done" }
    val label = BTN.getValue(h.kind)
    val mine = !s.marks[d]?.get(h.id).isNullOrEmpty() // a mark made on this day, which can be taken back here
    var menu by remember { mutableStateOf<String?>(null) } // which anchor the menu opened from: "more" or "done"
    var deleting by remember { mutableStateOf(false) }
    val items = @Composable {
        val item = @Composable { text: String, icon: String, tint: androidx.compose.ui.graphics.Color, on: Boolean, go: () -> Unit ->
            DropdownMenuItem({ Text(text, color = if (on) tint else W.dim.copy(alpha = 0.6f)) }, { menu = null; go() }, enabled = on,
                leadingIcon = { Icon(ICONS.getValue(icon), null, Modifier.size(20.dp), tint = if (on) tint else W.dim.copy(alpha = 0.6f)) })
        }
        item(label[1], "minus", W.text, !full) { log(store, ui, h, d, "skip") }
        item(label[2], "fail", W.coral, !full) { log(store, ui, h, d, "fail") }
        item("Take back the last bolt", "undo", W.text, mine) {
            var m: Mark? = null
            store.change { m = undo(this, h, d) }
            m?.let { Sfx.play("undo"); ui.tell("Bolt taken back") }
        }
        HorizontalDivider(color = W.line)
        item("Edit habit", "diary", W.text, true) { ui.openForm(h.id) }
        item("Delete habit", "trash", W.coral, true) { deleting = true }
    }
    val counter = @Composable { m: Modifier ->
        Column(m.semantics(mergeDescendants = true) {
            contentDescription = boltsIn(Progress(sl.count { it.s == "done" }, sl.count { it.s == "fail" }, h.target), false) + ", $logged of ${h.target} logged"
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(ICONS.getValue("bolts"), null, Modifier.size(16.dp), tint = W.steel)
                Text("$logged / ${h.target}", Modifier.padding(start = 6.dp), style = Numeral, color = W.text)
            }
            Tracker(sl, Modifier.fillMaxWidth().padding(top = 6.dp).height(8.dp))
        }
    }
    val actions = @Composable { m: Modifier ->
        Row(m, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val size = (if (stacked) Modifier.weight(1f) else Modifier.widthIn(min = 112.dp)).height(if (big) 52.dp else 44.dp)
            Box(size) {
                if (complete || (full && !ahead)) {
                    // finished: a quiet chip, not a dead button; a period closed by a skip or a sheared bolt reads in dim
                    Row(Modifier.matchParentSize().clip(CircleShape).border(1.dp, if (complete) W.steel else W.edge, CircleShape).longPress { menu = "done" }
                        .semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        if (complete) Icon(ICONS.getValue("check"), null, Modifier.size(18.dp), tint = W.steel)
                        Text(if (!complete) "Closed" else if (h.kind == "bad") "Held" else "All in", Modifier.padding(start = if (complete) 6.dp else 0.dp),
                            style = MaterialTheme.typography.labelLarge, color = if (complete) W.steel else W.dim)
                    }
                } else Button(onClick = { log(store, ui, h, d, "done") }, enabled = !full, modifier = Modifier.matchParentSize().longPress { menu = "done" },
                    shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) {
                    Text(if (ahead) "Not yet" else label[0])
                }
                DropdownMenu(menu == "done", { menu = null }, containerColor = W.raised, shape = RoundedCornerShape(12.dp)) { items() }
            }
            Box {
                OutlinedIconButton(onClick = { menu = "more" }, modifier = Modifier.size(44.dp), border = BorderStroke(1.dp, W.edge),
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = W.steel)) { Icon(ICONS.getValue("more"), "More for ${h.name}") }
                DropdownMenu(menu == "more", { menu = null }, containerColor = W.raised, shape = RoundedCornerShape(12.dp)) { items() }
            }
        }
    }
    val pick = Modifier.clickable(onClickLabel = if (select != null) "show details" else "edit") { if (select != null) select() else ui.openForm(h.id) }
    Card(Modifier.fillMaxWidth(), selected = selected, accent = if (complete) W.glow else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).then(pick)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(h.name, Modifier.weight(1f, fill = false), style = if (big) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (select == null && cue) Icon(ICONS.getValue("next"), null, Modifier.padding(start = 4.dp).size(16.dp), tint = W.dim)
                }
                Text(meta(s, h, d), style = MaterialTheme.typography.bodyMedium, color = W.dim, maxLines = if (stacked) 2 else 1, overflow = TextOverflow.Ellipsis)
            }
            if (!stacked) {
                counter(Modifier.padding(horizontal = 16.dp, vertical = 6.dp).width(120.dp))
                actions(Modifier)
            }
        }
        if (stacked) {
            counter(Modifier.fillMaxWidth().padding(top = 12.dp))
            actions(Modifier.fillMaxWidth().padding(top = 12.dp))
        }
    }
    if (deleting) ConfirmDeleteHabit(store, ui, h) { deleting = false }
}

// Deleting a habit, wherever it is asked for: a confirmation, then the habit comes off with an Undo that puts it back
// where it was. Marks are kept by date and habit id, so they come back with it untouched.
@Composable
fun ConfirmDeleteHabit(store: Store, ui: Ui, h: Habit, done: (Boolean) -> Unit) = AlertDialog(onDismissRequest = { done(false) },
    title = { Text("Delete “${h.name}”?") }, text = { Text("Its frame comes off the bench. What it already earned stays.") },
    confirmButton = {
        TextButton({
            var at = -1
            store.change { at = removeHabit(this, h) }
            ui.tell("“${h.name}” deleted") { store.change { restoreHabit(this, h, at) } }
            done(true)
        }) { Text("Delete", color = W.coral) }
    },
    dismissButton = { TextButton({ done(false) }) { Text("Keep it", color = W.text) } })

// A long press, seen before the control's own click, so it opens a menu without also pressing the button.
fun Modifier.longPress(go: () -> Unit) = pointerInput(go) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        // the press either lets go in time (an ordinary tap) or outlasts the long-press timeout
        val released = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            do { val e = awaitPointerEvent(PointerEventPass.Initial) } while (e.changes.any { it.pressed })
            true
        }
        if (released == null) {
            go()
            // swallow the rest of this press so the button under it does not fire on release
            do { val e = awaitPointerEvent(PointerEventPass.Initial); e.changes.forEach { it.consume() } } while (e.changes.any { it.pressed })
        }
    }
}

@Composable
fun Note(text: String, modifier: Modifier = Modifier) = Text(text, modifier.padding(vertical = 16.dp), style = MaterialTheme.typography.bodyLarge, color = W.dim)

@Composable
fun Gap(w: androidx.compose.ui.unit.Dp) = Spacer(Modifier.width(w))
