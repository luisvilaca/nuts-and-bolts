package com.luisvilaca.nutsandbolts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
        if (snack.showSnackbar(msg, if (undo != null) "Undo" else null, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) undo?.invoke()
    }
}

// wide: a landscape window with room for the rail and two panes side by side. phone: a narrow window.
class Lay(val wide: Boolean, val phone: Boolean) { val rail get() = wide }

private val TABS = listOf("today" to "Today", "shop" to "Shop", "habits" to "Habits", "diary" to "Diary")
private val TAB_ICON = mapOf("today" to "base", "shop" to "shop", "habits" to "habits", "diary" to "diary")

@Composable
fun App(store: Store) {
    val scope = rememberCoroutineScope()
    val ui = remember { Ui(scope) }
    store.tick // every change recomposes the screens
    BoxWithConstraints(Modifier.fillMaxSize().background(W.ground)) {
        val rail = maxWidth > maxHeight && maxWidth >= 600.dp
        val lay = Lay(rail, minOf(maxWidth, maxHeight) < 600.dp)
        val tall = maxHeight >= 560.dp
        // system Back: close what is open, then return to Today, then leave
        BackHandler(enabled = ui.tab != "today" && !ui.settings && ui.edit == null && ui.page == null) { ui.tab = "today" }
        Scaffold(
            containerColor = W.ground,
            snackbarHost = { SnackbarHost(ui.snack) { Snackbar(it, containerColor = W.raised, contentColor = W.text, actionColor = W.act) } },
            bottomBar = { if (!rail) BottomBar(ui) },
        ) { pad ->
            Row(Modifier.fillMaxSize().padding(pad)) {
                if (rail) Rail(ui, tall)
                Box(Modifier.weight(1f).fillMaxSize()) {
                    when {
                        ui.settings -> SettingsScreen(store, ui, lay)
                        ui.tab == "today" -> TodayScreen(store, ui, lay)
                        ui.tab == "shop" -> ShopScreen(store, ui, lay)
                        ui.tab == "habits" -> HabitsScreen(store, ui, lay)
                        else -> DiaryScreen(store, ui, lay)
                    }
                }
            }
        }
    }
}

private fun go(ui: Ui, tab: String) { ui.tab = tab; ui.settings = false; ui.edit = null; ui.page = null }

@Composable
private fun Rail(ui: Ui, tall: Boolean) {
    val item = NavigationRailItemDefaults.colors(selectedIconColor = W.act, selectedTextColor = W.text, indicatorColor = W.raised, unselectedIconColor = W.dim, unselectedTextColor = W.dim)
    NavigationRail(containerColor = W.rail, header = { if (tall) AtomMark(Modifier.padding(vertical = 18.dp).size(40.dp)) }) {
        TABS.forEach { (k, v) ->
            NavigationRailItem(selected = ui.tab == k && !ui.settings, onClick = { go(ui, k) }, icon = { Icon(ICONS.getValue(TAB_ICON.getValue(k)), null) }, label = { Text(v) }, colors = item)
        }
        Spacer(Modifier.weight(1f))
        NavigationRailItem(selected = ui.settings, onClick = { ui.settings = true; ui.edit = null; ui.page = null }, icon = { Icon(ICONS.getValue("settings"), null) }, label = { Text("Settings") }, colors = item)
        if (tall) Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BottomBar(ui: Ui) {
    NavigationBar(containerColor = W.rail) {
        TABS.forEach { (k, v) ->
            NavigationBarItem(selected = ui.tab == k && !ui.settings, onClick = { go(ui, k) }, icon = { Icon(ICONS.getValue(TAB_ICON.getValue(k)), null) }, label = { Text(v) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = W.act, selectedTextColor = W.text, indicatorColor = W.raised, unselectedIconColor = W.dim, unselectedTextColor = W.dim))
        }
    }
}

// The app's mark: a sprout with two leaves in a small tray.
@Composable
fun AtomMark(modifier: Modifier) = androidx.compose.foundation.Canvas(modifier) {
    val w = size.width
    drawLine(W.steel, androidx.compose.ui.geometry.Offset(w * 0.5f, w * 0.88f), androidx.compose.ui.geometry.Offset(w * 0.5f, w * 0.42f), w * 0.05f)
    leaf(androidx.compose.ui.geometry.Offset(w * 0.5f, w * 0.58f), w / 30f, Slot("done", false))
    leaf(androidx.compose.ui.geometry.Offset(w * 0.5f, w * 0.72f), w / 38f, Slot("done", false), flip = true)
    drawRoundRect(W.steel, androidx.compose.ui.geometry.Offset(w * 0.24f, w * 0.82f), androidx.compose.ui.geometry.Size(w * 0.52f, w * 0.13f),
        androidx.compose.ui.geometry.CornerRadius(w * 0.05f), style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.045f))
}

// ---------- shared pieces ----------

val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.UK)
fun plural(n: Int, w: String) = "$n $w${if (n == 1) "" else "s"}"

// badge: a screen showing no bay puts the robot small beside its title; on a phone it takes the wallet's place.
@Composable
fun Header(title: String, sub: String?, store: Store, ui: Ui, lay: Lay, modifier: Modifier = Modifier, badge: Boolean = false) {
    val small = badge && lay.phone
    Row(modifier.fillMaxWidth().heightIn(min = 72.dp), verticalAlignment = Alignment.CenterVertically) {
        if (badge) RobotView(store, Modifier.padding(end = 10.dp).size(40.dp, 52.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.displaySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (sub != null) Text(sub, Modifier.padding(start = 16.dp).weight(1f, fill = false), style = MaterialTheme.typography.bodyLarge, color = W.dim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (!small) Wallet(store.s)
        if (!lay.rail) IconButton(onClick = { ui.settings = true }, Modifier.padding(start = 4.dp)) { Icon(ICONS.getValue("settings"), "Settings", tint = W.dim) }
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
    var m: Mark? = null
    store.change { m = mark(this, h, d, kind) }
    val k = m ?: return
    val stat = STATS.getValue(h.stat).name
    when (kind) { "done" -> { store.fresh = h.id to now(); store.react("cheer") }; "fail" -> store.react("sad") }
    ui.tell(when (kind) {
        "done" -> "+${plural(k.b, "bolt")}, +${k.p} $stat" + if (k.n > 0) ", and a nut for the streak" else ""
        "fail" -> "Lost ${plural(-k.b, "bolt")} and ${-k.p} $stat"
        else -> "Skipped. Nothing gained, nothing lost."
    }) { store.change { undo(this, h, d) } }
}

fun meta(s: Data, h: Habit, d: LocalDate): String {
    val st = streak(s, h, d)
    return listOfNotNull(
        "${h.target} a ${h.per}", STATS.getValue(h.stat).name, if (h.kind == "bad") "Break it" else null,
        h.days?.let { ds -> ds.sorted().joinToString(" ") { DOW[it] } },
        h.end?.let { "until ${it.format(DateTimeFormatter.ofPattern("d MMM", Locale.UK))}" },
        if (st > 1) "$st ${h.per}s running" else null,
    ).joinToString(" · ")
}
val DOW = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// One habit: name, its slots, one clear Done, and the quieter Skip and Fail. In a narrow column the buttons take a second line.
@Composable
fun HabitRow(store: Store, ui: Ui, h: Habit, d: LocalDate, big: Boolean = false) = BoxWithConstraints {
    val stacked = big || maxWidth < 560.dp
    val s = store.s
    val full = periodMarks(s, h, d).size >= h.target || d > LocalDate.now()
    val label = BTN.getValue(h.kind)
    val marks = @Composable {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            slots(s, h, d).forEachIndexed { i, sl ->
                val own = sl.s != "" && !sl.other
                SlotMark(sl, modifier = if (own) Modifier.clickable(onClickLabel = "undo") {
                    // the newest mark of the day is the one that comes off
                    var m: Mark? = null
                    store.change { m = undo(this, h, d) }
                    m?.let { ui.tell("${mapOf("done" to "Done", "skip" to "Skipped", "fail" to "Failed").getValue(it.s)} mark removed") }
                }.semantics { contentDescription = "${sl.s}, tap to undo" } else Modifier.semantics { contentDescription = if (sl.s == "") "open slot" else sl.s + if (sl.other) ", logged another day" else "" })
            }
        }
    }
    val buttons = @Composable { m: Modifier ->
        Row(m, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { log(store, ui, h, d, "done") }, enabled = !full, modifier = (if (stacked) Modifier.weight(1f) else Modifier).height(if (big) 64.dp else 48.dp),
                shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) { Text(label[0]) }
            OutlinedIconButton(onClick = { log(store, ui, h, d, "skip") }, modifier = Modifier.size(48.dp), enabled = !full, border = BorderStroke(1.dp, W.edge),
                colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = W.steel, disabledContentColor = W.edge)) { Icon(ICONS.getValue("minus"), label[1]) }
            OutlinedIconButton(onClick = { log(store, ui, h, d, "fail") }, modifier = Modifier.size(48.dp), enabled = !full, border = BorderStroke(1.dp, if (full) W.edge else W.coral.copy(alpha = 0.45f)),
                colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = W.coral, disabledContentColor = W.edge)) { Icon(ICONS.getValue("fail"), label[2]) }
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).clickable(onClickLabel = "edit") { ui.edit = h.id; ui.tab = "habits"; ui.settings = false }) {
                Text(h.name, style = if (big) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(meta(s, h, d), style = MaterialTheme.typography.bodyMedium, color = W.dim, maxLines = 2)
            }
            Box(Modifier.padding(horizontal = 12.dp)) { marks() }
            if (!stacked) buttons(Modifier)
        }
        if (stacked) buttons(Modifier.fillMaxWidth().padding(bottom = 12.dp))
        HorizontalDivider(color = W.line)
    }
}

@Composable
fun Note(text: String, modifier: Modifier = Modifier) = Text(text, modifier.padding(vertical = 16.dp), style = MaterialTheme.typography.bodyLarge, color = W.dim)

@Composable
fun Gap(w: androidx.compose.ui.unit.Dp) = Spacer(Modifier.width(w))
