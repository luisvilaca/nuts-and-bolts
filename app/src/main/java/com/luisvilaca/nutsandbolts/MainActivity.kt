package com.luisvilaca.nutsandbolts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = Store.get(this)
        Reminder.schedule(this, store.s.remind) // re-arm after an update or a cleared alarm
        setContent { NbTheme { App(store) } }
    }
}
