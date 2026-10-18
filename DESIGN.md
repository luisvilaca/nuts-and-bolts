---
name: Nuts And Bolts
description: A habit tracker and diary where a small robot bolts together a frame for every habit at a reactor-powered workbench.
colors:
  ground: "#0D1615"
  rail: "#081313"
  card: "#152220"
  raised: "#15302B"
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
  focus: "#4AE3A1"
  uranium-glow: "#A6FF4D"
  nut-gold: "#F2C14E"
  shear-coral: "#FF6B4A"
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
- **Focus** (focus): a mint outline and cursor on the field being typed in.

### Secondary
- **Cool Steel** (cool-steel): driven bolt heads and their counts, the frames' 70% rim and the bench's top edge, attribute icons, gauge fills (with the glow), and every selected state that is not a place: the active segment's border, selected chips, the selected habit card's edge, the selection ring on a shop part, the robot's nameplate.
- **Joint** (joint): the robot's joints and claws, a bolt head's rim, the light rail's wires, the frames' clamps, the reactor's coil packs, and the rim of a frame whose habit failed today.

### Light
- **Uranium Glow** (uranium-glow): every glow in the app and nothing else. The reactor's plasma core, orbits, electrons, level lamps and the pulses climbing its cable; the light rail over the bench and its cone; the radial wash over the bay; the attribute gauges (a glow-to-steel fill with a soft halo and a bright leading spark); the done segments of a habit's tracker; a critical frame and its positrons. Other base rooms keep their own lamp colour on the rail (amber, violet, teal station light); the reactor glows green in every room.

### Tertiary
- **Nut Gold** (nut-gold): nuts, their counts, gold parts of the robot's world. Never a general highlight.
- **Shear Coral** (shear-coral): sheared bolts and failed tracker segments; Fail and Gave in in the habit menu; destructive text actions; form errors.

### Neutral
- **Ground** (ground): every screen.
- **Rail** (rail): the bottom bar, the diary tool rail, the robot's face glass, an open bolt hole; a step below the ground.
- **Card** (card): every card, with a 1dp white-8% **Border** (border).
- **Raised** (raised): controls at rest, the selected tab indicator, the active segment, selected chips, disabled Done, shop part tiles, menus, the snackbar, the frames and the day plates.
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

- **Wide Lab and Shop:** header across the top with 28dp side gutters; the bay takes 46% at left, the side or catalogue 54% at right. On the Lab the side holds today's habit cards, which scroll on their own, over the four attribute cards pinned at the bottom.
- **Stacked Lab:** on a tablet held upright the bay sits under the header, the habits scroll below it and the attributes stay pinned at the foot; on a phone the page scrolls, attributes under the bay and habits after.
- **Habits:** no bay. Day / Calendar / History in a sliding segmented control. Wider than 720dp the Day view splits: habit cards left (56%), the picked habit's detail right (44%). The calendar fills the space below its stepper and never scrolls.
- **Diary:** a calendar card that fills the screen; "Write today" (or "Open today") is a quiet text action beside the month. An open page fills the window beside a 56dp tool rail on the right edge; the bottom bar hides.
- **Form:** cards, one group each. Wider than 720dp, two columns: Name, Type, Attribute and Schedule left; Weight, Reward and the actions right.
- **Settings:** one column capped at 720dp.
- **Rhythm:** 10dp to 12dp between cards, 16dp inside them; group labels 10dp above their content.
- **Shop grid:** as many columns as fit at 156dp each (at least two), 12dp gaps.

### Named Rules
**The Content On Cards Rule.** Habits, form groups, history items and calendars sit on cards (12dp corners, 16dp inside, the card surface and a white-8% border). The bay is framed, not carded. Nothing nests a card in a card.

## Elevation & Depth

Flat. Depth is tonal: rail < ground < card < raised, each one step. The one atmospheric effect is light: inside the bay a vertical sky gradient, the floor band graded into the rail, and the reactor's uranium radial wash; outside it, the gauges' and trackers' glow. The robot's shell carries the only gradient on a figure. Material's FAB and snackbar keep their default elevation; nothing else casts a shadow.

## Shapes

Soft and machined. Every pressable action is a pill or a circle; the bay is framed with 20dp corners; cards and part tiles have 12dp corners, fields 10dp, calendar cells 10dp, menus 16dp, paper swatches 6dp. Progress is a pill track: the attribute gauge, the habit tracker (one segment per slot, 4dp apart), a habit's run bar. The bolt family is drawn from circles: a solid head with a cross, a dashed ring, a split head and a ringed hole.

## Components

### Buttons
- **Shape:** full pill, 44dp tall (52dp for a lead Done).
- **Done / primary:** ready green with on-ready text in Label Large. When it cannot log it falls to raised with dim text and says why: "All in" (a break habit: "Held") when every bolt of the period is in, "Closed" when the period is full with a skip or a sheared bolt in it, "Not yet" on a future day. Break habits read Resisted / Skip / Gave in.
- **Menu button:** a 44dp outlined circle with the three-dot icon in cool steel, beside Done. Its menu: Skip, Fail (coral), Take back the last bolt, then Edit habit and Delete habit (coral). Delete asks first.
- **Outlined:** 1dp edge border, frost text (Cancel, date pickers, backup).
- **Text actions:** ready green for confirm, coral for destructive, frost for dismiss.
- **FAB:** extended, ready green, bottom-end with a 20dp inset (Add habit).

### Habit card
A card per habit: the name in Title (a dim chevron after it where the name opens the form), the metadata in dim Body, a counter (the bolt icon and "2 / 3" in Numeral 14sp, frost when full) over the tracker, then Done and the menu button. Narrower than 480dp the counter and buttons take their own lines. On the split Habits view a card is picked by tapping it and shows a cool-steel edge.

### Tracker
A habit's period as an 8dp pill split into one segment per slot: done in a glow-to-steel gradient with a faint halo, failed coral, skipped a dashed dim outline, open an edge track. A slot logged on another day of the week is at half strength.

### Segments
A pill outlined in edge whose selection, a raised pill with a 1.5dp steel border, slides between options on a spring (damping 0.8) while the label colours cross-fade. Labels run to two lines. Four options in a column narrower than 420dp fall into a two-by-two grid of filter chips.

### Words
One word for a slot on screen: the bolt. "Slot" and "mark" are code names only. A count reads "4 of 9 bolts in", with ", 1 sheared" for fails, or "9 bolts planned" for a day ahead; Done says "+5 bolts · +1 Battery", Fail "Sheared · −5 bolts · −1 Battery", the menu "Take back the last bolt". A break habit's metadata leads with "Break it".

### Fields
A white-5% fill, 10dp corners, 12 by 16dp inside, a white-8% border at rest and a mint outline and cursor when focused. Errors are coral body text under the form; a missing name shows first as a dim hint.

### Chips
Material filter chips, 1dp edge border, frost label; selected fills raised with a 1.5dp steel border. Starter-habit suggestion chips are 40dp tall.

### Navigation
- **Bar:** Lab, Shop, Habits, Diary on rail at the bottom of every window; Settings is an icon in the header that turns ready green while open.
- **States:** the selected tab gets a raised 52x28dp indicator, a ready-green icon and a frost label; unselected are dim.
- **Header:** at least 60dp; display title, the date or summary beside it on wide windows, the wallet at right (bolt icon and count in cool steel, nut in gold, 22dp icons, Numeral), then Settings.
- **Snackbar:** raised with frost text and a ready-green Undo, for every log and purchase; an Undo plays its own sound.

### Attribute cards (Lab)
Four small cards in a row: the attribute's icon, its name in Label Large and "LV n" in Numeral on one line, its meaning (Energy, Mind, Body, Heart) in dim Label Medium, and a 4dp gauge to the next level. Narrower than 560dp only the icon and level show (the screen reader still names them); narrower than 400dp they go two by two with words.

### Habit detail (split Habits)
The picked habit: its name in Headline with Edit, its metadata, four stats in Numeral over dim captions (done rate, running now, best run, failed in coral when any), a gauge of the current run against the best, and a LOG card listing every day it was logged, newest first, with that day's bolt marks and a count; a day opens that date.

### Reward card (form)
What the habit as set up pays: the bolts and points of each Done (or each resist), what a fail takes back, the most a period can bring, how its nuts are earned, and how many Dones take its attribute to the next stage.

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
- **Day plate:** a day as a small plate with three bolt holes, one, two or three driven home (any done, half, all); the last hole shows a sheared bolt when anything failed; a dashed blank plate while the day is ahead, a plain blank plate when nothing ran. 42dp in roomy calendar cells, 26dp in tight ones.

### Icons
One robot grammar: 24dp line art at 1.8 stroke, square caps, mitred joins, angular strokes and a small pivot-joint circle wherever a part would hinge. Tinted by context (cool steel, dim, ready green, gold, coral).

### Diary page
A true diary template on the owner's paper: the weekday large in Lexend with the date beneath, a header rule, evenly ruled lines (no grid), a BOLTS field of five hex holes to fill in by hand and a DAY count at the top right beside the atom emblem, a large faint atom near the foot, and the log's owner and catalogue number in tracked caps at the bottom. The tools stand in a 56dp rail down the right edge, away from the writing hand: the calendar at the top; pen, eraser, a hairline and the five inks in the middle; undo, redo, paper and bin at the foot; on a short window the inks fold into the current ink's swatch. Tool tiles are 44x40dp with 10dp corners, the tool in hand raised with a 2x14dp ready-green bar on its inner side; ink swatches are 40x36dp targets with 22dp dots (26dp and a 2dp frost ring when chosen). Menus open to the rail's left. Pen and eraser settings are labelled sliders with their value in Numeral and a true-size preview; the stabilizer shows its ten steps as ticks. The eraser's outline on the page is a white ring inside a dark one over a faint fill.

### Motion and sound
One frame clock drives every animation and freezes when the system's animations are off. The robot idles, cheers on Done, droops on Fail and waves when its nameplate is tapped; bolts drive in, the reactor breathes and turns, positrons rise. Short synthesized sounds (tools/sfx.py) mark Done (a ratchet and a seated bolt), Skip (a tick), Fail (a shear), a level up (a rising bell arpeggio, in place of Done's), a purchase (two coin clinks) and Undo (a falling blip); Settings has a switch.

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
