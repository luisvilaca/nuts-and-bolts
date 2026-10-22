package com.luisvilaca.nutsandbolts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate

// The home-screen widget: today's habits, each with its count and one Done (Resisted for a habit to break) that logs
// it without opening the app, exactly as a reminder's button does. Tapping the title opens the Lab. It redraws when
// the app changes the data (App) and after its own Done.
class HabitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = Store.get(context)
        provideContent { Today(store.s) }
    }

    @Composable
    private fun Today(s: Data) {
        val t = LocalDate.now()
        val run = s.habits.filter { active(it, t, s.off) }
        Column(GlanceModifier.fillMaxSize().background(W.ground).cornerRadius(20.dp).padding(14.dp)) {
            Row(GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()), verticalAlignment = Alignment.CenterVertically) {
                Text(s.robot.uppercase(), GlanceModifier.defaultWeight(), style = TextStyle(color = c(W.steel), fontSize = 13.sp, fontWeight = FontWeight.Medium))
                Text("${s.bolts}", style = TextStyle(color = c(W.steel), fontSize = 14.sp, fontWeight = FontWeight.Bold))
                Spacer(GlanceModifier.width(10.dp))
                Text("${s.nuts}", style = TextStyle(color = c(W.gold), fontSize = 14.sp, fontWeight = FontWeight.Bold))
            }
            Spacer(GlanceModifier.height(8.dp))
            if (run.isEmpty()) Text(t(if (s.habits.isEmpty()) "The bench is empty" else "No habits run today."), style = TextStyle(color = c(W.dim), fontSize = 14.sp))
            else LazyColumn {
                items(run, itemId = { it.id.hashCode().toLong() }) { h -> HabitLine(s, h, t) }
            }
        }
    }

    @Composable
    private fun HabitLine(s: Data, h: Habit, t: LocalDate) {
        val n = periodMarks(s, h, t).size
        val open = n < h.target
        Column {
            Row(GlanceModifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(GlanceModifier.defaultWeight()) {
                    Text(h.name, maxLines = 1, style = TextStyle(color = c(W.text), fontSize = 15.sp, fontWeight = FontWeight.Medium))
                    Text("$n / ${h.target}", style = TextStyle(color = c(W.dim), fontSize = 12.sp))
                }
                val word = t(if (!open) "All in" else if (h.kind == Kind.Break) "Resisted" else "Done")
                Box(GlanceModifier.cornerRadius(18.dp).background(if (open) W.act else W.raised).padding(horizontal = 14.dp, vertical = 8.dp)
                    .let { if (open) it.clickable(actionRunCallback<LogDone>(actionParametersOf(HABIT to h.id))) else it }) {
                    Text(word, style = TextStyle(color = c(if (open) W.onAct else W.dim), fontSize = 13.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }

    private fun c(x: Color) = ColorProvider(x)

    companion object { val HABIT = ActionParameters.Key<String>("habit") }
}

// Done from the widget: the same mark the app's Done makes, saved before this process can be ended.
class LogDone : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val store = Store.get(context)
        val t = LocalDate.now()
        val h = store.s.habits.find { it.id == parameters[HabitWidget.HABIT] } ?: return
        if (!active(h, t, store.s.off) || logged(store.s, h, t)) return
        store.change { mark(this, h, t, Outcome.Done) }
        store.flush()
        HabitWidget().updateAll(context)
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = HabitWidget() }
