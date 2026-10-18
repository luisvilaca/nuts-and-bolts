package com.luisvilaca.nutsandbolts

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    private val go = mutableStateOf<String?>(null) // a section to open, from the reminder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = Store.get(this)
        Reminder.schedule(this, store.s.remind) // re-arm after an update or a cleared alarm
        Sfx.load(this)
        go.value = intent.getStringExtra(Reminder.TAB)
        setContent { NbTheme { App(store, go) } }
    }

    // Some pens report their side buttons as keys rather than in the pen's motion; those go to the open diary page (or
    // the Settings test pad). The Xiaomi Focus Pen sends Page Down and Page Up from its own keyboard device, so those
    // count only when they come from a pen, never from a real keyboard.
    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        val pen = e.device?.name?.contains("pen", ignoreCase = true) == true
        val b = when (e.keyCode) {
            KeyEvent.KEYCODE_STYLUS_BUTTON_PRIMARY -> 1; KeyEvent.KEYCODE_STYLUS_BUTTON_SECONDARY -> 2
            KeyEvent.KEYCODE_PAGE_DOWN -> if (pen) 1 else 0; KeyEvent.KEYCODE_PAGE_UP -> if (pen) 2 else 0
            else -> 0
        }
        val on = PenKeys.on
        if (b == 0 || on == null) return super.dispatchKeyEvent(e)
        if (e.action == KeyEvent.ACTION_DOWN && e.repeatCount == 0) on(b)
        return true
    }

    override fun onStop() {
        super.onStop()
        Store.get(this).flush() // the last save reaches the disk before the system may end the app
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        go.value = intent.getStringExtra(Reminder.TAB)
    }
}
