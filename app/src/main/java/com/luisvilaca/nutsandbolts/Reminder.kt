package com.luisvilaca.nutsandbolts

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Notification
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

// The daily reminder: an alarm at the set time that notifies only when the day has nothing logged yet.
class Reminder : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val s = Store.get(c).s
        val t = LocalDate.now()
        if (i.action == FIRE && s.remind.on && s.marks[t].isNullOrEmpty() && t !in s.diary) notify(c, s.name)
        schedule(c, s.remind) // also after a reboot, and tomorrow's after today's fired
    }

    private fun notify(c: Context, name: String) {
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("reminder", "Daily reminder", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        nm.notify(1, Notification.Builder(c, "reminder").setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("$name is waiting").setContentText("Nothing logged today yet.")
            .setContentIntent(open).setAutoCancel(true).build())
    }

    companion object {
        private const val FIRE = "com.luisvilaca.nutsandbolts.REMIND"

        fun schedule(c: Context, r: Remind) {
            val am = c.getSystemService(AlarmManager::class.java)
            val pi = PendingIntent.getBroadcast(c, 0, Intent(c, Reminder::class.java).setAction(FIRE), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            am.cancel(pi)
            if (!r.on) return
            val (h, m) = r.time.split(":").map { it.toInt() }
            var at = LocalDateTime.of(LocalDate.now(), LocalTime.of(h, m))
            if (!at.isAfter(LocalDateTime.now())) at = at.plusDays(1)
            // shortcut: allowed-while-idle alarms can land a few minutes late in deep sleep; exact alarms need a special permission
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pi)
        }
    }
}
