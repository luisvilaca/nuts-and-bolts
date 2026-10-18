package com.luisvilaca.nutsandbolts

import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsSelected
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Renders the real screens at each device class into .impeccable/review/ so layouts can be checked without a device.
// The data is synthetic.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ScreensTest {
    @get:Rule val rule = createComposeRule()
    private val out = File(System.getProperty("user.dir"), "../.impeccable/review").apply { mkdirs() }

    private fun seed(empty: Boolean = false): Store {
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        Settings.Global.putFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f) // a still robot
        val st = Store.get(ctx)
        val t = LocalDate.now()
        st.change {
            habits.clear(); marks.clear(); diary.clear()
            bolts = 0; nuts = 0; stats.keys.forEach { stats[it] = 30 }
            if (empty) return@change
            val water = Habit("w", "Drink water", per = "day", target = 3, stat = "battery", weight = "light", start = t.minusDays(30))
            val read = Habit("r", "Read 20 minutes", per = "day", target = 1, stat = "processor", weight = "medium", start = t.minusDays(30))
            val gym = Habit("g", "Workout", per = "week", target = 3, stat = "chassis", weight = "heavy", start = t.minusDays(30))
            val call = Habit("c", "Call a friend", per = "week", target = 1, stat = "core", weight = "medium", start = t.minusDays(30))
            val phone = Habit("p", "No phone in bed", kind = "bad", per = "day", target = 1, stat = "battery", weight = "medium", start = t.minusDays(30))
            habits += listOf(water, read, gym, call, phone)
            for (i in 1..9L) { mark(this, read, t.minusDays(i), "done"); mark(this, water, t.minusDays(i), "done"); if (i % 3 != 0L) mark(this, water, t.minusDays(i), "done") }
            for (i in listOf(2L, 5L)) mark(this, phone, t.minusDays(i), "fail")
            mark(this, gym, weekStart(t), "done")
            mark(this, water, t, "done"); mark(this, water, t, "done"); mark(this, read, t, "done"); mark(this, phone, t, "fail")
            bolts = 185; nuts = 5
            stats["battery"] = 62; stats["processor"] = 48; stats["chassis"] = 35; stats["core"] = 22
            diary += listOf(t.minusDays(1), t.minusDays(3), t.minusDays(4))
        }
        return st
    }

    private fun shot(name: String) {
        rule.waitForIdle()
        File(out, "$name.png").outputStream().use { rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun tap(text: String) { rule.onAllNodesWithText(text, useUnmergedTree = true)[0].performClick(); rule.waitForIdle() }

    private fun tour(prefix: String) {
        val st = seed()
        rule.setContent { NbTheme { App(st) } }
        shot("$prefix-today")
        tap("Shop"); shot("$prefix-shop")
        tap("Habits"); shot("$prefix-habits")
        tap("Calendar"); shot("$prefix-habits-cal"); tap("Day")
        tap("Add habit"); shot("$prefix-form")
        tap("Cancel"); tap("Diary"); shot("$prefix-diary")
        tap("Open today".takeIf { LocalDate.now() in st.s.diary } ?: "Write today"); shot("$prefix-page")
    }

    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun tabletLandscape() = tour("tablet")
    @Test @Config(qualifiers = "w854dp-h1280dp-port-hdpi") fun tabletPortrait() = tour("tablet-portrait")
    @Test @Config(qualifiers = "w411dp-h891dp-port-hdpi") fun phone() = tour("phone")
    @Test @Config(qualifiers = "w891dp-h411dp-land-hdpi") fun phoneLandscape() = tour("phone-landscape")

    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun emptyAndSettings() {
        val st = seed(empty = true)
        rule.setContent { NbTheme { App(st) } }
        shot("tablet-empty")
        rule.onNodeWithContentDescription("Settings").performClick(); rule.waitForIdle(); shot("tablet-settings")
    }

    // Every part on its own tile, the robot in the workshop parts, and the bay at each base tier, to check the art by eye.
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun catalogue() {
        val st = seed()
        // a robot per finish family, to see the palettes side by side
        val kits = listOf(
            mapOf("head" to "head-weld", "body" to "body-tokamak", "arms" to "arms-welder", "legs" to "legs-piston", "acc" to "acc-hardhat"),
            mapOf("head" to "head-binoc", "body" to "body-tank", "arms" to "arms-flex", "legs" to "legs-spring", "acc" to "acc-pack"),
            mapOf("head" to "head-cyclops", "body" to "body-rocket", "arms" to "arms-fin", "legs" to "legs-hover", "acc" to "acc-dosimeter"),
            mapOf("head" to "head-dome", "body" to "body-capsule", "arms" to "arms-mitt", "legs" to "legs-boots", "acc" to "acc-drone"),
            mapOf("head" to "head-saucer", "body" to "body-gumball", "arms" to "arms-tentacle", "legs" to "legs-spider", "acc" to "acc-crown"),
            mapOf("head" to "head-hex", "body" to "body-reactor", "arms" to "arms-magnet", "legs" to "legs-tread", "acc" to "acc-sprout"),
            mapOf("head" to "head-monitor", "body" to "body-crate", "arms" to "arms-wrench", "legs" to "legs-wheel", "acc" to "acc-prop"),
        )
        rule.setContent { NbTheme { Column(Modifier.background(W.ground).padding(8.dp)) {
            PARTS.filter { it.slot != "base" }.chunked(14).forEach { r -> Row { r.forEach { p -> Canvas(Modifier.padding(2.dp).size(86.dp).background(W.raised)) { thumb(p) } } } }
            Row { kits.forEach { RobotView(st, Modifier.size(110.dp, 143.dp), it) } }
            Row { PARTS.filter { it.slot == "base" }.forEach { b -> BayPanel(st, Modifier.size(252.dp, 230.dp), orbitsFor(st.s, LocalDate.now()), st.s.equip + ("base" to b.id)) } }
        } } }
        shot("catalogue")
    }

    // Settings saved before the sliders (three-step choices) map onto the new mm and percent values.
    @Test fun oldPenSettingsCarryOver() {
        fun load(pen: String) = dataFrom(json(Data()).put("pen", org.json.JSONObject(pen))).pen
        val old = load("""{"color":"#E0F7FA","size":2,"stab":0,"eraser":2}""")
        org.junit.Assert.assertEquals(listOf(1f, 0, 7f), listOf(old.nib, old.stab, old.rub))
        val new = load("""{"nib":0.8,"stabPct":37,"rub":40}""")
        org.junit.Assert.assertEquals(listOf(0.8f, 30, 15f), listOf(new.nib, new.stab, new.rub)) // stab snaps to tens, rub clamps
    }

    // Each pen button takes its action from Settings, and the choice shows as selected.
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun penButtonChoices() {
        val st = seed()
        rule.setContent { NbTheme { App(st) } }
        rule.onNodeWithContentDescription("Settings").performClick(); rule.waitForIdle()
        rule.onAllNodesWithText("Undo")[0].performScrollTo().performClick(); rule.waitForIdle()
        org.junit.Assert.assertEquals("undo", st.s.pen.b1)
        rule.onAllNodesWithText("Undo")[0].assertIsSelected()
        rule.onAllNodesWithText("Nothing")[1].performScrollTo().performClick(); rule.waitForIdle()
        org.junit.Assert.assertEquals("none", st.s.pen.b2)
        rule.onAllNodesWithText("Nothing")[1].assertIsSelected()
    }

    // A pen button that picks the next ink moves the selection in the ink swatches with it.
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun penButtonInkShows() {
        val st = seed()
        st.change { pen.color = PEN_COLORS[0].second; pen.b2 = "ink" }
        rule.setContent { NbTheme { App(st) } }
        tap("Diary"); tap("Write today".takeIf { LocalDate.now() !in st.s.diary } ?: "Open today")
        rule.runOnUiThread { PenKeys.on!!(2) }; rule.waitForIdle()
        rule.onNodeWithContentDescription(PEN_COLORS[1].first).assertIsSelected()
    }

    // A long press on Done opens the card's menu without logging; Delete there comes off with an Undo that restores it.
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun cardMenuAndDelete() {
        val st = seed()
        rule.setContent { NbTheme { App(st) } }
        tap("Habits")
        val before = st.s.bolts
        rule.onAllNodesWithText("Done")[0].performTouchInput { longClick() }; rule.waitForIdle()
        org.junit.Assert.assertEquals(before, st.s.bolts)
        tap("Delete habit"); tap("Delete"); rule.waitForIdle()
        org.junit.Assert.assertEquals(4, st.s.habits.size)
        rule.onNodeWithText("Undo").performClick(); rule.waitForIdle()
        org.junit.Assert.assertEquals("w", st.s.habits[0].id)
    }

    // The pen and eraser menus open with their sliders and values (popups are not in the screenshots).
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun penMenus() {
        val st = seed()
        rule.setContent { NbTheme { App(st) } }
        tap("Diary"); tap("Write today".takeIf { LocalDate.now() !in st.s.diary } ?: "Open today")
        rule.onNodeWithContentDescription("Pen. Tap again for nib and stabilizer").performClick(); rule.waitForIdle()
        rule.onNodeWithText("NIB").assertExists(); rule.onNodeWithText("0.6 mm").assertExists(); rule.onNodeWithText("50%").assertExists()
        rule.onNodeWithContentDescription("Eraser. Tap again for its size").performClick(); rule.waitForIdle()
        rule.onNodeWithContentDescription("Eraser. Tap again for its size").performClick(); rule.waitForIdle()
        rule.onNodeWithText("4 mm").assertExists()
    }
}
