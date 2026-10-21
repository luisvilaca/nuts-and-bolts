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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.compose.ui.geometry.Offset
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
            val water = Habit("w", "Drink water", per = Per.Day, target = 3, stat = Attr.Battery, weight = Heft.Light, start = t.minusDays(30))
            val read = Habit("r", "Read 20 minutes", per = Per.Day, target = 1, stat = Attr.Processor, weight = Heft.Medium, start = t.minusDays(30))
            val gym = Habit("g", "Workout", per = Per.Week, target = 3, stat = Attr.Chassis, weight = Heft.Heavy, start = t.minusDays(30))
            val call = Habit("c", "Call a friend", per = Per.Week, target = 1, stat = Attr.Core, weight = Heft.Medium, start = t.minusDays(30))
            val phone = Habit("p", "No phone in bed", kind = Kind.Break, per = Per.Day, target = 1, stat = Attr.Battery, weight = Heft.Medium, start = t.minusDays(30))
            habits += listOf(water, read, gym, call, phone)
            for (i in 1..9L) { mark(this, read, t.minusDays(i), Outcome.Done); mark(this, water, t.minusDays(i), Outcome.Done); if (i % 3 != 0L) mark(this, water, t.minusDays(i), Outcome.Done) }
            for (i in listOf(2L, 5L)) mark(this, phone, t.minusDays(i), Outcome.Fail)
            mark(this, gym, weekStart(t), Outcome.Done)
            mark(this, water, t, Outcome.Done); mark(this, water, t, Outcome.Done); mark(this, read, t, Outcome.Done); mark(this, phone, t, Outcome.Fail)
            bolts = 185; nuts = 5
            stats[Attr.Battery] = 62; stats[Attr.Processor] = 48; stats[Attr.Chassis] = 35; stats[Attr.Core] = 22
            diary += listOf(t.minusDays(1), t.minusDays(3), t.minusDays(4))
        }
        st.writePage(t, Page(PAPERS[0], mutableListOf())); st.flush() // today blank, whatever an earlier test wrote on it
        // a written page, so the diary has one on disk
        if (!empty) st.writePage(t.minusDays(1), Page("#393939", mutableListOf(
            InkStroke("#00FF88", 3f, false, floatArrayOf(160f, 300f, .6f, 700f, 300f, .6f)),
            InkStroke("#E0F7FA", 2f, false, FloatArray(60) { i -> when (i % 3) { 0 -> 160f + i * 20f; 1 -> 420f + (if (i % 2 == 0) 30f else 0f) + (i / 15) * 120f; else -> .6f } }),
        )))
        return st
    }

    private fun shot(name: String) {
        rule.waitForIdle()
        File(out, "$name.png").outputStream().use { rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    // today's cell in the diary calendar opens today's page
    private fun openToday() { rule.onNodeWithContentDescription(LocalDate.now().format(dayFmt), substring = true).performClick(); rule.waitForIdle() }

    private fun tap(text: String) { rule.onAllNodesWithText(text, useUnmergedTree = true)[0].performClick(); rule.waitForIdle() }

    // font: the system font scale to render at; 1.3 checks large text clips nothing
    private fun tour(prefix: String, font: Float = 1f) {
        val st = seed()
        rule.setContent { NbTheme { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, font)) { App(st) } } }
        shot("$prefix-today")
        tap("Shop"); shot("$prefix-shop")
        tap("Habits"); shot("$prefix-habits")
        tap("Calendar"); shot("$prefix-habits-cal"); tap("History"); shot("$prefix-habits-hist"); tap("Day")
        tap("Add habit"); shot("$prefix-form")
        tap("Cancel"); tap("Diary")
        shot("$prefix-diary")
        openToday(); shot("$prefix-page")
    }

    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun tabletLandscape() = tour("tablet")
    @Test @Config(qualifiers = "w854dp-h1280dp-port-hdpi") fun tabletPortrait() = tour("tablet-portrait")
    @Test @Config(qualifiers = "w411dp-h891dp-port-hdpi") fun phone() = tour("phone")
    @Test @Config(qualifiers = "w891dp-h411dp-land-hdpi") fun phoneLandscape() = tour("phone-landscape")
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun tabletLargeText() = tour("large-tablet", 1.3f)
    @Test @Config(qualifiers = "w411dp-h891dp-port-hdpi") fun phoneLargeText() = tour("large-phone", 1.3f)

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

    // A state file from before versions loads (no "v", no peak, the old pen), the codes come back typed, and saving
    // writes the very same strings, so older backups and newer builds read each other.
    @Test fun oldStateLoads() {
        val old = org.json.JSONObject("""{"name":"Rivet","bolts":40,"nuts":1,"stats":{"battery":65,"processor":30,"chassis":30,"core":30},
            "owned":["head-box"],"equip":{"head":"head-box"},"habits":[{"id":"a","name":"Walk","kind":"bad","per":"week","target":3,"stat":"core","weight":"heavy"}],
            "marks":{"2026-10-12":{"a":[["done",20,4,0]]}},"pen":{"size":2}}""")
        val d = dataFrom(old)
        val h = d.habits.single()
        org.junit.Assert.assertEquals(listOf(Kind.Break, Per.Week, Attr.Core, Heft.Heavy), listOf(h.kind, h.per, h.stat, h.weight))
        org.junit.Assert.assertEquals(Outcome.Done, d.marks.values.single().values.single().single().s)
        org.junit.Assert.assertEquals(level(65), d.peak[Attr.Battery]) // peak made up from the level reached
        org.junit.Assert.assertEquals(1f, d.pen.nib)
        val back = json(d)
        org.junit.Assert.assertEquals(listOf(SCHEMA, "bad", "week", "core", "heavy", "done"), listOf(back.getInt("v"),
            back.getJSONArray("habits").getJSONObject(0).getString("kind"), back.getJSONArray("habits").getJSONObject(0).getString("per"),
            back.getJSONArray("habits").getJSONObject(0).getString("stat"), back.getJSONArray("habits").getJSONObject(0).getString("weight"),
            back.getJSONObject("marks").getJSONObject("2026-10-12").getJSONArray("a").getJSONArray(0).getString(0)))
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
        tap("Diary"); openToday()
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

    // An edit opened on the Lab returns to the Lab: the form opens over the tab that asked for it.
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun editReturnsToLab() {
        val st = seed()
        rule.setContent { NbTheme { App(st) } }
        tap("Drink water"); rule.onNodeWithText("Edit habit").assertExists()
        tap("Cancel")
        rule.onNodeWithText("Edit habit").assertDoesNotExist()
        org.junit.Assert.assertEquals(2, rule.onAllNodesWithText("Lab").fetchSemanticsNodes().size) // the title and the tab
    }

    // Trying parts on across categories builds one outfit for the visit; leaving the Shop clears it.
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun tryOnOutfit() {
        val st = seed()
        rule.setContent { NbTheme { App(st) } }
        tap("Shop"); tap("Dome"); tap("Arms"); tap("Wrenches")
        rule.onNodeWithText("Trying on: Dome, Wrenches").assertExists()
        shot("tablet-shop-tryon")
        tap("Lab"); tap("Shop")
        rule.onNodeWithText("Trying on: Dome, Wrenches").assertDoesNotExist()
        org.junit.Assert.assertEquals("head-box", st.s.equip["head"]) // trying on buys nothing
    }

    // A dozen bolts on one frame still stand below the light rail.
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun crowdedBench() {
        val st = seed()
        st.change { habits += Habit("x", "Twelve", per = Per.Day, target = 12, stat = Attr.Core, start = LocalDate.now().minusDays(1)) }
        rule.setContent { NbTheme { BayPanel(st, Modifier.size(600.dp, 600.dp), orbitsFor(st.s, LocalDate.now())) } }
        shot("bench-twelve")
    }

    // An ink that would vanish on the paper is flagged: ice blue on cream, matte black on matte black.
    @Test fun faintInks() {
        org.junit.Assert.assertTrue(contrast("#E0F7FA", "#F4ECD8") < 1.5f)
        org.junit.Assert.assertTrue(contrast("#393939", "#393939") < 1.5f)
        org.junit.Assert.assertTrue(contrast("#E0F7FA", "#393939") >= 1.5f)
    }

    // The pen and eraser menus open with their sliders and values (popups are not in the screenshots).
    @Test @Config(qualifiers = "w1280dp-h854dp-land-hdpi") fun penMenus() {
        val st = seed()
        rule.setContent { NbTheme { App(st) } }
        tap("Diary"); openToday()
        rule.onNodeWithContentDescription("Pen. Tap again for nib and stabilizer").performClick(); rule.waitForIdle()
        rule.onNodeWithText("NIB").assertExists(); rule.onNodeWithText("0.6 mm").assertExists(); rule.onNodeWithText("50%").assertExists()
        rule.onNodeWithContentDescription("Eraser. Tap again for its size").performClick(); rule.waitForIdle()
        rule.onNodeWithContentDescription("Eraser. Tap again for its size").performClick(); rule.waitForIdle()
        rule.onNodeWithText("4 mm").assertExists()
    }

    // On a phone held upright the page fills the height. Two fingers zoom without leaving ink; one finger writes, or
    // moves the page in Move mode.
    @Test @Config(qualifiers = "w411dp-h891dp-port-hdpi") fun fingersZoomMoveAndWrite() {
        val st = seed()
        st.change { pen.move = false }
        rule.setContent { NbTheme { App(st) } }
        tap("Diary"); openToday()
        val page = rule.onNodeWithContentDescription("Diary page for", substring = true)
        val today = LocalDate.now()
        val tall = page.fetchSemanticsNode().size.height
        page.performTouchInput { pinch(center - Offset(200f, 0f), center - Offset(40f, 0f), center + Offset(200f, 0f), center + Offset(40f, 0f)) }  // pinched out, so the whole page stays in reach
        rule.waitForIdle()
        org.junit.Assert.assertFalse("a pinch left ink", today in st.s.diary)
        rule.onNodeWithContentDescription("Move: off, a finger writes. Tap to move the page with a finger").performClick(); rule.waitForIdle()
        page.performTouchInput { swipe(center, center + Offset(-150f, 80f)) }
        rule.waitForIdle()
        org.junit.Assert.assertFalse("a finger in Move mode wrote", today in st.s.diary)
        rule.onNodeWithContentDescription("Move: a finger moves the page. Tap to write with a finger").performClick(); rule.waitForIdle()
        page.performTouchInput { swipe(center, center + Offset(120f, 60f)) }
        rule.waitForIdle()
        org.junit.Assert.assertTrue("a finger did not write", today in st.s.diary)
        org.junit.Assert.assertTrue("the page does not fill the height", tall > 0.85f * rule.onRoot().fetchSemanticsNode().size.height)
        shot("phone-page-pinched-out")
    }
}
