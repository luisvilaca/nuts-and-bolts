---
name: Nuts And Bolts
description: A habit tracker and diary where a small robot bolts together a frame for every habit at a reactor-powered workbench.
colors:
  ground: "#0D1615"
  rail: "#081313"
  card: "#152220"
  raised: "#1C3A34"
  border: "rgba(255,255,255,0.08)"
  hairline: "#1B3632"
  edge: "#24443E"
  bay-sky: "#13302B"
  bay-floor: "#0E2420"
  frost: "#E6F2EE"
  dim: "#9EB3AC"
  cool-steel: "#CFE3DE"
  joint: "#6C8F87"
  ready-green: "#6EE787"
  on-ready: "#06281A"
  selection: "rgba(207,227,222,0.55)"
  selection-fill: "rgba(207,227,222,0.06)"
  uranium-glow: "#A6FF4D"
  nut-gold: "#F2C14E"
  shear-coral: "#FF7457"
  paper-matte-black: "#393939"
  paper-cream: "#F4ECD8"
  paper-night: "#0B1020"
  ink-ice-blue: "#E0F7FA"
  ink-matrix-green: "#00FF88"
  ink-electric-cyan: "#00E5FF"
  ink-neon-fuchsia: "#FF007F"
  ink-matte-black: "#393939"
typography:
  display:
    fontFamily: "Lexend, sans-serif"
    fontSize: "28sp"
    fontWeight: 700
    lineHeight: "34sp"
    letterSpacing: "-0.01em"
  headline:
    fontFamily: "Lexend, sans-serif"
    fontSize: "22sp"
    fontWeight: 600
    lineHeight: "28sp"
  title-large:
    fontFamily: "Lexend, sans-serif"
    fontSize: "21sp"
    fontWeight: 600
    lineHeight: "28sp"
  title:
    fontFamily: "Lexend, sans-serif"
    fontSize: "18sp"
    fontWeight: 600
    lineHeight: "24sp"
  body-large:
    fontFamily: "Lexend, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
  body:
    fontFamily: "Lexend, sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
  label-large:
    fontFamily: "Lexend, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "20sp"
  label-medium:
    fontFamily: "Lexend, sans-serif"
    fontSize: "13sp"
    fontWeight: 500
    lineHeight: "16sp"
  label:
    fontFamily: "Lexend, sans-serif"
    fontSize: "13sp"
    fontWeight: 500
    letterSpacing: "0.14em"
  numeral:
    fontFamily: "Lexend, sans-serif"
    fontSize: "18sp"
    fontWeight: 600
    fontFeature: "tnum"
rounded:
  swatch: "6dp"
  cell: "10dp"
  card: "12dp"
  menu: "16dp"
  bay: "20dp"
  pill: "9999dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  card: "16dp"
  lg: "20dp"
  xl: "28dp"
components:
  button-done:
    backgroundColor: "{colors.ready-green}"
    textColor: "{colors.on-ready}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
    height: "44dp"
  button-done-disabled:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.dim}"
    rounded: "{rounded.pill}"
    height: "44dp"
  button-done-lead:
    backgroundColor: "{colors.ready-green}"
    textColor: "{colors.on-ready}"
    rounded: "{rounded.pill}"
    height: "52dp"
  button-menu:
    textColor: "{colors.cool-steel}"
    rounded: "{rounded.pill}"
    size: "44dp"
  button-outlined:
    textColor: "{colors.frost}"
    rounded: "{rounded.pill}"
    height: "44dp"
  fab:
    backgroundColor: "{colors.ready-green}"
    textColor: "{colors.on-ready}"
  card:
    backgroundColor: "{colors.card}"
    rounded: "{rounded.card}"
    padding: "16dp"
  nav-container:
    backgroundColor: "{colors.rail}"
    textColor: "{colors.dim}"
  nav-item-selected:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.frost}"
  segment-active:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.frost}"
  field:
    backgroundColor: "rgba(255,255,255,0.05)"
    textColor: "{colors.frost}"
    rounded: "10dp"
    padding: "12dp 16dp"
  bay-panel:
    backgroundColor: "{colors.bay-sky}"
    rounded: "{rounded.bay}"
    padding: "20dp"
  snackbar:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.frost}"
  snackbar-action:
    textColor: "{colors.ready-green}"
  menu:
    backgroundColor: "{colors.raised}"
    rounded: "{rounded.menu}"
    padding: "16dp"
---

# Design System: Nuts And Bolts

## Overview

**Creative North Star: "Reactor Workshop"**

A robot's workbench after dark. The app is a deep greenhouse-teal lab in which the robot stands on its round platform beside a steel bench. Under the bench a glass fusion capsule powers it all; its uranium-green light and the light rail it feeds hang over the bench and brighten as the Core attribute rises. Every habit running today is a steel frame clamped upright on the bench and every slot is a bolt hole in it: Done drives a bolt home, Skip leaves a dashed ring, Fail shears the bolt, and an open slot is an empty tapped hole. A habit with every bolt in goes critical: its frame glows uranium green and positrons rise off it. Uranium green is light, not paint; the interface itself stays teal, cool steel and frost, with ready green as the one live colour.

Content sits on cards: every habit, form group, history item and calendar is a card one step above the ground. The bolt is the single ornament and always carries meaning: the slot, the day, the five hex holes of the diary page's gauge. The diary page's emblem is an atom, three orbits round a nucleus.

Density is compact and calm: 44dp controls, 28dp gutters on wide windows, one clear Done per habit with everything else in its menu. One rounded, open face (Lexend) carries everything.

**Key Characteristics:**
- Greenhouse-teal ground with a deeper rail; content on cards; tonal steps, no shadows.
- One live accent (ready green); gold for nuts, coral for failure, cool steel for selection, metal and driven bolts.
- Uranium glow as the only light: the reactor, its rail, the gauges, a critical frame.
- Lexend throughout: bold display, semibold titles, tracked small caps for labels.
- Bolts and frames as the one ornament and the one state language.
- Robot-grammar line icons: 24dp, 1.8 stroke, square caps, pivot-joint circles.
- Synthesized sound for the moments that change something.

## Colors

A dark lab lit by one small reactor, where ready green means pressable or here and every other colour holds a single job.

### Primary
- **Ready Green** (ready-green): the Done pill, primary buttons and the FAB, switches, confirm text actions, the snackbar's Undo, and the place markers (selected tab icon, the shop tab indicator, today's ring on the calendars, the active diary tool's bar, the diary calendar's page mark). In the starter robot it lights the eyes, the smile and the dashed platform ring. Text on it is **On-Ready** (on-ready), a near-black green.
- Ready green also outlines and carets the field being typed in; there is one interface green.

### Secondary
- **Cool Steel** (cool-steel): driven bolt heads and their counts, the frames' 70% rim and the bench's top edge, attribute icons, gauge fills (with the glow), and the shop's Equipped and Living here (with a check icon). Every selected state that is not a place uses **Selection** (selection): steel at 55% as a 1.5dp edge over a steel-6% **Selection Fill** (selection-fill): the active segment, selected chips, the picked habit card, the picked shop part, a lit pen-button pad. Never solid steel, which reads as white on the device.
- **Joint** (joint): the robot's joints and claws, a bolt head's rim, the light rail's wires, the frames' clamps, the reactor's coil packs, and the rim of a frame whose habit failed today.

### Light
- **Uranium Glow** (uranium-glow): every glow in the app and nothing else. The reactor's plasma core, orbits, electrons, level lamps and the pulses climbing its cable; the light rail over the bench and its cone; the radial wash over the bay; the attribute gauges (a glow-to-steel fill with a soft halo and a bright leading spark); a habit's tracker once its period is complete (a soft halo round the whole track); the current run in a history bar; the Charging Bay's battery cells; a critical frame and its positrons. Other base rooms keep their own lamp colour on the rail (amber, violet, teal station light); the reactor glows green in every room.

### Tertiary
- **Nut Gold** (nut-gold): nuts, their counts, gold parts of the robot's world. Never a general highlight.
- **Shear Coral** (shear-coral): sheared bolts and failed tracker segments; Fail and Gave in in the habit menu; destructive text actions; form errors.

### Neutral
- **Ground** (ground): every screen.
- **Rail** (rail): the bottom bar, the diary tool rail, the robot's face glass, an open bolt hole; a step below the ground.
- **Card** (card): every card, with a 1dp white-8% **Border** (border).
- **Raised** (raised): one visible step above a card: controls at rest, the selected tab indicator, the active segment, selected chips, disabled Done, shop part tiles, menus, the snackbar, the frames and the day plates.
- **Bay Sky** (bay-sky) to **Bay Floor** (bay-floor): the bay's vertical gradient and its floor band, with the room's light as a skirting line where they meet.
- **Hairline** (hairline): calendar week rules, dividers in menus.
- **Edge** (edge): outlines at rest (outlined buttons, chips, segments, the menu button), gauge and tracker tracks, future-day plates, the bench top and legs.
- **Frost** (frost): primary text. **Dim** (dim): secondary text, metadata, labels, unselected tabs, open and skipped bolt holes.

### Diary papers and inks
The diary page keeps the owner's own materials, separate from the app palette (owner-pinned). Papers (three replaceable slots, each page keeps the paper it was written on): Matte Black (paper-matte-black), Cream (paper-cream), Night (paper-night). Inks with the owner's roles: Ice Blue (ink-ice-blue) for text, Matrix Green (ink-matrix-green) for headers, Electric Cyan (ink-electric-cyan) for vectors and equations, Neon Fuchsia (ink-neon-fuchsia) for annotations, Matte Black (ink-matte-black) for cream paper. The printed template follows the paper: on dark paper the weekday, header rule and atom emblem are Matrix Green, the date and field labels Ice Blue at 72%, the rules white at 15% and the large foot emblem Electric Cyan at 20%; on light paper all of it is Matte Black at matching strengths.

### Named Rules
**The Ready Or Pressable Rule.** Ready green appears only where something can be pressed or marks where the owner is (plus the starter robot's eyes, smile and platform ring; painted parts carry their finish's own eye colour). Selection that is not a place is cool steel.

**The Light Is Light Rule.** Uranium green is light, not paint: it glows (the reactor, the rail, gauges, completed work) and never fills a control, a label or text.

**The One Meaning Rule.** Gold is nuts, coral is failed or destructive. Neither decorates, warns generally, or adds variety.

## Typography

**Display Font:** Lexend, variable, weights 600/700 (bundled, with sans-serif fallback)
**Body Font:** Lexend, variable, weights 400/500/600
**Label Font:** Lexend Medium, tracked uppercase

**Character:** One rounded, open, highly legible face throughout; weight and tracking do the work a second family would.

### Hierarchy
- **Display** (700, 28sp, 34sp, -0.01em): screen titles in the header.
- **Headline** (600, 22sp, 28sp): the empty-state line ("The bench is empty"), the picked habit's name in Habits' detail pane.
- **Title Large** (600, 21sp, 28sp): a shop part's name in its detail.
- **Title** (600, 18sp, 24sp): habit names on cards and in history, the month and day stepper.
- **Body Large** (400, 16sp, 24sp): header subtitle (the date), notes, part names in the grid, field text.
- **Body** (400, 14sp, 20sp): habit metadata joined by " · ", summaries, helper text, the reward lines.
- **Label Large** (600, 16sp, 20sp): button text, segment labels, shop tabs, calendar day numbers, attribute names on the Lab.
- **Label Medium** (500, 13sp, 16sp): attribute meanings, stat captions in the detail pane.
- **Label** (500, 13sp, 0.14em tracking, uppercase, dim): form group labels, the detail pane's LOG, calendar weekday heads, the robot's nameplate (at body-large size in cool steel).
- **Numeral** (600, 18sp, tabular figures): wallet balances, levels, the card counter (at 14sp), stats, the target stepper, the reminder time.

### Named Rules
**The One Face Rule.** Lexend sets everything, including the printed diary template. Contrast comes from weight (400 to 700), size and tracked caps, never from a second family.

**The Tabular Count Rule.** Every count and balance uses Numeral with tabular figures so numbers hold still as they change.

## Layout

A window is **wide** when it is landscape and at least 600dp wide; otherwise it **stacks**. A window whose short side is under 600dp is a **phone**. The bottom bar sits at the foot of every window at every size; there is no side rail.

- **Wide Lab and Shop:** header across the top with 28dp side gutters; the bay takes 46% at left, the side or catalogue 54% at right. On the Lab the side holds today's habit cards, which scroll on their own, and the attribute panel pinned at the bottom right (owner, 2026-10-18).
- **Stacked Lab:** on a tablet held upright the bay sits under the header, the habits scrolling below it and the attribute panel pinned at the foot; on a phone the page scrolls, attributes under the bay and habits after.
- **Habits:** no bay. Day / Calendar / History in a sliding segmented control. Wider than 720dp the Day view splits: habit cards left (56%), the picked habit's detail right (44%). The calendar fills the space below its stepper and never scrolls, with a one-line legend under it. Add habit floats over every view and starts a new habit on the day picked last.
- **Diary:** a calendar card that fills the screen. Each written day shows the pen icon in steel, nothing more (owner, 2026-10-18: a mark, not a picture of the page). Any day opens its page from its cell, today included; there is no separate Write today action (owner, 2026-10-18). An open page covers the window beside a 56dp tool rail on the right edge, never leaving bare ground: as wide as the space on a tablet (it moves up and down) and as tall as it on a phone held upright (it moves sideways; owner, 2026-10-21). Two fingers zoom it, up to 4x and out to the whole page centred on the ground, and move it; the bottom bar hides.
- **Form:** opens over whichever tab asked for it (the Lab, Habits) and returns there on Cancel or Save. Its own bar replaces the header: a back arrow, "New habit" or "Edit habit" in Display, and a Save text action at the right. Cards, one group each. Wider than 720dp, two columns that scroll on their own: Name, Type, Attribute, Schedule and Reminder (a switch and its time, like the daily reminder in Settings) left; Weight, Reward and the actions right, which stay in view while the schedule is edited. On a phone Cancel and Save are pinned in a bar above the keyboard. Every field survives the system ending the app in the background. A habit can ask for up to 12 a day or 7 a week; the selected weekday chips carry a check.
- **Settings:** one column capped at 720dp: Robot, Sync (Sign in with Google, then the account, its state and Sign out), Backup, Daily reminder, Sounds, Diary, Pen buttons.
- **Rhythm:** 10dp to 12dp between cards, 16dp inside them; group labels 10dp above their content.
- **Shop grid:** as many columns as fit at 156dp each (at least two), 12dp gaps.

### Named Rules
**The Content On Cards Rule.** Habits, form groups, history items and calendars sit on cards (12dp corners, 16dp inside, the card surface and a white-8% border). The bay is framed, not carded. Nothing nests a card in a card.

## Elevation & Depth

Flat. Depth is tonal: rail < ground < card < raised, each one visible step (a unit test keeps the ladder in order and every text colour at 4.5:1 or better on each surface). The one atmospheric effect is light: inside the bay a vertical sky gradient, the floor band graded into the rail, and the reactor's uranium radial wash; outside it, the gauges' and trackers' glow. The robot's shell carries the only gradient on a figure. Material's FAB and snackbar keep their default elevation; nothing else casts a shadow.

## Shapes

Soft and machined. Every pressable action is a pill or a circle; the bay is framed with 20dp corners; cards and part tiles have 12dp corners, fields 10dp, calendar cells 10dp, menus 16dp, paper swatches 6dp. Progress is a pill track: the attribute gauge, the habit tracker (one segment per slot, 4dp apart), a habit's run bar. The bolt family is drawn from circles: a solid head with a cross, a dashed ring, a split head and a ringed hole.

## Components

### Buttons
- **Shape:** full pill, 44dp tall (52dp for a lead Done).
- **Done / primary:** ready green with on-ready text in Label Large. A finished period swaps it for the finished chip (see Habit card); a future day disables it to raised with dim "Not yet". Break habits read Resisted / Skip / Gave in.
- **Menu button:** a 44dp outlined circle with the three-dot icon in cool steel, beside Done. Its menu (raised): Skip, Fail (coral), Take back the last bolt, then Edit habit and Delete habit (coral); items that cannot act read dim at 60%. Delete asks first.
- **Outlined:** 1dp edge border, frost text (Cancel, date pickers, backup).
- **Text actions:** ready green for confirm, coral for destructive, frost for dismiss.
- **FAB:** extended, ready green, bottom-end with a 20dp inset (Add habit).

### Habit card
Read left to right: the name in Title (18sp semibold) with a dim chevron after it where the name opens the form (not on the Lab), the metadata in dim Body (one line with an ellipsis on a wide card), the counter (the side-on bolt at 16dp in steel and "2 / 3" in Numeral 18sp frost) over the tracker, then Done and the menu button. Narrower than 480dp the counter and buttons take their own lines. A long press on Done opens the same menu anchored to the button, so Fail is two quick moves. On the split Habits view a card is picked by tapping it and takes the selection edge and fill.

When every bolt of the period is in, Done gives way to a finished chip, not a button: a steel check and "All in" ("Held" for a break habit) in steel inside a 1dp steel outline on a transparent pill, and the card takes a 3dp uranium-glow bar down its left edge. A period filled with a skip or a sheared bolt shows the same chip in dim, reading "Closed". A future day keeps a disabled raised pill reading "Not yet".

Deleting a habit, from its menu or the form, asks first, then shows a snackbar whose Undo puts the habit back at its old place with its bolts untouched.

### Tracker
A habit's period as an 8dp pill split into one segment per slot: done in solid cool steel, failed coral, skipped a dashed dim outline, open an edge track. A slot logged on another day of the week is at half strength. When every slot is done the whole tracker takes a soft uranium halo, as its frame goes critical on the bench.

### Large text
Every screen is rendered at 1.3x font scale on the tablet and the phone (ScreensTest's large-* renders) and nothing clips: segment labels may take three lines, the form's schedule line wraps Day / Week below the count, and attribute names keep their own line.

### Segments
A pill outlined in edge whose selection, a selection-fill pill with a 1.5dp selection edge, slides between options on a spring (damping 0.8) while the label colours cross-fade. Labels run to two lines. Four options in a column narrower than 420dp fall into a two-by-two grid of filter chips.

### Words
One word for a slot on screen: the bolt. "Slot" and "mark" are code names only. A count reads "4 of 9 bolts in", with ", 1 sheared" for fails, or "9 bolts planned" for a day ahead; Done says "+5 bolts · +1 Battery", Fail "Sheared · −5 bolts · −1 Battery", the menu "Take back the last bolt". A break habit's metadata leads with "Break it".

### Fields
A white-5% fill, 10dp corners, 12 by 16dp inside, a white-8% border at rest and a ready-green outline and cursor when focused. Errors are coral body text under the form; a missing name shows first as a dim hint.

### Chips
Material filter chips, 1dp edge border, frost label; selected takes the selection fill and a 1.5dp selection edge. Starter-habit suggestion chips are 40dp tall.

### Navigation
- **Bar:** Lab, Shop, Habits, Diary on rail at the bottom of every window; Settings is an icon in the header that turns ready green while open.
- **States:** the selected tab gets a raised 64x32dp indicator, a ready-green icon and a frost label; unselected are dim.
- **Header:** at least 60dp; the display title alone, no subtitle or summary beside or under it (owner, 2026-10-18), the wallet at right (the side-on bolt and its count in cool steel, the nut in gold, 22dp icons, Numeral), then Settings.
- **Snackbar:** raised with frost text and a ready-green Undo, for every log and purchase; an Undo plays its own sound. On a wide window it is at most 480dp wide at the bottom end over the side pane, and on the Lab it rises above the attribute row so it never covers the levels a Done has just changed.
- **Wallet:** a polite live region, so a screen reader announces the new balance after a Done.

### Attribute panel (Lab)
One card pinned at the bottom right of the Lab, holding all four attributes (on a phone held sideways it follows the habit list and scrolls with it, since pinned it would leave no room for a habit). Where the side is at least 560dp wide (scaled with the text size), a single row of four dials divided by hairlines: each dial is a 48dp pressure gauge in the reactor's manner, a rail face with ten level ticks (steel up to the level reached, edge beyond), a 270° edge track whose way to the next level is filled in uranium glow with a soft halo, the level in Numeral at its centre and the attribute's icon in the arc's open foot; beside it the name in Label Large (14sp, frost) over what it means for you (Energy, Mind, Body, Heart) in dim Label Medium. Narrower, or at large text, one compact line per attribute: icon, name, meaning, a 5dp gauge and "LV n", the name and meaning columns growing with the text. A long press repeats the name and meaning in a tooltip; the screen reader hears name, meaning, level and the share to the next.

### Empty state (Lab)
"The bench is empty" in Headline, one dim line ("Each habit becomes a frame on Rivet's bench. Every time you do it, a bolt goes in."), the Add your first habit button, and the starter chips, each led by the icon of the attribute it trains; a chip adds its habit with an Undo.

### History
A card per habit: its icon, name and bolts-in rate; a line of bolts, best run and current run; the run bar; and its last twelve weeks. The run bar spans the habit's whole life, one step per day or week since it began, labelled "31 days ago" and "today": the best run in steel at 55% and the current run in uranium glow, both reaching to today. The twelve weeks are a block of day pips (14dp cells, seven to a week, oldest at the left: steel when full, half steel when partly done, coral when sheared, an empty ring when nothing went in, blank when the habit did not run) or, for a weekly habit, twelve week plates.

### Habit detail (split Habits)
The picked habit: its name in Headline with Edit, its metadata, four stats in Numeral over dim captions (bolts-in rate, running now, best run, sheared in coral when any), the run bar, and a LOG card listing every day it was logged, newest first, with that day's bolt marks and a count; a day opens that date.

### Reward card (form)
What the habit as set up pays: the bolts and points of each Done (or each resist), what a fail takes back, the most a period can bring, how its nuts are earned, and how many Dones take its attribute to the next stage.

### Shop
The bay wears whatever is being tried on. Tapping parts in different categories builds one outfit for the visit; a dim line under the picked part reads "Trying on: Dome, Wrenches" with a Reset text action, and leaving the Shop clears it (trying on buys nothing). On a wide window the category tabs stay pinned above the grid and only the grid scrolls. Within a category parts sort worn first, then owned, then cheapest. Tiles are radio choices in a group; a part owned but not worn carries a small steel check badge in its corner. Buy reads "Buy · (bolt) 120" when the balance covers it and plain disabled "Buy" when it does not, the shortfall then shown under the price. A base tile renders its room once and reuses the picture until the base, the robot or today's bolts change.

### Shop prices
A price shows the bolt and nut icons with their counts in steel and gold. When the balance falls short the counts turn dim and a dim line beneath says what is missing ("45 more bolts · 2 more nuts"), so the reason is in words, not opacity. Disabled menu items read dim at 60%, never edge.

### Bolt slots (signature)
Each slot state has its own form, seen face on; colour only confirms it.
- **Done:** a bolt head driven home, solid cool steel with a joint rim and the bolts currency's cross recessed.
- **Skipped:** a dashed dim ring with a dash across it.
- **Failed:** a sheared head, solid coral, split diagonally into two halves pushed apart.
- **Open:** an empty tapped hole: a rail disc with a dim ring and a fainter inner thread ring.
- A slot logged on another day of the week shows at 45%.

### The reactor bench (signature)
- **Bay:** the robot on its round platform (dashed green ring) stands left; at right two joint wires hang the light rail over a steel bench on two legs. One frame per habit running today, up to six, spaced evenly: a raised plate clamped upright whose height grows with its target, one bolt hole per slot from the bottom up; its rim is joint if the habit failed today. With no habits the bench holds one dashed frame.
- **Reactor:** a glass fusion capsule standing on the floor between the bench legs. A plasma core burns inside with one orbit per three Core levels (one to four), electrons racing round them; copper-wound coil packs clamp it either side, a pressure gauge reads Core, ten lamps on a hazard-striped plinth light one per Core level, and pulses of light climb a cable up the right bench leg into the bench.
- **Drive:** Done screws the newest bolt on that habit's frame in over 700ms (cubic ease-out): the head grows to size while its cross turns three quarters of a turn, and the robot cheers.
- **Critical:** when every slot of a habit's period is done its frame glows uranium green: a darker green fill, a solid rim, two soft outer strokes and a radial wash that breathe slowly. Positrons rise off its top: four-point white sparks with a green core and halo that swell and fade over a three-second flight, drifting aside, each trailing a thin line of light. The glow ignites just after the last bolt seats.
- **Day plate:** a day as a small plate with three bolt holes, one, two or three driven home (any done, half, all); the last hole shows a sheared bolt when anything failed; a dashed blank plate while the day is ahead. In the habits calendar a day when no habit ran shows nothing at all; a plate is 42dp in cells 110dp tall or more and 26dp otherwise, with "2/3" in Numeral 12sp under it in cells of 60dp or more. A legend under the calendar shows the real bolts: driven = done, ring = open, split = sheared.
- **Crowded frames:** a frame with many bolts closes up its holes (and the bolts shrink) so even a dozen stand below the light rail.

### Icons
One robot grammar: 24dp line art at 1.8 stroke, square caps, mitred joins, angular strokes and a small pivot-joint circle wherever a part would hinge. Tinted by context (cool steel, dim, ready green, gold, coral).
- **Bolts** (the currency): a bolt seen side on, a hex head over a threaded shank, so a balance never reads as an add button. A slot's bolt is the face-on head with a cross (see Bolt slots); the two are different things.
- **Nuts:** a hex nut with its hole.
- **Settings:** a gear with a centre hole.
- **Tabs:** the Lab is the robot's head, the Shop a parts crate with a nut stencilled on its front, Habits a frame on the bench with its top bolt driven, the Diary a pen. Tab icons draw at 24dp.

### Diary page
A true diary template on the owner's paper: the weekday large in Lexend with the date beneath, a header rule, evenly ruled lines (no grid), a BOLTS field of five hex holes to fill in by hand and a DAY count at the top right beside the atom emblem, a large faint atom near the foot, and the log's owner and catalogue number in tracked caps at the bottom. The tools stand in a 56dp rail down the right edge, away from the writing hand: the calendar at the top; pen, eraser, Move (four arrows round a pivot, raised while one finger moves the page instead of writing), a hairline and the five inks in the middle; undo, redo, paper and bin at the foot; on a short window the inks fold into the current ink's swatch. Tool tiles are 44x40dp with 10dp corners, the tool in hand raised with a 2x14dp ready-green bar on its inner side; ink swatches are 40x36dp targets with 22dp dots (26dp and a 2dp frost ring when chosen); an ink whose contrast with the page's paper is under 1.5:1 (ice blue on cream, matte black on matte black) shows at 40% with a thin dim slash through it, and can still be chosen. Menus open to the rail's left. Pen and eraser settings are labelled sliders with their value in Numeral and a true-size preview; the stabilizer shows its ten steps as ticks. The eraser's outline on the page is a white ring inside a dark one over a faint fill.

### Motion and sound
One frame clock drives every animation and freezes when the system's animations are off; it is read in the draw phase, so the bay redraws each frame without recomposing, and it stops while the bay is off screen. Done presses in to 96% over 150ms, and a card that completes cross-fades into its finished chip over 250ms; with animations off both land at once. The robot idles, cheers on Done, droops on Fail and waves when its nameplate is tapped; bolts drive in, the reactor breathes and turns, positrons rise. The frame clock ticks at most every 13ms (60 to 72 redraws a second), so the tablet's 144Hz panel does not paint the room twice as often as anything in it moves.

Sound is the workshop's: synthesized by tools/sfx.py and played at the media volume (system sonification is muted by silent and vibrate modes, and by HyperOS outright). Done is a short servo turn, three ratchet clicks and a seated bolt; a frame going critical adds a rising reactor hum, thickening Geiger ticks and a Cherenkov shimmer of glassy bells; Skip is a relay over and back; Fail is a metal crack, a sagging motor and a crackle of the counter; a level up is the robot's beeper climbing an arpeggio over a swell of hum (in place of Done's), a level lost a falling whine whose tremolo slows; a nut is two brass clinks; a purchase is a pneumatic sigh, a crate landing and two clinks; fitting a part is a servo swing and a latch; Undo is a falling blip; a new habit is three rising taps and a chirp, a deleted one is unbolted and clanks into the bin; the robot answers a tap with a bee-boop; a tab change is the faintest relay tick; binning a page is a decontamination hiss. Notifications play the robot's three-note call over a few Geiger ticks. Settings has a switch.

Press feedback always takes the shape that is drawn: a control whose touch area is larger than its drawing (a bottom-bar tab, a calendar day, an ink swatch, a shop tile, a segmented choice, a shop category) shows its press only inside the pill, ring, swatch, panel or slider it draws, never as the bare rectangle of its touch area; areas whose answer is the screen changing (a habit card's words, the nameplate) show no press shade at all.

## Do's and Don'ts

### Do:
- **Do** give each habit exactly one ready-green Done pill and put Skip, Fail, take back, Edit and Delete in its menu.
- **Do** show every slot state with its own bolt form (driven, dashed, sheared, open hole) so state never depends on colour alone.
- **Do** set every screen in Lexend, counts and balances in Numeral with tabular figures.
- **Do** put content on cards one tonal step above the ground, and lift surfaces by tone instead of shadows.
- **Do** keep uranium green as light: the reactor, the rail, gauges, completed work.
- **Do** show selection in cool steel and offer Undo in the snackbar after every log and purchase.
- **Do** keep the diary page on the owner's papers and inks, ruled lines only, with the atom emblem.

### Don't:
- **Don't** use ready green for decoration, emphasis or non-place selection; it means pressable or here.
- **Don't** paint a control, label or text uranium green.
- **Don't** use coral for anything but failure and destruction, or gold for anything but nuts.
- **Don't** nest cards, or build the dark-dashboard default of glowing stat tiles and progress rings.
- **Don't** add ornaments beyond bolts, frames and the atom, or bring back plants.
- **Don't** put squared or grid paper anywhere; calendars use ruled rows, the diary ruled lines.
- **Don't** draw icons outside the robot grammar or substitute glyphs and emoji for them.

## Parts catalogue

The shop sells parts in six slots, drawn from the robot roles in Theme.kt.

**One bay, everywhere.** The Lab, Shop and the base tiles all draw the same room picture with the same framing (scene x 40..960, y 120..880 always visible), so trying a part on in the Shop shows it exactly as it will look on the Lab.

**Finishes.** The starter parts keep the white factory shell; every other part carries its own finish (shell gradient, flat panels, joints, eyes and glass): Copper, Cobalt, Signal orange, Mint enamel, Stealth, Brass, Violet, Uranium green, Chrome, Hazard yellow, Rose and Cherenkov navy. A robot built from the catalogue is a deliberate patchwork; the neck takes the body's joint colour.

**Rooms.** Each base is a room with its own wall, floor and light colour, which tints the skirting and the light rail and its cone:
- **Charging Bay:** greenhouse teal and uranium light; panelled walls and a charging station whose screen shows the Battery attribute in five cells, cabled to the platform.
- **Workshop:** warm wood and amber light; a pegboard with spanner, hammer and screwdriver, parts bins, hazard tape along the skirting.
- **Observatory:** violet night; a bolted porthole onto a nebula and a ringed sunset planet.
- **Star Deck:** teal station light; a panoramic window onto a planet's limb and its moon, and a console of coloured keys.
- **Reactor Hall:** uranium green; an arched window onto cooling towers breathing steam at dusk, the radiation sign, and coolant pipes with an orange valve.

The workshop line: **Welding Mask** (head, one slit eye that blinks like a pair), **Tokamak** (body, a torus window whose plasma ring circulates), **Wrenches** and **Arc Welders** (arms), **Hydraulics** (legs), **Dosimeter** and **Hard Hat** (accessories), **Reactor Hall** (base, tier 5). The Aquarium became the **Coolant Tank** and the Sprout the **Isotope Antenna**; plants were removed from the app.

## Levels

Each attribute climbs ten levels; level n starts at 10 n (n - 1) points (0, 20, 60, 120 ... 900) and points count on past the top. Done adds the habit's weight in points, Fail takes them back, so a level can be lost. Reaching a level for the first time pays 2 nuts (stored on the mark, so undo is exact); climbing back to a level already reached pays nothing. The robot cheers, the level-up sound plays and the snackbar leads with the news ("Level up! Core is now Fission Pile, level 4, +2 nuts").

The stages, in the robot's own terms (named in the level-up news and the form's reward card, not on the Lab):
- **Battery:** AA Cell, Nickel Pack, Lithium Cell, Li-ion Bank, Solid-State, Supercapacitor, Graphene Cell, Fusion Cell, Zero-Point Cell, Antimatter Cell.
- **Processor:** 4-bit, 8-bit, 16-bit, 32-bit, 64-bit, Multicore, Neural Engine, Quantum Core, Photonic Mind, Singularity.
- **Chassis:** Tin Can, Aluminium, Steel, Titanium, Carbon Fibre, Tungsten, Graphene, Nanoweave, Diamond Lattice, Neutronium.
- **Core:** Spark, Dynamo, Turbine, Fission Pile, Breeder, Tokamak, Fusion Heart, Stellarator, Star Core, Supernova.

The room answers too: the reactor's glow, lamps, orbits and gauge follow the Core level, and the Charging Bay's station lights a cell for every two Battery levels. Mood follows the average level (under 2 low power, 4 and up fully charged).
