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
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.material3.ripple
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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import androidx.glance.appwidget.updateAll
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext

// What the screens share: where the owner is (kept across the system ending the app in the background, see saver) and
// the message bar.
class Ui(val scope: CoroutineScope) {
    var tab by mutableStateOf(Tab.Lab)
    var settings by mutableStateOf(false)
    var edit by mutableStateOf<String?>(null) // habit id in the form; "" is a new habit
    fun openForm(id: String) { edit = id; settings = false } // the form opens over the current tab, which it returns to
    var page by mutableStateOf<LocalDate?>(null) // the open diary page
    var date by mutableStateOf(LocalDate.now()) // the day Habits shows
    val snack = SnackbarHostState()

    fun tell(msg: String, undo: (() -> Unit)? = null) = scope.launch {
        snack.currentSnackbarData?.dismiss()
        if (snack.showSnackbar(msg, if (undo != null) t("Undo") else null, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) { Sfx.play("undo"); undo?.invoke() }
    }

    companion object {
        fun saver(scope: CoroutineScope) = Saver<Ui, List<Any?>>(
            save = { listOf(it.tab.name, it.edit, it.page?.toString(), it.date.toString(), it.settings) },
            restore = { l -> Ui(scope).apply { tab = Tab.valueOf(l[0] as String); edit = l[1] as String?; page = (l[2] as String?)?.let(LocalDate::parse); date = LocalDate.parse(l[3] as String); settings = l[4] as Boolean } },
        )
    }
}

// wide: a landscape window with room for two panes side by side. phone: a narrow window. A data class, so a screen
// handed an equal layout skips recomposing when only the App above it changed.
data class Lay(val wide: Boolean, val phone: Boolean)

// The four sections on the bottom bar, each with its label and icon.
enum class Tab(val label: String, val icon: String) { Lab("Lab", "base"), Shop("Shop", "shop"), Habits("Habits", "habits"), Diary("Diary", "diary") }

@Composable
fun App(store: Store, go: MutableState<String?> = remember { mutableStateOf(null) }) {
    val scope = rememberCoroutineScope()
    val ui = rememberSaveable(saver = Ui.saver(scope)) { Ui(scope) }
    LaunchedEffect(store) { snapshotFlow { store.s.sound }.collect { Sfx.on = it } }
    // every alarm is set again at launch and whenever a reminder could have moved: a habit added, deleted, rescheduled
    // or given a time, the daily reminder changed, a backup restored or the other device's changes arriving
    val ctx = LocalContext.current
    LaunchedEffect(store) {
        snapshotFlow { store.s.let { s -> s.remind.on to s.remind.time to s.habits.map { listOf(it.id, it.remind, it.start, it.end, it.days) } } }
            .collect { Reminder.scheduleAll(ctx, store.s) }
    }
    // an achievement reached gives its part at once, with the news
    LaunchedEffect(store) {
        snapshotFlow { store.tick }.collect {
            newFeats(store.s, LocalDate.now()).forEach { f ->
                store.change { give(this, f) }
                Sfx.play("level")
                ui.tell(t("Achievement: {0}. {1} is yours.", t(f.en), PART.getValue(f.part).name))
            }
        }
    }
    // the home-screen widget follows every change, a second after the last of a burst
    LaunchedEffect(store) { snapshotFlow { store.tick }.collectLatest { kotlinx.coroutines.delay(1000); HabitWidget().updateAll(ctx) } }
    LaunchedEffect(go.value) {
        go.value?.let { g -> ui.tab = Tab.entries.firstOrNull { it.name.equals(g, ignoreCase = true) } ?: Tab.Lab; ui.date = LocalDate.now(); ui.page = null; ui.edit = null; ui.settings = false; go.value = null }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(W.ground)) {
        val lay = Lay(maxWidth > maxHeight && maxWidth >= 600.dp, minOf(maxWidth, maxHeight) < 600.dp)
        val rail = lay.wide && lay.phone
        // system Back: close what is open, then return to the Lab, then leave
        BackHandler(enabled = ui.tab != Tab.Lab && !ui.settings && ui.edit == null && ui.page == null) { ui.tab = Tab.Lab }
        Scaffold(
            containerColor = W.ground,
            // on a wide window the message sits bottom-end over the side pane, at most 480dp wide and, on the Lab, above
            // the attribute row it would otherwise cover just as a Done changes it
            snackbarHost = {
                Box(Modifier.fillMaxWidth(), contentAlignment = if (lay.wide) Alignment.BottomEnd else Alignment.BottomCenter) {
                    // on Habits it rises above the Add habit button, which it would otherwise cover
                    val over = ui.edit == null && !ui.settings
                    val lift = if (over && ui.tab == Tab.Habits) 76.dp else if (over && ui.tab == Tab.Lab && lay.wide) 104.dp else 0.dp
                    SnackbarHost(ui.snack, (if (!lay.wide) Modifier else Modifier.widthIn(max = 480.dp).padding(end = 16.dp)).padding(bottom = lift)) {
                        Snackbar(it, containerColor = W.raised, contentColor = W.text, actionColor = W.act)
                    }
                }
            },
            // the tabs sit at the bottom, except under an open diary page, which needs the height, and on a phone held
            // sideways, where a bar would leave the screens a sliver: there they stand in a rail at the left
            bottomBar = { if (ui.page == null && !rail) BottomBar(ui) },
        ) { pad ->
            Row(Modifier.fillMaxSize().padding(pad)) {
                if (rail && ui.page == null) SideRail(ui)
                Box(Modifier.weight(1f).fillMaxHeight()) { when {
                    ui.settings -> SettingsScreen(store, ui, lay)
                    ui.edit != null -> { BackHandler { ui.edit = null }; HabitForm(store, ui, lay, store.s.habits.find { it.id == ui.edit }) }
                    ui.tab == Tab.Lab -> LabScreen(store, ui, lay)
                    ui.tab == Tab.Shop -> ShopScreen(store, ui, lay)
                    ui.tab == Tab.Habits -> HabitsScreen(store, ui, lay)
                    else -> DiaryScreen(store, ui, lay)
                } }
            }
        }
    }
}

private fun go(ui: Ui, tab: Tab) { ui.tab = tab; ui.settings = false; ui.edit = null; ui.page = null }

@Composable
private fun BottomBar(ui: Ui) {
    // a compact bar: 60dp of tabs above the system's own navigation area
    Row(Modifier.fillMaxWidth().background(W.rail).navigationBarsPadding().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
        Tab.entries.forEach { k -> TabItem(ui, k, Modifier.weight(1f).fillMaxHeight()) }
    }
}

// The same four tabs standing in a 72dp rail down the left edge (a phone held sideways).
@Composable
private fun SideRail(ui: Ui) {
    Column(Modifier.fillMaxHeight().width(72.dp).background(W.rail), verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
        Tab.entries.forEach { k -> TabItem(ui, k, Modifier.fillMaxWidth().height(72.dp)) } // room for a two-line label
    }
}

@Composable
private fun TabItem(ui: Ui, k: Tab, modifier: Modifier) {
    val on = ui.tab == k && !ui.settings
    // the whole column takes the tap, but the press shows only inside the pill, never as a box round the tab
    val press = remember { MutableInteractionSource() }
    Column(modifier.selectable(on, interactionSource = press, indication = null, role = Role.Tab) { if (ui.tab != k) Sfx.play("tab"); go(ui, k) },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(64.dp, 32.dp).clip(CircleShape).background(if (on) W.raised else Color.Transparent).indication(press, ripple()), contentAlignment = Alignment.Center) {
            Icon(ICONS.getValue(k.icon), null, Modifier.size(24.dp), tint = if (on) W.act else W.dim)
        }
        Text(t(k.label), Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelMedium, color = if (on) W.text else W.dim, textAlign = TextAlign.Center, maxLines = 2)
    }
}

// ---------- shared pieces ----------


@Composable
// A screen's title alone (no subtitle, owner 2026-10-18), the wallet and Settings.
fun Header(title: String, store: Store, ui: Ui, lay: Lay, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 60.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.displaySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        store.s.let { Wallet(it.bolts, it.nuts) }
        IconButton(onClick = { ui.settings = true }, Modifier.padding(start = 4.dp)) { Icon(ICONS.getValue("settings"), t("Settings"), tint = if (ui.settings) W.act else W.dim) }
    }
}

// A value worked out from the data once per change (and per key), not on every recomposition: a month's progress,
// each habit's record, today's bench.
@Composable
fun <T> derived(store: Store, vararg keys: Any?, calc: () -> T): T = remember(store.tick, *keys) { calc() }

@Composable
fun Wallet(bolts: Int, nuts: Int) = Row(Modifier.semantics(mergeDescendants = true) { contentDescription = "${plural(bolts, "bolt")}, ${plural(nuts, "nut")}"; liveRegion = LiveRegionMode.Polite },
    horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
    Coin("bolts", bolts); Coin("nuts", nuts)
}

@Composable
// short: the balance does not cover it, so the price reads in dim instead of its metal
fun Coin(k: String, n: Int, small: Boolean = false, short: Boolean = false) = Row(verticalAlignment = Alignment.CenterVertically) {
    val c = if (short) W.dim else if (k == "nuts") W.gold else W.steel
    Icon(ICONS.getValue(k), null, Modifier.size(if (small) 18.dp else 22.dp), tint = c)
    Text("$n", Modifier.padding(start = 6.dp), style = Numeral.copy(fontSize = if (small) MaterialTheme.typography.bodyMedium.fontSize else Numeral.fontSize), color = c)
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) = Text(t(text).uppercase(), modifier.padding(top = 28.dp, bottom = 10.dp), style = Label)

private val BTN = mapOf(Kind.Build to listOf("Done", "Skip", "Fail"), Kind.Break to listOf("Resisted", "Skip", "Gave in"))

fun log(store: Store, ui: Ui, h: Habit, d: LocalDate, kind: Outcome) {
    val st = h.stat
    val before = level(store.s.stats.getValue(h.stat))
    var m: Mark? = null
    store.change { m = mark(this, h, d, kind) }
    val k = m ?: return
    val after = level(store.s.stats.getValue(h.stat))
    // the level is the news when it moves; otherwise the outcome, and a frame that just went critical hums up under its
    // bolt, and a nut clinks on top
    Sfx.play(if (after > before) "level" else if (after < before) "down" else kind.id)
    if (kind == Outcome.Done && slots(store.s, h, d).all { it.s == Outcome.Done }) Sfx.play("critical")
    if (k.n > 0) Sfx.play("nut")
    when (kind) { Outcome.Done -> { store.fresh = h.id to now(); store.react("cheer") }; Outcome.Fail -> store.react("sad"); else -> {} }
    // crossing a level is the news; the points come second
    ui.tell(when {
        after > before -> t("Level up! {0} is now {1}, level {2}", st.title, st.stages[after - 1], after) + if (k.l > 0) ", +${plural(LEVEL_NUTS * k.l, "nut")}" else ""
        after < before -> t("{0} fell back to {1}, level {2}. Lost {3}.", st.title, st.stages[after - 1], after, plural(-k.b, "bolt"))
        kind == Outcome.Done -> "+${plural(k.b, "bolt")} · +${k.p} ${st.title}" + if (k.n > 0) t(" · +1 nut for the streak") else ""
        kind == Outcome.Fail -> t("Sheared") + " · −${plural(-k.b, "bolt")} · −${-k.p} ${st.title}"
        else -> t("Skipped. Nothing gained, nothing lost.")
    }) { store.change { undo(this, h, d) } }
}

fun meta(s: Data, h: Habit, d: LocalDate): String {
    val st = streak(s, h, d)
    return listOfNotNull(
        if (h.kind == Kind.Break) t("Break it") else null, often(h.target, h.per), h.stat.title,
        h.days?.let { ds -> ds.sorted().joinToString(" ") { DOW[it] } },
        h.end?.let { t("until {0}", fmt(it, "d MMM")) },
        if (st > 1) t("{0} running", plural(st, h.per.id)) else null,
    ).joinToString(" · ")
}

// One habit as a card, read left to right: name and meta, the counter over its tracker, one clear Done, and a menu for
// the rest (Skip, Fail, take back, Edit, Delete), which a long press on Done also opens. A period with every bolt in
// shows a finished chip instead of a button and a glowing edge. select: the card picks the habit for the side panel
// (split Habits); otherwise the name opens the form. cue: a chevron after the name, where the name opens the form.
@Composable
fun HabitRow(store: Store, ui: Ui, h: Habit, d: LocalDate, big: Boolean = false, selected: Boolean = false, cue: Boolean = true, select: (() -> Unit)? = null) = BoxWithConstraints {
    val stacked = big || maxWidth < 480.dp
    val s = store.s
    val sl = slots(s, h, d)
    val logged = sl.count { it.s != null }
    val ahead = d > LocalDate.now()
    val full = logged >= h.target || ahead
    val complete = !ahead && sl.all { it.s == Outcome.Done }
    val label = BTN.getValue(h.kind)
    val mine = !s.marks[d]?.get(h.id).isNullOrEmpty() // a mark made on this day, which can be taken back here
    var menu by remember { mutableStateOf<String?>(null) } // which anchor the menu opened from: "more" or "done"
    var deleting by remember { mutableStateOf(false) }
    var noting by remember { mutableStateOf(false) }
    val items = @Composable {
        val item = @Composable { text: String, icon: String, tint: Color, on: Boolean, go: () -> Unit ->
            DropdownMenuItem({ Text(text, color = if (on) tint else W.dim.copy(alpha = 0.6f)) }, { menu = null; go() }, enabled = on,
                leadingIcon = { Icon(ICONS.getValue(icon), null, Modifier.size(20.dp), tint = if (on) tint else W.dim.copy(alpha = 0.6f)) })
        }
        item(t(label[1]), "minus", W.text, !full) { log(store, ui, h, d, Outcome.Skip) }
        item(t(label[2]), "fail", W.coral, !full) { log(store, ui, h, d, Outcome.Fail) }
        item(t("Take back the last bolt"), "undo", W.text, mine) {
            var m: Mark? = null
            store.change { m = undo(this, h, d) }
            m?.let { Sfx.play("undo"); ui.tell(t("Bolt taken back")) }
        }
        item(t("Note on the last bolt"), "diary", W.text, mine) { noting = true }
        HorizontalDivider(color = W.line)
        item(t("Edit habit"), "diary", W.text, true) { ui.openForm(h.id) }
        item(t("Delete habit"), "trash", W.coral, true) { deleting = true }
    }
    val said = Modifier.semantics(mergeDescendants = true) {
        contentDescription = boltsIn(Progress(sl.count { it.s == Outcome.Done }, sl.count { it.s == Outcome.Fail }, h.target), false) + t(", {0} of {1} logged", logged, h.target)
    }
    val count = @Composable { m: Modifier ->
        Row(m, verticalAlignment = Alignment.CenterVertically) {
            Icon(ICONS.getValue("bolts"), null, Modifier.size(16.dp), tint = W.steel)
            Text("$logged / ${h.target}", Modifier.padding(start = 6.dp), style = Numeral, color = W.text)
        }
    }
    val counter = @Composable { m: Modifier ->
        Column(m.then(said)) {
            count(Modifier)
            Tracker(sl, Modifier.fillMaxWidth().padding(top = 6.dp).height(8.dp))
        }
    }
    val actions = @Composable { m: Modifier ->
        Row(m, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // a fixed width on a wide card: what fills it (button or chip) must not take the name's room
            val size = (if (stacked) Modifier.weight(1f) else Modifier.width(132.dp)).height(if (big) 52.dp else 44.dp)
            val still = animationsOff()
            Crossfade(complete || (full && !ahead), size, animationSpec = if (still) snap() else tween(250), label = "finish") { finished -> Box(Modifier.fillMaxSize()) {
                if (finished) {
                    // finished: a quiet chip, not a dead button; a period closed by a skip or a sheared bolt reads in dim
                    Row(Modifier.matchParentSize().clip(CircleShape).border(1.dp, if (complete) W.glow else W.edge, CircleShape).longPress { menu = "done" }
                        .semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        if (complete) Icon(ICONS.getValue("check"), null, Modifier.size(18.dp), tint = W.glow)
                        Text(t(if (!complete) "Closed" else if (h.kind == Kind.Break) "Held" else "All in"), Modifier.padding(start = if (complete) 6.dp else 0.dp),
                            style = MaterialTheme.typography.labelLarge, color = if (complete) W.glow else W.dim)
                    }
                } else {
                    val press = remember { MutableInteractionSource() }
                    val down by press.collectIsPressedAsState()
                    val k by animateFloatAsState(if (down) 0.96f else 1f, if (still) snap() else tween(150), label = "press")
                    Button(onClick = { log(store, ui, h, d, Outcome.Done) }, enabled = !full, modifier = Modifier.matchParentSize().graphicsLayer { scaleX = k; scaleY = k }.longPress { menu = "done" },
                        shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim), interactionSource = press) {
                        Text(t(if (ahead) "Not yet" else label[0]))
                    }
                }
                DropdownMenu(menu == "done", { menu = null }, containerColor = W.raised, shape = RoundedCornerShape(12.dp)) { items() }
            } }
            Box {
                OutlinedIconButton(onClick = { menu = "more" }, modifier = Modifier.size(44.dp), border = BorderStroke(1.dp, W.edge),
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = W.steel)) { Icon(ICONS.getValue("more"), t("More for {0}", h.name)) }
                DropdownMenu(menu == "more", { menu = null }, containerColor = W.raised, shape = RoundedCornerShape(12.dp)) { items() }
            }
        }
    }
    // no press shade over the words: the form opening, or the card taking the selection edge, is the answer to the tap
    val pick = Modifier.clickable(remember { MutableInteractionSource() }, null, onClickLabel = t(if (select != null) "show details" else "edit")) { if (select != null) select() else ui.openForm(h.id) }
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
            } else count(Modifier.padding(start = 12.dp).then(said)) // stacked: the count sits beside the name, saving a line
        }
        if (stacked) {
            Tracker(sl, Modifier.fillMaxWidth().padding(top = 10.dp).height(8.dp))
            actions(Modifier.fillMaxWidth().padding(top = 12.dp))
        }
    }
    if (deleting) ConfirmDeleteHabit(store, ui, h) { deleting = false }
    if (noting) s.marks[d]?.get(h.id)?.lastOrNull()?.let { m -> NoteDialog(m.note.orEmpty(), { noting = false }) { v -> store.change { m.note = v.trim().ifEmpty { null } } } }
}

// A few words on a bolt ("8 glasses", "5 km"), shown in the habit's log. An empty note takes the note off.
@Composable
fun NoteDialog(start: String, close: () -> Unit, save: (String) -> Unit) {
    var v by remember { mutableStateOf(start) }
    AlertDialog(onDismissRequest = close, title = { Text(t("Note on the last bolt")) },
        text = { Field(v, { v = it.take(60) }, t("8 glasses, 5 km…")) },
        confirmButton = { TextButton({ save(v); close() }) { Text(t("Save"), color = W.act) } },
        dismissButton = { TextButton(close) { Text(t("Cancel"), color = W.text) } })
}

// Deleting a habit, wherever it is asked for: a confirmation, then the habit comes off with an Undo that puts it back
// where it was. Marks are kept by date and habit id, so they come back with it untouched.
@Composable
fun ConfirmDeleteHabit(store: Store, ui: Ui, h: Habit, done: (Boolean) -> Unit) = AlertDialog(onDismissRequest = { done(false) },
    title = { Text(t("Delete “{0}”?", h.name)) }, text = { Text(t("Its frame comes off the bench. What it already earned stays.")) },
    confirmButton = {
        TextButton({
            var at = -1
            store.change { at = removeHabit(this, h) }
            Sfx.play("delete")
            ui.tell(t("“{0}” deleted", h.name)) { store.change { restoreHabit(this, h, at) } }
            done(true)
        }) { Text(t("Delete"), color = W.coral) }
    },
    dismissButton = { TextButton({ done(false) }) { Text(t("Keep it"), color = W.text) } })

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
fun Gap(w: Dp) = Spacer(Modifier.width(w))
