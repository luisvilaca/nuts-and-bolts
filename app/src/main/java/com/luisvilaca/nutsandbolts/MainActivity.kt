package com.luisvilaca.nutsandbolts

import android.content.Intent
import android.os.Bundle
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        go.value = intent.getStringExtra(Reminder.TAB)
    }
}
