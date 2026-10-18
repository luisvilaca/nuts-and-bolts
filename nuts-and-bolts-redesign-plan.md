# Nuts And Bolts — design and architecture pass

Paste this whole file into Claude Code at the root of the `nuts-and-bolts` repo.

## How to work through this

- Work **phase by phase, in order**. Phase 0 must land first: Impeccable reads `.impeccable/` and `DESIGN.md`, and right now those files describe an older "Night Grow Room" design. Any Impeccable command run before Phase 0 will push the app toward leaves and magenta.
- After each phase:
  1. Run `./gradlew testDebugUnitTest`. This runs `LogicTest` and `ScreensTest`. `ScreensTest` writes PNGs to `.impeccable/review/`.
  2. Open the PNGs for tablet landscape, tablet portrait, phone, and phone landscape. Check the phase's acceptance points against them.
  3. Commit with a message naming the phase.
- If Impeccable is installed, run its **audit** and **critique** commands on the screens touched at the end of each phase. Use whatever names your install lists, e.g. `/impeccable:audit`. Fix what they raise only if it doesn't contradict the rules below.
- Keep the code style of the repo: short comments in plain English that explain *why*, no new libraries unless a task says so.

## Rules that must not change (owner-pinned)

- App name "Nuts And Bolts"; robot default name Rivet (renamable).
- Diary papers and inks (Ice Blue #E0F7FA, Matrix Green #00FF88, Electric Cyan #00E5FF, Neon Fuchsia #FF007F, Matte Black #393939). Pages keep their paper.
- No squared or grid paper anywhere. Diary is ruled lines only.
- Lexend is the only typeface.
- Tabs at the bottom at every size; hidden under an open diary page.
- One-tap Done per habit. Skip, Fail, take back, Edit and Delete live in the card menu.
- Every log and purchase shows a snackbar with Undo.
- Robot, bench, reactor and bolt language stay. No plants.
- Animations freeze when system animations are off.

---

## Phase 0 — Make the design sources tell the truth

**0.1 Rewrite `.impeccable/design.json` and `.impeccable/surfaces/app.md`.**
Both still describe the retired "Night Grow Room": leaf slots, magenta grow light #E05AD0, sprout mark, leaf-unfurl motion, 48dp targets, and a navigation rail. Regenerate them from the current app:
- Reactor Workshop north star.
- Uranium glow #A6FF4D as the only light colour.
- Bolt slots.
- Bottom bar only.
- The real tokens in `Theme.kt`.
- Rename the color keys: `leaf-green` → `ready-green`, `wilt-coral` → `shear-coral`, `grow-magenta` removed, `uranium-glow` added.
- Replace the "Leaf Slots" and "Navigation Rail" components with "Bolt Slots" and "Bottom Bar".

**0.2 Make `DESIGN.md` consistent with itself and with `Theme.kt`.**
It was patched with dated addenda that contradict the body. Resolve each of these, keeping the newer decision:
- "The No Card Rule" vs "Cards, split views and fields (2026-10-18)". Cards won; delete the No Card Rule and hairline-row language.
- "Cherenkov blue" throughout vs "Uranium glow (2026-10-18)". Uranium won; rewrite every Cherenkov reference except the Cherenkov *finish* name in the parts catalogue.
- Layout says "the Material navigation rail sits at the left" vs Navigation says "never a side rail". Bottom bar won.
- Token values. Code wins: ground `#0D1615` (doc says `#0B1A1A`), dim `#9EB3AC` (doc says `#8FB0A8`), card `#152220`, focus `#4AE3A1`. Put `card`, `border`, `focus` and `glow` in the front-matter `colors`.
- "Today" → "Lab" everywhere.
- Remove the week strip, tally line, "THIS WEEK" label and next-level line. The owner removed them on 2026-10-18.
- Control heights: one number. The code uses 44dp for Done and menu, 52dp for lead. Write that.
- Fold the dated sections into the body. Don't keep an append-only log; history lives in git.

**0.3 Fix the window background.**
`res/values/themes.xml` sets `ground` to `#0C1320`, a blue left over from an older palette. The app ground is `#0D1615`, so launch flashes the wrong tint. Set it to `#0D1615`.

**Acceptance:** `grep -ri "leaf\|sprout\|magenta\|grow light\|cherenkov" .impeccable DESIGN.md` only hits the Cherenkov finish name and the history note that plants were removed.

---

## Phase 1 — One vocabulary

The UI uses five words for one idea. Screens currently show:
- "0 / 3 slots" (habit card)
- "3 of 3 bolts fastened" (Habits header)
- "3 slots planned" (future day)
- "Take back the last mark" (menu)
- "Logged" (button)
- "frame", "bench" (empty state, delete dialog)

**1.1 Pick the bolt as the user-facing word for a slot.** "Slot" and "mark" stay as code names only. Apply it everywhere:
- Card counter: `2 / 3` with the bolt icon in front, not "2 / 3 slots". The tracker under it already shows the slots.
- Habits header: "3 of 9 bolts in" today; "9 bolts planned" for a future day; ", 1 sheared" for fails.
- Menu: "Take back the last bolt".
- Calendar read-outs (`say =` in `Habits.kt`): same wording.
- History: "14 of 21 bolts in".
- Snackbar on Done: "+5 bolts · +1 Battery". Keep the level-up lead.

**1.2 Card label when the period is full.** "Logged" on a greyed button reads as disabled or broken. See 3.2 for the visual. The text is "All in" for build habits and "Held" for break habits.

**1.3 Break-it habits.** The trio is Resisted / Skip / Gave in. Make meta read "Break it · 1 a day" first, so the kind is visible before the attribute.

**Acceptance:** `grep -n "slot" app/src/main/java/**/*.kt` finds no user-visible strings, only identifiers and comments.

---

## Phase 2 — Colour discipline

There are four near greens on screen at once:
- ready `#6EE787` (pressable)
- glow `#A6FF4D` (light)
- focus `#4AE3A1`
- the diary's Matrix Green

The design rule "green means pressable or here" is also broken in several places.

**2.1 Stop using `W.act` for static text.**
- `Shop.kt` `Detail` and `Tile` paint "Equipped" and "Living here" in `W.act`. Use `W.steel` with the `check` icon. Selection or ownership that is not a place is steel.

**2.2 Merge focus into ready.** Delete `W.focus`. Field focus, cursor and outline use `W.act`. That leaves one interface green and one light green.

**2.3 Done slots in the tracker are steel, not glow.**
- `Kit.kt` `Tracker` draws done segments as a glow→steel gradient with a halo, so a done slot reads as "light".
- Draw done segments solid `W.steel`.
- Reserve the glow for the moment a period completes: when every slot is done, the whole tracker gets the soft glow halo. This mirrors the bench, where a frame only goes critical when complete.

**2.4 Collapse the two raised surfaces.** `W.card` `#152220` and `W.raised` `#15302B` are almost the same step and sit on top of each other: the disabled Done button, chips and menus are all `raised` on a `card`.
- Make the ladder explicit in `Theme.kt`: `rail < ground < card < raised`, each visibly one step apart.
- Lift `raised` to roughly `#1C3A34` so a control on a card separates.
- Document the ladder in `DESIGN.md`.

**2.5 Disabled text must stay legible.**
- `App.kt` `HabitRow` menu tints disabled items `W.edge` (`#24443E` on `#15302B`). That is about 1.3:1 and unreadable. Use `W.dim` at 60% alpha.
- `Shop.kt` `Price(short = true)` fades the price to 45% alpha. Keep the price at full `W.dim` and add "Need 60" in `W.dim` beside it, so the reason isn't carried by opacity alone.

**2.6 Selection borders are too loud.** The selected habit card, the active segment and the selected shop tile all draw a 1.5–2dp `W.steel` (`#CFE3DE`) border. On device that reads as pure white and is the brightest line on screen.
- Use `W.steel` at 55% alpha for selection borders.
- Add a faint fill instead: `W.steel` at 6%.
- Keep 1.5dp.

**Acceptance:** `W.act` appears only on buttons, the FAB, switches, text buttons, the selected tab icon, and today's ring. Check with `grep -n "W.act"`. Contrast of every text colour on `card` is ≥ 4.5:1; add a unit test that asserts this for the token pairs.

---

## Phase 2B — Icons that say what they mean

Seen on device, several icons in `Icons.kt` read as something else.

**2B.1 The bolts currency icon reads as "add".**
- `bolts` is a circle with a plus: ⊕. In the header wallet, "⊕ 45" looks like an add button. In Shop prices, "⊕ 120" reads as "+120".
- Redraw `bolts` as a bolt seen from the side, in the robot grammar: a hex head, then a shank with three thread ticks. `DESIGN.md` already describes this side-on threaded bolt for the diary page.
- Keep the face-on cross head for **slot marks** (`Atom.kt` `bolt()`); that's a bolt driven home, a different thing from the currency.

**2B.2 Settings looks like a brightness sun.** The `settings` path is eight rays round a circle. Redraw it as a gear: a toothed ring, the centre hole, and a pivot joint.

**2B.3 The Habits tab icon is the nut icon with a tick.** Both are hexagons, and gold nuts sit right next to the tabs in the header.
- Redraw `habits` as a frame on the bench: an upright rounded rectangle with three bolt holes, the top one driven. That ties the tab to the bench.

**2B.4 The Shop tab icon is unreadable at 20dp.** Redraw `shop` as a parts crate: a box with a lid line and a hex bolt stencilled on the front.

**2B.5 Bottom bar icon size.** The tab icons are drawn at 20dp; use 24dp, the size the icon grammar is drawn for.

**Acceptance:** in the ScreensTest PNGs, nothing in the wallet looks pressable. A person seeing the bar for the first time can name each tab from its icon.

---

## Phase 3 — The habit card (the most-used surface)

**3.1 Give the card a clear left-to-right reading.** Name and meta → counter and tracker → Done → menu is right. Change these:
- Name 18sp semibold.
- Meta in `W.dim` 14sp, one line with ellipsis on wide cards.
- The `next` chevron after the name currently implies "opens details". On the Lab it jumps to another tab (see 4.2). Remove it on Lab cards. Keep the whole text area tappable.

**3.2 A completed state that feels finished, not disabled.**
- When the period is full, swap the Done pill for a non-button "All in" chip: steel check icon, steel text, transparent background, 1dp steel border.
- The card's left edge gets a 3dp `W.glow` accent bar.
- Not-yet (future day) stays a disabled pill reading "Not yet".

**3.3 Make the counter count.** `2 / 3` in Numeral 18sp `W.text`, bolt icon 16dp steel. The tracker stays 8dp but gains 6dp height on tablet.

**3.4 Fail is too deep.** The owner moved Skip and Fail into the menu on purpose; keep that. Add a long-press on Done that opens the same menu anchored to the button, so Fail is two quick moves, not a hunt.

**3.5 Delete has one implementation.** The delete dialog is duplicated in `App.kt` (`HabitRow`) and `Habits.kt` (`Form`).
- Extract `ConfirmDeleteHabit(h, onDone)`.
- After delete, show a snackbar with Undo that restores the habit at its old index. Marks are untouched by delete, so undo is exact.

**Acceptance:** ScreensTest gets a new seeded state with one habit full, one partly done, and one failed. All three read differently at a glance in the PNG.

---

## Phase 4 — Lab

**4.1 Attribute cards truncate.** On tablet landscape the four-across row shows "Proces…". At four across, show the icon, the name in 13sp label style (Battery, Processor, Chassis, Core all fit at 13sp in ~150dp), `LV n`, and the gauge. Move the gloss (energy, mind, body, heart) into the screen-reader description and a long-press tooltip. Never ellipsize the name.

**4.2 Keep the owner where they are.**
- Tapping a habit's name on the Lab sets `ui.tab = "habits"` and opens the form. Cancel then lands on Habits, not the Lab.
- Add `ui.editFrom` (the tab that opened the form). Cancel and Save return there.
- Render the form over the current tab instead of switching tabs.

**4.3 Close the dead space on the right pane.**
- On tablet landscape with one or two habits, about 60% of the right pane is empty: one card at the top, the attributes pinned to the very bottom.
- Let the attributes follow the habit list directly (12dp gap). Pin them to the bottom only when the list is long enough to scroll.
- Make the attribute cards a little taller (16dp padding, 6dp gauge), so the row reads as a proper second block.

**4.4 Empty state.** The current copy is one long paragraph. Shorten it:
- "The bench is empty"
- One line: "Each habit becomes a frame on Rivet's bench. Every time you do it, a bolt goes in."
- The button.
- The suggestion chips. Prefix each with the attribute icon it trains so the chips aren't all identical.

**Acceptance:** tablet-landscape PNG shows all four attribute names in full; Cancel from an edit opened on the Lab returns to the Lab (add a ScreensTest step).

---

## Phase 5 — Shop

**5.1 Sticky category tabs.** On wide layouts the tab row is inside the scrolling column, so it scrolls away. Pin `SecondaryScrollableTabRow` above the scroll and scroll only the grid.

**5.2 Try on a whole outfit.**
- `pick` resets when changing category, so the bay only ever previews one slot.
- Replace it with `tryOn: MutableMap<slot, partId>` held for the visit.
- Show a "Trying on: Dome, Wrenches · Reset" line under the detail.
- Leaving the Shop clears it.

**5.3 Purchase button states.**
- "Need 60 more bolts" inside a disabled pill is the only place the shortfall shows. Keep the pill disabled with "Buy", and put the shortfall under the price ("60 more bolts") in `W.dim`.
- When affordable, the pill reads "Buy · 120" with the coin icon.

**5.4 Tile semantics and state.**
- `Tile` uses `Role.Tab`; use `Role.RadioButton` inside a `selectableGroup()`.
- Add an "Owned" corner tick (steel) on owned, unequipped tiles so the grid shows what you have.

**5.5 Base tiles are expensive.** Each base tile draws the whole room live. Render each base thumbnail once into an `ImageBitmap` keyed on `(baseId, equip, habitsHash)` and reuse it.

**5.6 Sort.** Within a category: equipped first, then owned, then by price ascending. Affordable items read first.

---

## Phase 6 — Habits planner and form

**6.1 Calendar day marks are too small to read.** At 26dp the three-hole plate is decoration.
- When cells are roomy (≥ 60dp tall), draw the plate at 42dp and add a one-line "2/3" under it in Numeral 12sp.
- Add a one-line legend under the calendar card: driven = done, ring = open, split = failed. Use the real `SlotMark` drawings.

**6.1b Draw nothing where nothing ran.**
- On device, every day of October before the first habit shows a faint blank plate. That's 17 identical empty rectangles of noise.
- A day with no habit running (before the first start, or all off-days) gets no mark at all.
- Future days with habits keep the dashed plate.

**6.2 Keep "Add habit" reachable in Calendar view.** The FAB is hidden there. Show it, and pre-fill the start date with the tapped day if one is selected.

**6.3 The form gets its own top bar.**
- Replace `Header` (title + wallet + settings) in `Form` with a form bar: back icon, "New habit" / "Edit habit", and a "Save" text button on the right.
- On phones, also pin Cancel/Save in a bottom action bar above the keyboard (`imePadding()`), so saving doesn't need a scroll to the end.

**6.4 Target range.** `target` is capped at 7 in both `Form` and `dataFrom` (`coerceIn(1, 7)`).
- Allow up to 12 per day and 7 per week. The bench already shrinks bolts on a crowded frame (`br`).
- Check the frame height formula `bench - 30 - 44 * slots` still fits at 12 and clamp the hole spacing if not.

**6.5 Form state survives process death.** Use `rememberSaveable` for every field in `Form`. `LocalDate` and `Set<Int>` need a small `Saver`.

**6.6 History view earns its space.**
- Each history card adds a 12-week strip of mini day plates (or week plates for weekly habits) under the run bar.
- Name, % and run bars stay.

**6.7 The header summary must match the view.**
- Bug seen on device: in History the header reads "0 of 0 bolts fastened". `summary(s, d, p)` uses the day in `ui.date`, which History doesn't show, and there's no stepper to tell which day.
- Day view: keep the day summary.
- Calendar: the month ("41 of 60 bolts in October").
- History: totals across all habits ("4 habits · best run 9 days").

**6.8 History run bar.**
- `step = min(14dp, width / best)` draws a 1-day run as a 14dp blip on a full-width track. It reads as an empty bar.
- Scale the bar to the habit's lifetime in periods: track = days since start, steel = best run, green = current run.
- Label both ends.

**6.9 Weekday chips show selection.** In the form, all seven selected chips look like plain outlined buttons. Selected chips get the 6% steel fill (2.6) and a leading `check` icon, so "every day" is visibly chosen.

**6.10 The form's right column.** On tablet landscape the actions end halfway down and the rest is empty. Make the right column sticky (`Modifier.align(Top)` in a non-scrolling Row, with the left column scrolling), so Save stays in view while the schedule is edited.

---

## Phase 7 — Diary calendar

**7.1 Show the pages.**
- Days with a page currently show only a pen icon.
- Render a thumbnail of the page (paper colour + strokes, no template) into each roomy cell.
- Generate it off the main thread from the page file and cache it in `filesDir/thumbs/<date>.png`. Invalidate on `writePage`.
- Fall back to the pen icon in compact cells.
- This is the biggest delight win available on the tablet.

**7.2 "Write today" deserves weight.** Make it a filled pill (ready green) when today has no page, and a text button "Open today" when it does.

**7.3 Show which inks work on this paper.**
- On cream paper, Ice Blue (#E0F7FA) is nearly invisible. On Matte Black paper, Matte Black ink is invisible.
- Keep all five inks (owner-pinned). When an ink's contrast against the current paper is under 1.5:1, draw its swatch with a thin diagonal slash and 40% opacity. Leave it selectable.

**7.4 Ask the owner about the page emblem.**
- The atom at the top right of the diary page (three ellipses round a dot) reads at a glance as the React logo.
- Don't change it unilaterally; `DESIGN.md` records the atom as a choice. Ask the owner whether to swap it for the side-on threaded bolt (2B.1) or tilt and thin the orbits so it no longer matches that logo.

---

## Phase 8 — Architecture

**8.1 Replace stringly-typed state with types.**

| Today | Change to |
|---|---|
| tabs `"today"`, `"shop"`, … | `enum class Tab { Lab, Shop, Habits, Diary }` (rename "today" → Lab in code too) |
| `Habit.kind` `"good"` / `"bad"` | `enum class Kind { Build, Break }` |
| `Habit.per` `"day"` / `"week"` | `enum class Per { Day, Week }` |
| `Mark.s` `"done"` / `"skip"` / `"fail"`, `Slot.s` `""` | `enum class Outcome { Done, Skip, Fail }` and `Outcome?` for open |
| `stat`, `weight` | enums keyed by the same ids |

Keep the JSON strings identical so existing state files and backups load unchanged; map at the JSON boundary in `Store.kt`. Extend `LogicTest`.

**8.2 Add a schema version to `state.json`.** Write `"v": 2` and route loading through `migrate(o: JSONObject): JSONObject`. Move the existing ad-hoc carry-overs there: missing `peak`, old pen sizes. Keep `oldPenSettingsCarryOver` passing.

**8.3 Stop recomposing the bay every frame.**
- `BayPanel` and `RobotView` call `clock()` in composition, so the composable recomposes at the display refresh rate.
- Read the frame time inside the draw phase instead: hold it in a `State<Long>` and read `.value` inside the `Canvas` lambda.
- Only the draw phase runs per frame; the nameplate and layout don't.
- When the Activity is stopped, the loop already stops (withFrameMillis). Also stop it when the bay is off-screen.

**8.4 Narrow invalidation.**
- `Store.s` reads `tick` on every access and `App` reads `store.tick` at the top, so every change recomposes the whole tree, including the calendar, which recomputes `slotProgress` for 42 days × all habits.
- Remove the top-level `store.tick` read.
- Memoize derived values with `remember(tick, …)` or `derivedStateOf`: per-day progress for the visible month, `record()` per habit, `orbitsFor(today)`.

**8.5 Disk off the main thread.**
- `Store.change` calls `save()` synchronously.
- Serialize on the caller (cheap), write on a single-thread dispatcher (`Dispatchers.IO.limitedParallelism(1)`) with a coalescing latest-wins queue.
- Flush in `onStop`.
- Backup export reads every page file on the main thread; move it to `withContext(Dispatchers.IO)`.

**8.6 Keep rules in `Logic.kt`.**
- The Shop's purchase undo mutates state inline in `Shop.kt` (`owned -= …; bolts += …`). Add `unbuy(s, p, before)` to `Logic.kt` next to `buy`, with a test.
- Do the same for "add suggested habit" and its undo in `Lab.kt`.

**8.7 Navigation state survives process death.** `Ui` holds `tab`, `edit`, `page`, `date` and `settings` in plain `mutableStateOf` inside `remember`. Make `Ui` saveable: a `Saver` or `rememberSaveable` per field.

**8.8 Housekeeping.**
- Replace inline fully-qualified calls with imports (`androidx.compose.material3.HorizontalDivider`, `androidx.compose.material3.AlertDialog`, etc.).
- Remove the unused `dayProgress` in `Logic.kt` if nothing calls it, along with its test.
- `Sfx.play` reads `Store.get(c).s`; pass the sound flag in instead so Sfx doesn't depend on Store.

**Acceptance:** `LogicTest` and `ScreensTest` pass; an old `state.json` without `v` loads; Layout Inspector shows `BayPanel` recomposition count flat while the robot animates.

---

## Phase 9 — Accessibility and polish sweep

- **9.1** Every icon-only control has a content description: Settings, prev/next, menu, diary tools. Check the diary rail tools in particular.
- **9.2** Respect font scale. Run ScreensTest at `fontScale = 1.3` for tablet landscape and phone. Fix any clipped header, attribute card or card counter.
- **9.3** The bottom bar's selected indicator is 52×28dp. Make it 64×32 for Material proportion and a larger hit target; the whole column is already tappable.
- **9.4** Header: the wallet's semantics read "N bolts, N nuts" — good. Also add live-region politeness on the wallet so a screen reader announces the change after Done.
- **9.5** Snackbars: one at a time is right. On device, the snackbar spans from the middle of the bay across the right pane and covers three of the four attribute cards, right when a Done has just changed them.
  - On wide layouts, cap the width at 480dp.
  - Anchor it bottom-end over the right pane.
  - Raise it above the attribute row (or let 4.3 move the attributes up so it no longer overlaps them).
- **9.6** Motion: keep the 700ms bolt drive. Add a 150ms scale-press on the Done pill (0.96) and a 250ms cross-fade when a card switches to "All in". All of it goes through the shared clock, so it stays frozen when animations are off.

---

## Done when

- Phases 0, 1, 2, 2B and 3–9 committed separately.
- `.impeccable/review/*.png` regenerated for all four window classes and attached to the final commit message summary.
- `DESIGN.md` describes exactly what the PNGs show.
