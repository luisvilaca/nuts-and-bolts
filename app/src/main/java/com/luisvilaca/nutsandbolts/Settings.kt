package com.luisvilaca.nutsandbolts

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
            OutlinedTextField(name, { name = it.take(14); store.change { this.name = name.trim().ifEmpty { "Rivet" } } }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = W.turq, unfocusedBorderColor = W.orbit, cursorColor = W.turq, focusedLabelColor = W.turq))

            SectionLabel("Backup")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                OutlinedButton({ save.launch("nuts-and-bolts-${LocalDate.now()}.json") }, Modifier.height(48.dp), border = BorderStroke(1.dp, W.orbit)) { Text("Save a backup", color = W.text) }
                OutlinedButton({ open.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) }, Modifier.height(48.dp), border = BorderStroke(1.dp, W.orbit)) { Text("Restore a backup", color = W.text) }
                Text(s.backedUp?.let { "Last backup " + it.format(DateTimeFormatter.ofPattern("d MMMM", Locale.UK)) } ?: "Never backed up", style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
            Text("One file with every habit, mark, part and diary page. Keep a copy off this device, in Drive for example.", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Daily reminder")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(s.remind.on, { remind(it) }, colors = SwitchDefaults.colors(checkedTrackColor = W.turq, checkedThumbColor = W.onTurq, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.orbit))
                Text(if (s.remind.on) "On at" else "Off", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
                OutlinedButton({ clock = true }, enabled = s.remind.on, border = BorderStroke(1.dp, W.orbit)) { Text(s.remind.time, style = Numeral, color = if (s.remind.on) W.text else W.dim) }
            }
            Text("A notification at this time, only on days with nothing logged yet.", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
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
            TimePicker(st, colors = TimePickerDefaults.colors(selectorColor = W.turq, timeSelectorSelectedContainerColor = W.raised, clockDialColor = W.rail))
        }, confirmButton = { TextButton({ remind(true, "%02d:%02d".format(st.hour, st.minute)); clock = false }) { Text("OK", color = W.turq) } },
            dismissButton = { TextButton({ clock = false }) { Text("Cancel", color = W.text) } })
    }
}
