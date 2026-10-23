package com.luisvilaca.nutsandbolts

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import androidx.compose.ui.graphics.toArgb

// Notifications, all on alarms set on this device (they need no network): the daily reminder, at the set time, only
// when the day has nothing logged and no page; and each habit's own reminder, at its time on the days it runs, only
// while its period still has open slots. A habit's notification carries Done (Resisted for a habit to break), which
// logs it without opening the app. Every alarm sets the next one when it fires, and all are set again at boot, at
// launch and whenever the habits or their times change (App).
class Reminder : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val store = Store.get(c)
        val s = store.s
        val t = LocalDate.now()
        val h = i.data?.lastPathSegment?.let { id -> s.habits.find { it.id == id } }
        when (i.action) {
            FIRE -> if (s.remind.on && t !in s.off && s.marks[t].isNullOrEmpty() && t !in s.diary) notify(c, 1, DAY, t("{0} is waiting", s.robot), t("Nothing logged today yet."))
            HABIT -> if (h?.remind != null && active(h, t, s.off) && !logged(s, h, t)) notifyHabit(c, s, h, t)
            DONE -> if (h != null && active(h, t, s.off)) {
                store.change { mark(this, h, t, Outcome.Done) }
                store.flush() // the app may not be running: the mark reaches the disk before this process can be ended
                c.getSystemService(NotificationManager::class.java).cancel(h.id.hashCode())
            }
        }
        scheduleAll(c, store.s) // also after a reboot, and tomorrow's after today's fired
    }

    companion object {
        private const val FIRE = "com.luisvilaca.nutsandbolts.REMIND"
        private const val HABIT = "com.luisvilaca.nutsandbolts.HABIT"
        private const val DONE = "com.luisvilaca.nutsandbolts.DONE"
        const val TAB = "tab" // the section a notification opens
        private const val DAY = "day"
        private const val HABITS = "habits"
        const val BUDDY = "buddy"

        private fun notifyHabit(c: Context, s: Data, h: Habit, t: LocalDate) {
            val p = slots(s, h, t)
            val word = t(if (h.kind == Kind.Break) "Resisted" else "Done")
            val done = PendingIntent.getBroadcast(c, 0, Intent(c, Reminder::class.java).setAction(DONE).setData(uri(h)), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val left = h.target - p.count { it.s != null }
            notify(c, h.id.hashCode(), HABITS, h.name,
                (if (h.target == 1) "" else t("{0} of {1} in", p.count { it.s == Outcome.Done }, plural(h.target, "bolt")) + " · ") + t(if (h.per == Per.Week) "{0} still open this week" else "{0} still open today", plural(left, "bolt")),
                Notification.Action.Builder(Icon.createWithResource(c, R.drawable.ic_stat), word, done).build())
        }

        // tab: the section tapping it opens
        internal fun notify(c: Context, id: Int, channel: String, title: String, text: String, action: Notification.Action? = null, tab: Tab = Tab.Habits) {
            val nm = c.getSystemService(NotificationManager::class.java)
            // the robot's own call, not the system ping; a channel's sound is fixed once made, hence new channel ids
            val sound = Uri.parse("android.resource://${c.packageName}/${R.raw.sfx_notify}")
            val att = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            nm.deleteNotificationChannel("reminder") // the first build's channel, with the default sound
            nm.createNotificationChannel(NotificationChannel(DAY, t("Daily reminder"), NotificationManager.IMPORTANCE_DEFAULT).apply { setSound(sound, att) })
            nm.createNotificationChannel(NotificationChannel(HABITS, t("Habit reminders"), NotificationManager.IMPORTANCE_HIGH).apply { setSound(sound, att) })
            nm.createNotificationChannel(NotificationChannel(BUDDY, t("Buddy cheers"), NotificationManager.IMPORTANCE_HIGH).apply { setSound(sound, att) })
            val open = PendingIntent.getActivity(c, tab.ordinal, Intent(c, MainActivity::class.java).putExtra(TAB, tab.name)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            nm.notify(id, Notification.Builder(c, channel).setSmallIcon(R.drawable.ic_stat).setColor(W.act.toArgb())
                .setContentTitle(title).setContentText(text).setContentIntent(open).setAutoCancel(true)
                .apply { if (action != null) addAction(action) }.build())
        }

        private fun uri(h: Habit) = Uri.parse("nutsandbolts://habit/${h.id}") // one alarm per habit: intents differ by data

        fun scheduleAll(c: Context, s: Data) {
            val am = c.getSystemService(AlarmManager::class.java)
            val daily = PendingIntent.getBroadcast(c, 0, Intent(c, Reminder::class.java).setAction(FIRE), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            am.cancel(daily)
            if (s.remind.on) next(s.remind.time) { true }?.let { alarm(am, it, daily) }
            // a deleted habit's alarm is left to fire once: it finds no habit, shows nothing and sets nothing
            s.habits.forEach { h ->
                val pi = PendingIntent.getBroadcast(c, 0, Intent(c, Reminder::class.java).setAction(HABIT).setData(uri(h)), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                am.cancel(pi)
                h.remind?.let { r -> next(r) { active(h, it, s.off) }?.let { alarm(am, it, pi) } }
            }
        }

        // the next time "HH:mm" comes round on a day that runs, within a year
        internal fun next(time: String, now: LocalDateTime = LocalDateTime.now(), runs: (LocalDate) -> Boolean): LocalDateTime? {
            val (hh, mm) = time.split(":").map { it.toInt() }
            return (0..366L).map { LocalDateTime.of(now.toLocalDate().plusDays(it), LocalTime.of(hh, mm)) }.firstOrNull { it.isAfter(now) && runs(it.toLocalDate()) }
        }

        // On time to the minute: exact alarms are granted to reminder apps (USE_EXACT_ALARM, Android 13+; earlier ones
        // grant SCHEDULE_EXACT_ALARM). Where they are refused, an inexact alarm can land a few minutes late in deep sleep.
        // shortcut: Google Play allows USE_EXACT_ALARM only for alarm and calendar apps; if the app ever goes to Play, ask for SCHEDULE_EXACT_ALARM in Settings instead
        private fun alarm(am: AlarmManager, at: LocalDateTime, pi: PendingIntent) {
            val ms = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pi)
        }
    }
}
