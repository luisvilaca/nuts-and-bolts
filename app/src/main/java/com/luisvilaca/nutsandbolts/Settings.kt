package com.luisvilaca.nutsandbolts

import androidx.compose.ui.graphics.Color
import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(store: Store, ui: Ui, lay: Lay) {
    BackHandler { ui.settings = false }
    val ctx = LocalContext.current
    val s = store.s
    var pending by remember { mutableStateOf<Pair<Data, Map<String, Page>>?>(null) }
    var pendingDate by remember { mutableStateOf<String?>(null) }
    var clock by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(s.name) }

    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            ctx.contentResolver.openOutputStream(uri)!!.use { it.write(store.backup().toByteArray()) }
            store.change { backedUp = LocalDate.now() }
            ui.tell("Backup saved")
        } catch (e: Exception) { ui.tell("The backup could not be made.") }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = try { ctx.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() } } catch (e: Exception) { null }
        val b = text?.let(store::checkBackup)
        if (b == null) ui.tell("That file is not a Nuts And Bolts backup.")
        else { pending = b; pendingDate = Regex("\"saved\":\"([0-9-]+)\"").find(text)?.groupValues?.get(1) }
    }
    val notify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!ok) ui.tell("Notifications are off for Nuts And Bolts, so the reminder cannot show.")
    }
    fun remind(on: Boolean, time: String = s.remind.time) {
        store.change { remind = Remind(on, time) }
        if (on && Build.VERSION.SDK_INT >= 33) notify.launch(Manifest.permission.POST_NOTIFICATIONS)
        Reminder.schedule(ctx, store.s.remind)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = if (lay.wide) 40.dp else 20.dp).padding(bottom = 32.dp)) {
        Header("Settings", null, store, ui, lay)
        Column(Modifier.widthIn(max = 720.dp)) {
            SectionLabel("Robot")
            Field(name, { name = it.take(14); store.change { this.name = name.trim().ifEmpty { "Rivet" } } }, "Rivet")

            SectionLabel("Backup")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                OutlinedButton({ save.launch("nuts-and-bolts-${LocalDate.now()}.json") }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text("Save a backup", color = W.text) }
                OutlinedButton({ open.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text("Restore a backup", color = W.text) }
                Text(s.backedUp?.let { "Last backup " + it.format(DateTimeFormatter.ofPattern("d MMMM", Locale.UK)) } ?: "Never backed up", style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
            Text("One file with every habit, bolt, part and diary page. Keep a copy off this device, in Drive for example.", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Daily reminder")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(s.remind.on, { remind(it) }, colors = SwitchDefaults.colors(checkedTrackColor = W.act, checkedThumbColor = W.onAct, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.edge))
                Text(if (s.remind.on) "On at" else "Off", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
                OutlinedButton({ clock = true }, enabled = s.remind.on, border = BorderStroke(1.dp, W.edge)) { Text(s.remind.time, style = Numeral, color = if (s.remind.on) W.text else W.dim) }
            }
            Text("A notification at this time, only on days with nothing logged yet.", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Sounds")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(s.sound, { v -> store.change { sound = v } }, colors = SwitchDefaults.colors(checkedTrackColor = W.act, checkedThumbColor = W.onAct, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.edge))
                Text("Sound effects", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
            }
            Text("A bolt driven home, a skip, a sheared bolt, a level up, a purchase and an undo each make a short sound.",
                Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Diary")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(s.pen.palm, { v -> store.change { pen.palm = v } }, colors = SwitchDefaults.colors(checkedTrackColor = W.act, checkedThumbColor = W.onAct, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.edge))
                Text("Palm rejection", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
            }
            Text("On: once the pen has written, only the pen writes and a finger scrolls the page. While the pen is near the page, or for a contact as broad as a palm, touches do nothing at all, on the page and on its tools. Off: fingers write too.",
                Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Pen buttons")
            PenButtons(store)
            Spacer(Modifier.height(24.dp))
        }
    }

    pending?.let { b ->
        val when_ = pendingDate?.let { " from " + LocalDate.parse(it).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)) } ?: ""
        AlertDialog(onDismissRequest = { pending = null }, title = { Text("Replace everything?") },
            text = { Text("Everything on this device is replaced with the backup$when_. Habits, robot and diary pages written since then are lost.") },
            confirmButton = { TextButton({
                try { store.restore(b); name = store.s.name; Reminder.schedule(ctx, store.s.remind); ui.tell("Backup restored") }
                catch (e: Exception) { ui.tell("The backup could not be restored.") }
                pending = null
            }) { Text("Replace", color = W.coral) } },
            dismissButton = { TextButton({ pending = null }) { Text("Keep what is here", color = W.text) } })
    }
    if (clock) {
        val (h, m) = s.remind.time.split(":").map { it.toInt() }
        val st = rememberTimePickerState(h, m, is24Hour = true)
        AlertDialog(onDismissRequest = { clock = false }, text = {
            TimePicker(st, colors = TimePickerDefaults.colors(selectorColor = W.act, timeSelectorSelectedContainerColor = W.raised, clockDialColor = W.rail))
        }, confirmButton = { TextButton({ remind(true, "%02d:%02d".format(st.hour, st.minute)); clock = false }) { Text("OK", color = W.act) } },
            dismissButton = { TextButton({ clock = false }) { Text("Cancel", color = W.text) } })
    }
}

// What each of the pen's two side buttons does on a diary page, and a pad to find out which button is which: hover the
// pen over it and press a button, and that button's row lights up.
private val PEN_ACTS = listOf("eraser" to "Pen ↔ eraser", "ink" to "Next ink", "undo" to "Undo", "none" to "Nothing")

@Composable
private fun PenButtons(store: Store) {
    val s = store.s
    var lit by remember { mutableIntStateOf(0) }
    // a pen whose buttons arrive as keys lights the pad too, for a moment
    var tick by remember { mutableIntStateOf(0) }
    androidx.compose.runtime.DisposableEffect(Unit) { PenKeys.on = { b -> lit = b; tick++ }; onDispose { PenKeys.on = null } }
    androidx.compose.runtime.LaunchedEffect(tick) { if (tick > 0) { kotlinx.coroutines.delay(900); lit = 0 } }
    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(12.dp)).background(W.card)
        .background(if (lit > 0) W.selFill else Color.Transparent).border(1.dp, if (lit > 0) W.sel else W.border, RoundedCornerShape(12.dp))
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val bs = awaitPointerEvent().motionEvent?.buttonState ?: continue
                    if (tick == 0 || bs != 0) lit = when { bs and android.view.MotionEvent.BUTTON_STYLUS_PRIMARY != 0 -> 1; bs and android.view.MotionEvent.BUTTON_STYLUS_SECONDARY != 0 -> 2; else -> 0 }
                }
            }
        }, contentAlignment = Alignment.Center) {
        Text(if (lit > 0) "Button ${lit}" else "Press a pen button (hovering over this pad, or anywhere) to see which is which",
            Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium, color = if (lit > 0) W.text else W.dim)
    }
    listOf(1 to s.pen.b1, 2 to s.pen.b2).forEach { (b, cur) ->
        Text("BUTTON $b", Modifier.padding(top = 16.dp, bottom = 8.dp), style = Label.copy(color = if (lit == b) W.text else W.dim))
        Segments(PEN_ACTS, cur) { v -> store.change { if (b == 1) pen.b1 = v else pen.b2 = v } }
    }
    Text("A press works while the pen hovers over the page or writes on it. If a button does nothing here, the tablet may keep it for its own shortcut: turn that off in the tablet's pen settings.",
        Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
}
