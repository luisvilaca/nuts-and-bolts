package com.luisvilaca.nutsandbolts

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(store: Store, ui: Ui, lay: Lay) {
    BackHandler { ui.settings = false }
    val ctx = LocalContext.current
    val s = store.s
    var pending by remember { mutableStateOf<Pair<Data, Map<String, Page>>?>(null) }
    var pendingDate by remember { mutableStateOf<String?>(null) }
    var clock by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(s.robot) } // the default name in this language and look (Rebite, Freddy)

    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        // every diary page is read for the file, so the work runs off the main thread
        ui.scope.launch {
            val ok = withContext(Dispatchers.IO) { runCatching { ctx.contentResolver.openOutputStream(uri)!!.use { it.write(store.backup().toByteArray()) } }.isSuccess }
            if (ok) { store.change { backedUp = LocalDate.now() }; ui.tell(t("Backup saved")) } else ui.tell(t("The backup could not be made."))
        }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        ui.scope.launch {
            val text = withContext(Dispatchers.IO) { runCatching { ctx.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() } }.getOrNull() }
            val b = text?.let { withContext(Dispatchers.Default) { store.checkBackup(it) } }
            if (b == null) ui.tell(t("That file is not a Nuts And Bolts backup."))
            else { pending = b; pendingDate = Regex("\"saved\":\"([0-9-]+)\"").find(text)?.groupValues?.get(1) }
        }
    }
    val notify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!ok) ui.tell(t("Notifications are off for Nuts And Bolts, so the reminder cannot show."))
    }
    fun remind(on: Boolean, time: String = s.remind.time) {
        store.change { remind = Remind(on, time) }
        if (on && Build.VERSION.SDK_INT >= 33) notify.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = if (lay.wide) 40.dp else 20.dp).padding(bottom = 32.dp)) {
        Header(t("Settings"), store, ui, lay)
        // wide: the device and account on the left, sounds and the pen on the right, so half the screen is not bare
        val left = @Composable {
            SectionLabel("Robot")
            Field(name, { name = it.take(14); store.change { this.name = name.trim().ifEmpty { "Rivet" } } }, t("Rivet"))

            SectionLabel("Language")
            // each language named in itself; System follows the phone (and Android's own per-app language setting)
            Segments(Lang.CHOICES.map { (k, v) -> k to if (k.isEmpty()) t(v) else v }, s.lang) { v -> store.change { lang = v } }

            SectionLabel("Sync")
            SyncRow(ui)
            if (Sync.ready(ctx)) {
                SectionLabel("Buddy")
                BuddyRow(store, ui)
            }

            SectionLabel("Backup")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                OutlinedButton({ save.launch("nuts-and-bolts-${LocalDate.now()}.json") }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text(t("Save a backup"), color = W.text) }
                OutlinedButton({ open.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text(t("Restore a backup"), color = W.text) }
                Text(s.backedUp?.let { t("Last backup {0}", fmt(it, "d MMMM")) } ?: t("Never backed up"), style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
            Text(t("One file with every habit, bolt, part and diary page. Keep a copy off this device, in Drive for example."), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Daily reminder")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(s.remind.on, { remind(it) }, colors = SwitchDefaults.colors(checkedTrackColor = W.act, checkedThumbColor = W.onAct, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.edge))
                Text(t(if (s.remind.on) "On at" else "Off"), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
                OutlinedButton({ clock = true }, enabled = s.remind.on, border = BorderStroke(1.dp, W.edge)) { Text(s.remind.time, style = Numeral, color = if (s.remind.on) W.text else W.dim) }
            }
            Text(t("A notification at this time, only on days with nothing logged yet."), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        }
        val right = @Composable {
            SectionLabel("Sounds")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(s.sound, { v -> store.change { sound = v }; Sfx.on = v; Sfx.play("chirp") }, colors = SwitchDefaults.colors(checkedTrackColor = W.act, checkedThumbColor = W.onAct, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.edge))
                Text(t("Sound effects"), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
            }
            Text(t("The workshop's own sounds, at the media volume: a bolt driven home, a frame going critical, a sheared bolt, a level gained or lost, a nut, a purchase, a part fitted, a habit added or taken off the bench, and {0} answering a tap.", s.robot),
                Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Diary")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(s.pen.palm, { v -> store.change { pen.palm = v } }, colors = SwitchDefaults.colors(checkedTrackColor = W.act, checkedThumbColor = W.onAct, uncheckedTrackColor = W.rail, uncheckedBorderColor = W.edge))
                Text(t("Palm rejection"), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyLarge)
            }
            Text(t("On: while the pen is near the page, or for a contact as broad as a palm, touches do nothing at all, on the page and on its tools, and once the pen has written a finger moves the page instead of writing (the Move button on the page's rail switches it back). Off: touches always count."),
                Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)

            SectionLabel("Pen buttons")
            PenButtons(store)
            Spacer(Modifier.height(24.dp))
        }
        if (lay.wide) Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
            Column(Modifier.weight(1f)) { left() }
            Column(Modifier.weight(1f)) { right() }
        } else Column(Modifier.widthIn(max = 720.dp)) { left(); right() }
    }

    pending?.let { b ->
        val when_ = pendingDate?.let { t(" from {0}", fmt(LocalDate.parse(it), "d MMMM yyyy")) } ?: ""
        AlertDialog(onDismissRequest = { pending = null }, title = { Text(t("Replace everything?")) },
            text = { Text(t("Everything on this device is replaced with the backup{0}. Habits, robot and diary pages written since then are lost.", when_)) },
            confirmButton = { TextButton({
                pending = null
                ui.scope.launch {
                    val ok = withContext(Dispatchers.IO) { runCatching { store.restore(b) }.isSuccess }
                    if (ok) { name = store.s.robot; Sync.restored(); ui.tell(t("Backup restored")) } else ui.tell(t("The backup could not be restored."))
                }
            }) { Text(t("Replace"), color = W.coral) } },
            dismissButton = { TextButton({ pending = null }) { Text(t("Keep what is here"), color = W.text) } })
    }
    if (clock) {
        val (h, m) = s.remind.time.split(":").map { it.toInt() }
        val st = rememberTimePickerState(h, m, is24Hour = true)
        AlertDialog(onDismissRequest = { clock = false }, text = {
            TimePicker(st, colors = TimePickerDefaults.colors(selectorColor = W.act, timeSelectorSelectedContainerColor = W.raised, clockDialColor = W.rail))
        }, confirmButton = { TextButton({ remind(true, "%02d:%02d".format(st.hour, st.minute)); clock = false }) { Text(t("OK"), color = W.act) } },
            dismissButton = { TextButton({ clock = false }) { Text(t("Cancel"), color = W.text) } })
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
    DisposableEffect(Unit) { PenKeys.on = { b -> lit = b; tick++ }; onDispose { PenKeys.on = null } }
    LaunchedEffect(tick) { if (tick > 0) { kotlinx.coroutines.delay(900); lit = 0 } }
    Box(Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(12.dp)).background(W.card)
        .background(if (lit > 0) W.selFill else Color.Transparent).border(1.dp, if (lit > 0) W.sel else W.border, RoundedCornerShape(12.dp))
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val bs = awaitPointerEvent().motionEvent?.buttonState ?: continue
                    if (tick == 0 || bs != 0) lit = when { bs and android.view.MotionEvent.BUTTON_STYLUS_PRIMARY != 0 -> 1; bs and android.view.MotionEvent.BUTTON_STYLUS_SECONDARY != 0 -> 2; else -> 0 }
                }
            }
        }, contentAlignment = Alignment.Center) {
        Text(if (lit > 0) t("Button {0}", lit) else t("Press a pen button (hovering over this pad, or anywhere) to see which is which"),
            Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium, color = if (lit > 0) W.text else W.dim)
    }
    listOf(1 to s.pen.b1, 2 to s.pen.b2).forEach { (b, cur) ->
        Text(t("BUTTON {0}", b), Modifier.padding(top = 16.dp, bottom = 8.dp), style = Label.copy(color = if (lit == b) W.text else W.dim))
        Segments(PEN_ACTS.map { (k, v) -> k to t(v) }, cur) { v -> store.change { if (b == 1) pen.b1 = v else pen.b2 = v } }
    }
    Text(t("A press works while the pen hovers over the page or writes on it. If a button does nothing here, the tablet may keep it for its own shortcut: turn that off in the tablet's pen settings."),
        Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
}

// Signing in with the same Google account on the tablet and the phone keeps one copy of everything on both.
@Composable
private fun SyncRow(ui: Ui) {
    val ctx = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    if (!Sync.ready(ctx)) {
        Text(t("Sync is not set up in this build."), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        return
    }
    val who = Sync.who
    var byMail by remember { mutableStateOf(false) }
    var mail by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
        if (who == null) {
            Button({
                busy = true
                ui.scope.launch { Sync.signIn(ctx)?.let { if (it.isNotEmpty()) ui.tell(it) }; busy = false }
            }, Modifier.height(44.dp), enabled = !busy, shape = CircleShape, colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) { Text(t("Sign in with Google")) }
            if (!byMail) TextButton({ byMail = true }, Modifier.height(44.dp)) { Text(t("Or use an email and password"), color = W.text) }
        }
        else {
            Column {
                Text(who, style = MaterialTheme.typography.bodyLarge)
                Text(Sync.state, style = MaterialTheme.typography.bodyMedium, color = W.dim)
            }
            OutlinedButton({ ui.scope.launch { Sync.signOut(ctx) } }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) { Text(t("Sign out"), color = W.text) }
        }
    }
    // an account by email and password, for someone who would rather not use Google's
    if (who == null && byMail) Column(Modifier.padding(top = 12.dp).widthIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Field(mail, { mail = it.take(80) }, t("Email"))
        Field(pass, { pass = it.take(64) }, t("Password"), password = true)
        fun go(create: Boolean) {
            busy = true
            ui.scope.launch { Sync.signInEmail(mail, pass, create)?.let { ui.tell(it) }; busy = false }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
            Button({ go(false) }, Modifier.height(44.dp), enabled = !busy && mail.isNotBlank() && pass.isNotEmpty(), shape = CircleShape,
                colors = ButtonDefaults.buttonColors(W.act, W.onAct, W.raised, W.dim)) { Text(t("Sign in")) }
            OutlinedButton({ go(true) }, Modifier.height(44.dp), enabled = !busy && mail.isNotBlank() && pass.isNotEmpty(), border = BorderStroke(1.dp, W.edge)) { Text(t("Create account"), color = W.text) }
            TextButton({
                if (mail.isBlank()) ui.tell(t("Type your email first."))
                else ui.scope.launch { ui.tell(Sync.resetPassword(mail) ?: t("A link to choose a new password was sent to {0}.", mail.trim())) }
            }, Modifier.height(44.dp)) { Text(t("Forgot password?"), color = W.dim) }
        }
    }
    Text(t(if (who == null) "Sign in with the same Google account on the tablet and the phone, and both keep the same habits, robot and diary pages. Changes made offline go across once the device is back online."
        else "Every change goes to the other device as it is made. Signing out keeps everything on this device."),
        Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
}

// One person invites the other by their Google email; the other accepts on their phone (here, on the Lab, or from the
// notification), and then each app shows the other's character and their day, and either can send a cheer.
@Composable
private fun BuddyRow(store: Store, ui: Ui) {
    var email by remember { mutableStateOf(Buddy.buddy) }
    LaunchedEffect(Buddy.buddy) { email = Buddy.buddy }
    if (Sync.who == null) {
        Text(t("Sign in under Sync first."), style = MaterialTheme.typography.bodyMedium, color = W.dim)
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
        Field(email, { email = it.take(80) }, t("Your buddy's Google email"), Modifier.widthIn(max = 360.dp))
        OutlinedButton({ Buddy.link(email, store)?.let { ui.tell(it) } }, Modifier.height(44.dp), border = BorderStroke(1.dp, W.edge)) {
            Text(t(if (email.isBlank() && Buddy.buddy.isNotEmpty()) "Unlink" else "Invite"), color = W.text)
        }
    }
    val b = Buddy.buddy
    Text(when {
        b.isEmpty() -> t("Type your buddy's Google email and send an invitation. Once they accept it, you both see each other's character and today's progress, and can send a cheer.")
        Buddy.them == null -> t("Invitation sent to {0}. Once they accept it on their phone, you are linked.", b)
        else -> t("Linked with {0}.", b)
    }, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = W.dim)
    Buddy.invites.forEach { InviteCard(it, store, ui) }
}
