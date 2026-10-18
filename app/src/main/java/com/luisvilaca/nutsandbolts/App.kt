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

// What the screens share that is not saved: where the owner is, and the message bar.
class Ui(val scope: CoroutineScope) {
    var tab by mutableStateOf("today")
    var settings by mutableStateOf(false)
    var edit by mutableStateOf<String?>(null) // habit id in the form; "" is a new habit
    var page by mutableStateOf<LocalDate?>(null) // the open diary page
    var date by mutableStateOf(LocalDate.now()) // the day Habits shows
    val snack = SnackbarHostState()

    fun tell(msg: String, undo: (() -> Unit)? = null) = scope.launch {
        snack.currentSnackbarData?.dismiss()
        if (snack.showSnackbar(msg, if (undo != null) "Undo" else null, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) { Sfx.play("undo"); undo?.invoke() }
    }
}

// wide: a landscape window with room for two panes side by side. phone: a narrow window.
class Lay(val wide: Boolean, val phone: Boolean)

private val TABS = listOf("today" to "Lab", "shop" to "Shop", "habits" to "Habits", "diary" to "Diary")
private val TAB_ICON = mapOf("today" to "base", "shop" to "shop", "habits" to "habits", "diary" to "diary")

@Composable
fun App(store: Store, go: androidx.compose.runtime.MutableState<String?> = remember { mutableStateOf(null) }) {
    val scope = rememberCoroutineScope()
    val ui = remember { Ui(scope) }
    androidx.compose.runtime.LaunchedEffect(go.value) {
        go.value?.let { ui.tab = it; ui.date = LocalDate.now(); ui.page = null; ui.edit = null; ui.settings = false; go.value = null }
    }
    store.tick // every change recomposes the screens
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
                    Icon(ICONS.getValue(TAB_ICON.getValue(k)), null, Modifier.size(20.dp), tint = if (on) W.act else W.dim)
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

@Composable
fun Wallet(s: Data) = Row(Modifier.semantics(mergeDescendants = true) { contentDescription = "${plural(s.bolts, "bolt")}, ${plural(s.nuts, "nut")}" },
    horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
    Coin("bolts", s.bolts); Coin("nuts", s.nuts)
}

@Composable
fun Coin(k: String, n: Int, small: Boolean = false) = Row(verticalAlignment = Alignment.CenterVertically) {
    val c = if (k == "nuts") W.gold else W.steel
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

// One habit as a card: name and meta, a progress counter (slots logged of the target over a segment per slot), one
// clear primary button that logs the next slot, and a menu for the rest: Skip, Fail, taking the last mark back, Edit
// and Delete. select: the card picks the habit for the side panel (split Habits); otherwise the name opens the form.
@Composable
fun HabitRow(store: Store, ui: Ui, h: Habit, d: LocalDate, big: Boolean = false, selected: Boolean = false, select: (() -> Unit)? = null) = BoxWithConstraints {
    val stacked = big || maxWidth < 480.dp
    val s = store.s
    val sl = slots(s, h, d)
    val logged = sl.count { it.s != "" }
    val ahead = d > LocalDate.now()
    val full = logged >= h.target || ahead
    val label = BTN.getValue(h.kind)
    val mine = !s.marks[d]?.get(h.id).isNullOrEmpty() // a mark made on this day, which can be taken back here
    var menu by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val counter = @Composable { m: Modifier ->
        Column(m.semantics(mergeDescendants = true) {
            contentDescription = boltsIn(Progress(sl.count { it.s == "done" }, sl.count { it.s == "fail" }, h.target), false) + ", $logged of ${h.target} logged"
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(ICONS.getValue("bolts"), null, Modifier.size(14.dp), tint = W.steel)
                Text("$logged / ${h.target}", Modifier.padding(start = 6.dp), style = Numeral.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize), color = if (logged >= h.target) W.text else W.dim)
            }
            Tracker(sl, Modifier.fillMaxWidth().padding(top = 6.dp).height(8.dp))
        }
    }
    val actions = @Composable { m: Modifier ->
        Row(m, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { log(store, ui, h, d, "done") }, enabled = !full, modifier = (if (stacked) Modifier.weight(1f) else Modifier.widthIn(min = 112.dp)).height(if (big) 52.dp else 44.dp),
                shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) {
                // a greyed button says why it is greyed
                Text(when {
                    ahead -> "Not yet"
                    !full -> label[0]
                    sl.all { it.s == "done" } -> if (h.kind == "bad") "Held" else "All in"
                    else -> "Closed" // full, but with a skip or a sheared bolt in it
                })
            }
            Box {
                OutlinedIconButton(onClick = { menu = true }, modifier = Modifier.size(44.dp), border = BorderStroke(1.dp, W.edge),
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = W.steel)) { Icon(ICONS.getValue("more"), "More for ${h.name}") }
                DropdownMenu(menu, { menu = false }, containerColor = W.raised, shape = RoundedCornerShape(12.dp)) {
                    val item = @Composable { text: String, icon: String, tint: androidx.compose.ui.graphics.Color, on: Boolean, go: () -> Unit ->
                        DropdownMenuItem({ Text(text, color = if (on) tint else W.edge) }, { menu = false; go() }, enabled = on,
                            leadingIcon = { Icon(ICONS.getValue(icon), null, Modifier.size(20.dp), tint = if (on) tint else W.edge) })
                    }
                    item(label[1], "minus", W.text, !full) { log(store, ui, h, d, "skip") }
                    item(label[2], "fail", W.coral, !full) { log(store, ui, h, d, "fail") }
                    item("Take back the last bolt", "undo", W.text, mine) {
                        var m: Mark? = null
                        store.change { m = undo(this, h, d) }
                        m?.let { Sfx.play("undo"); ui.tell("Bolt taken back") }
                    }
                    androidx.compose.material3.HorizontalDivider(color = W.line)
                    item("Edit habit", "diary", W.text, true) { ui.edit = h.id; ui.tab = "habits"; ui.settings = false }
                    item("Delete habit", "trash", W.coral, true) { deleting = true }
                }
            }
        }
    }
    val pick = Modifier.clickable(onClickLabel = if (select != null) "show details" else "edit") {
        if (select != null) select() else { ui.edit = h.id; ui.tab = "habits"; ui.settings = false }
    }
    Card(Modifier.fillMaxWidth(), selected = selected) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).then(pick)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(h.name, Modifier.weight(1f, fill = false), style = if (big) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (select == null) Icon(ICONS.getValue("next"), null, Modifier.padding(start = 4.dp).size(16.dp), tint = W.dim) // the name opens the form
                }
                Text(meta(s, h, d), style = MaterialTheme.typography.bodyMedium, color = W.dim, maxLines = 2)
            }
            if (!stacked) {
                counter(Modifier.padding(horizontal = 16.dp).width(120.dp))
                actions(Modifier)
            }
        }
        if (stacked) {
            counter(Modifier.fillMaxWidth().padding(top = 12.dp))
            actions(Modifier.fillMaxWidth().padding(top = 12.dp))
        }
    }
    if (deleting) androidx.compose.material3.AlertDialog(onDismissRequest = { deleting = false },
        title = { Text("Delete “${h.name}”?") }, text = { Text("Its frame comes off the bench. What it already earned stays.") },
        confirmButton = { TextButton({ store.change { habits.remove(h) }; deleting = false }) { Text("Delete", color = W.coral) } },
        dismissButton = { TextButton({ deleting = false }) { Text("Keep it", color = W.text) } })
}

@Composable
fun Note(text: String, modifier: Modifier = Modifier) = Text(text, modifier.padding(vertical = 16.dp), style = MaterialTheme.typography.bodyLarge, color = W.dim)

@Composable
fun Gap(w: androidx.compose.ui.unit.Dp) = Spacer(Modifier.width(w))
