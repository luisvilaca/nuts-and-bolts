---
name: Nuts And Bolts
description: A habit tracker and diary where a small robot bolts together a frame for every habit at a reactor-powered workbench.
colors:
  graphite: "#0C1320"
  rail-graphite: "#070C15"
  raised-graphite: "#17223A"
  hairline: "#1B2638"
  edge-graphite: "#2A3A55"
  bay-sky: "#111A2C"
  bay-floor: "#0F1828"
  frost: "#E8EEF7"
  dim-slate: "#94A3BA"
  cool-steel: "#D3DDEA"
  joint-slate: "#6F809A"
  ready-green: "#6EE787"
  on-ready: "#06281A"
  cherenkov-blue: "#4CC9FF"
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
    fontSize: "32sp"
    fontWeight: 700
    lineHeight: "40sp"
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
  tile: "12dp"
  menu: "16dp"
  bay: "20dp"
  pill: "9999dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  lg: "20dp"
  xl: "28dp"
  xxl: "40dp"
components:
  button-done:
    backgroundColor: "{colors.ready-green}"
    textColor: "{colors.on-ready}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
    height: "48dp"
  button-done-disabled:
    backgroundColor: "{colors.raised-graphite}"
    textColor: "{colors.dim-slate}"
    rounded: "{rounded.pill}"
    height: "48dp"
  button-done-lead:
    backgroundColor: "{colors.ready-green}"
    textColor: "{colors.on-ready}"
    rounded: "{rounded.pill}"
    height: "64dp"
  icon-button-skip:
    textColor: "{colors.cool-steel}"
    rounded: "{rounded.pill}"
    size: "48dp"
  icon-button-fail:
    textColor: "{colors.shear-coral}"
    rounded: "{rounded.pill}"
    size: "48dp"
  button-outlined:
    textColor: "{colors.frost}"
    rounded: "{rounded.pill}"
    height: "48dp"
  fab:
    backgroundColor: "{colors.ready-green}"
    textColor: "{colors.on-ready}"
  nav-container:
    backgroundColor: "{colors.rail-graphite}"
    textColor: "{colors.dim-slate}"
  nav-item-selected:
    backgroundColor: "{colors.raised-graphite}"
    textColor: "{colors.frost}"
  segment-active:
    backgroundColor: "{colors.raised-graphite}"
    textColor: "{colors.frost}"
  chip-selected:
    backgroundColor: "{colors.raised-graphite}"
    textColor: "{colors.frost}"
  part-tile:
    backgroundColor: "{colors.raised-graphite}"
    rounded: "{rounded.tile}"
  bay-panel:
    backgroundColor: "{colors.bay-sky}"
    rounded: "{rounded.bay}"
    padding: "20dp"
  snackbar:
    backgroundColor: "{colors.raised-graphite}"
    textColor: "{colors.frost}"
  snackbar-action:
    textColor: "{colors.ready-green}"
  menu:
    backgroundColor: "{colors.raised-graphite}"
    rounded: "{rounded.menu}"
    padding: "16dp"
---

# Design System: Nuts And Bolts

## Overview

**Creative North Star: "Reactor Workshop"**

A robot's workbench after dark. The app is a deep graphite-blue workshop in which the robot stands on its round platform beside a steel bench. Under the bench sits a small reactor that powers it all; its Cherenkov-blue core and the light rail it feeds hang over the bench and glow brighter as the Core attribute rises. Every habit running today is a steel frame clamped upright on the bench and every slot is a bolt hole in it: Done drives a bolt home, Skip leaves a dashed ring, Fail shears the bolt, and an open slot is an empty tapped hole. The week is seven small plates with three holes each, more bolted the better the day went. Cherenkov blue belongs to the reactor alone; the interface itself stays graphite, cool steel and frost, with ready green as the one live colour.

Hierarchy comes from type scale and hairlines, not boxes. Rows are separated by one-pixel graphite rules, sections are announced by small tracked caps, and the only filled surfaces are controls, shop part tiles and the framed bay. The bolt is the single ornament and always carries meaning: the slot, the day, the five hex holes of the diary page's gauge and the bolt emblem printed on every page.

Density is calm and evening-friendly: 48dp targets, 28dp gutters on wide windows, one clear Done per row with Skip and Fail quieter beside it. One rounded, open face (Lexend) carries everything.

**Key Characteristics:**
- Graphite-blue ground with a deeper rail; tonal steps, no shadows.
- One live accent (ready green); gold for nuts, coral for failure, cool steel for selection, metal and driven bolts.
- Cherenkov blue only as the reactor's light inside the bay.
- Lexend throughout: bold display, semibold titles, tracked small caps for labels.
- Bolts and frames as the one ornament and the one state language.
- Hairline rows, no cards around sections.
- Robot-grammar line icons: 24dp, 1.8 stroke, square caps, pivot-joint circles.

## Colors

A dark workshop lit by one small reactor, where green means ready or pressable and every other colour holds a single job.

### Primary
- **Ready Green** (ready-green): the Done pill, primary buttons and the FAB, and the place markers (selected bar icon, the shop tab indicator, today's ring on the calendar, the active diary tool's underline, the diary calendar's page mark). Also field focus, the reminder switch track, confirm text actions and the snackbar's Undo. In the robot it lights the eyes, the smile and the dashed platform ring. Text on it is **On-Ready** (on-ready), a near-black green.

### Secondary
- **Cool Steel** (cool-steel): bolts and their counts, a driven bolt's head, the frames' 70% rim and the bench's top edge, attribute icons and gauge fills, and every selected state that is not a place: the active segment's border, selected chips' border, the selection ring on a shop part, the robot's nameplate.
- **Joint Slate** (joint-slate): the robot's joints and claws, a bolt head's rim, the light rail's hanging wires, the frames' clamps, the reactor's casing and conduit, and the rim of a frame whose habit failed today.

### Tertiary
- **Cherenkov Blue** (cherenkov-blue): the reactor's light only: its core window (35% to 90% with the Core attribute, breathing slowly), the light rail over the bench (solid, with a halo), the radial glow washing the bay at 10% to 32%, the faint light cone, the base's orbiting rings at higher tiers, and the Isotope Antenna's bulb. Never in the interface chrome.
- **Nut Gold** (nut-gold): nuts, their counts, and gold parts of the robot's world. Never a general highlight.
- **Shear Coral** (shear-coral): sheared bolts on rows, frames and the week; the Fail button's cross and its 45% border; destructive text actions; form errors.

### Neutral
- **Graphite** (graphite): the ground of every screen.
- **Rail Graphite** (rail-graphite): the bottom bar, the diary toolbar, the robot's face glass, an open bolt hole, the reactor's fuel rods and the bay floor's lower edge; a step deeper than the ground.
- **Raised Graphite** (raised-graphite): controls at rest, the selected nav indicator, active segments and selected chips, disabled Done, shop part tiles, menus, the snackbar, the frames, the day plates and the reactor vessel.
- **Bay Sky** (bay-sky) to **Bay Floor** (bay-floor): the bay's vertical gradient (sky into ground) and its floor band (floor into rail), with a 10% cool-steel skirting line where they meet.
- **Hairline** (hairline): row dividers, calendar week rules, empty gauge and run tracks.
- **Edge Graphite** (edge-graphite): outlines at rest (outlined buttons, fields, chips, segments, Skip), disabled glyphs, future-day plates, the bench top and legs.
- **Frost** (frost): primary text. **Dim Slate** (dim-slate): secondary text, metadata, labels, unselected nav, open and skipped bolt holes.

### Diary papers and inks
The diary page keeps the owner's own materials, separate from the app palette (owner-pinned). Papers (three replaceable slots, each page keeps the paper it was written on): Matte Black (paper-matte-black), Cream (paper-cream), Night (paper-night). Inks with the owner's roles: Ice Blue (ink-ice-blue) for text, Matrix Green (ink-matrix-green) for headers, Electric Cyan (ink-electric-cyan) for vectors and equations, Neon Fuchsia (ink-neon-fuchsia) for annotations, Matte Black (ink-matte-black) for cream paper. The printed template follows the paper: on dark paper the weekday, header rule and small bolt emblem are Matrix Green, the date and field labels Ice Blue at 72%, the rules white at 15% and the large foot emblem Electric Cyan at 20%; on light paper all of it is Matte Black at matching strengths.

### Named Rules
**The Ready Or Pressable Rule.** Ready green appears only where something can be pressed or marks where the owner is (plus the starter robot's eyes, smile and the platform ring; painted parts carry their finish's own eye colour). Done is shown by a steel bolt, not by green. Selection that is not a place is cool steel.

**The Light Stays In The Bay Rule.** Cherenkov blue and each room's light colour are light, not paint: they live inside the bay (the reactor core, the light rail and its cone, the room's glow) and never colour a control, a label or a mark.

**The One Meaning Rule.** Gold is nuts, coral is failed or destructive. Neither decorates, warns generally, or adds variety.

## Typography

**Display Font:** Lexend, variable, weights 600/700 (bundled, with sans-serif fallback)
**Body Font:** Lexend, variable, weights 400/500/600
**Label Font:** Lexend Medium, tracked uppercase

**Character:** One rounded, open, highly legible face throughout; weight and tracking do the work a second family would.

### Hierarchy
- **Display** (700, 32sp, 40sp, -0.01em): screen titles in the header; on a phone, the name of the next habit to log on Today.
- **Headline** (600, 22sp, 28sp): the empty-state line ("The bench is empty").
- **Title Large** (600, 21sp, 28sp): a shop part's name in its detail, the selected day's name on Habits.
- **Title** (600, 18sp, 24sp): habit names in rows and history, the month and day stepper.
- **Body Large** (400, 16sp, 24sp): header subtitle (the date), notes, part names in the grid, attribute names (at 500).
- **Body** (400, 14sp, 20sp): habit metadata joined by " · ", summaries, helper text.
- **Label Large** (600, 16sp, 20sp): button text, shop tabs, calendar day numbers.
- **Label Medium** (500, 13sp, 16sp): weekday names under the week's plates, attribute glosses.
- **Label** (500, 13sp, 0.14em tracking, uppercase, dim slate): section labels ("ATTRIBUTES", "THIS WEEK"), the day's status line ("TODAY · 4 OF 9 BOLTS FASTENED · 1 FAILED"), form field labels, calendar weekday heads, part catalogue numbers, the robot's nameplate (at body-large size in cool steel).
- **Numeral** (600, 18sp, tabular figures): wallet balances, attribute values and percentages, the target stepper, the reminder time. At small sizes it drops to 14sp.

### Named Rules
**The One Face Rule.** Lexend sets everything, including the printed diary template. Contrast comes from weight (400 to 700), size and tracked caps, never from a second family.

**The Tabular Count Rule.** Every count and balance uses Numeral with tabular figures so columns of numbers hold still as they change.

## Layout

Two window classes drive everything. A window is **wide** when it is landscape and at least 600dp wide: the Material navigation rail sits at the left and screens split into two panes. Otherwise the Material navigation bar sits at the foot and screens stack in one scroll. A window whose short side is under 600dp is a **phone**.

- **Wide Today and Shop:** header across the top with 28dp side gutters; the bay takes 46% at left, the list or catalogue 54% at right (8dp start, 32dp end padding). Shop's part detail sits under its bay.
- **Habits and Diary:** no bay at any size; the robot rides as a badge in the header, the day or month summary sits beside the title on wide windows (below it when stacked), and the planner or calendar takes the full width.
- **Stacked:** 20dp side gutters; the bay sits under the header (560dp tall on a tablet in portrait, 360dp on a phone, the same on Today and Shop). Habits, Diary and the form show the robot badge.
- **Forms and Settings:** one column capped at 720dp, 40dp gutters wide, 20dp stacked.
- **Rows:** 12dp vertical padding, a hairline under each. A habit row whose column is under 560dp puts its buttons on a second line with Done filling the width.
- **Phone Today:** the next habit still to log leads the list at display size with a 64dp Done; the rest follow at normal size.
- **Section rhythm:** section labels sit 28dp below what precedes them and 10dp above what they label (22dp and 8dp in the form). Gaps between controls are 8dp or 12dp.
- **Shop grid:** as many columns as fit at 156dp each (at least two), 12dp gaps.
- **Calendars:** a hairline grid: each week a ruled row, each day a cell at least 64dp tall with its number above and its mark (a day plate, or the pen icon for a diary page) beneath.

### Named Rules
**The No Card Rule.** Sections are never boxed. Hierarchy is type scale, tracked labels and hairlines; the only filled surfaces are controls, shop part tiles, menus and the bay.

## Elevation & Depth

Flat. Depth is tonal: rail graphite sits below the graphite ground and raised graphite lifts controls and tiles. The one atmospheric effect is light, inside the bay: a vertical sky gradient, the floor band graded into the rail, and the reactor's Cherenkov radial glow. The robot's shell carries the only gradient on a figure (white to pale steel blue). Material's own FAB and snackbar keep their default elevation; nothing else casts a shadow.

### Named Rules
**The Tonal Step Rule.** To lift a surface, move it one graphite step (rail, ground, raised). Never add a shadow; the only glow in the app is the reactor's.

## Shapes

Soft and machined against straight hairlines. Every pressable action is a pill or a circle; the bay is framed with 20dp corners; part tiles, week days and diary tools have 12dp corners, calendar cells 10dp, menus 16dp, paper swatches 6dp. Progress is a pill track: the attribute gauge is 8dp tall, a habit's run bar 14dp with the best run in cool steel at 55% and the current run in ready green, drawn exactly its days long. The bolt family is drawn from circles: a solid head with a cross, a dashed ring, a split head and a ringed hole.

## Components

### Buttons
Confident on the primary, quiet on the rest.
- **Shape:** full pill (rounded.pill), 48dp tall; the phone's lead habit uses 64dp.
- **Done / primary:** ready green with on-ready text in Label Large. Disabled when the period is full: raised graphite with dim text. Bad habits relabel the trio Resisted / Skip / Gave in.
- **Skip:** a 48dp outlined circle, 1dp edge-graphite border, a cool-steel minus.
- **Fail:** a 48dp outlined circle, coral cross, border coral at 45%; both fall to edge graphite when disabled.
- **Outlined:** 1dp edge-graphite border, frost text (Cancel, Take off, backup, date pickers).
- **Text actions:** ready green for confirm, coral for destructive, frost for dismiss.
- **FAB:** extended, ready green with on-ready content, bottom-end with a 24dp inset (Add habit).

### Chips
- **Style:** Material filter chips, 1dp edge-graphite border, frost label.
- **State:** selected fills raised graphite with a 1.5dp cool-steel border. Starter-habit suggestion chips are 40dp tall with an edge-graphite border.

### Cards / Containers
- **Bay panel:** the bay clipped to 20dp corners inside 12dp of padding, with the robot's nameplate (name in tracked caps, mood and "building N frames" in dim body) 20dp inside its top corner on Today.
- **Shop part tile:** one raised-graphite tile per part, 12dp corners, square art; the selection ring (2dp cool steel) sits on the tile itself. Name and price or status sit beneath it (catalogue numbers stay in the data, never on screen), unboxed.
- **Menus:** raised graphite, 16dp corners, 16dp padding, 300dp wide.
- **Shadow Strategy:** none (see Elevation & Depth).

### Inputs / Fields
- **Style:** Material outlined text fields, edge-graphite border at rest.
- **Focus:** border, label and cursor turn ready green.
- **Error:** coral body text below the form.

### Navigation
- **Bar:** Today, Shop, Habits, Diary on rail graphite at the bottom of every window, never a side rail; Settings is an icon in the header.
- **States:** selected item gets a raised-graphite indicator, ready-green icon and frost label; unselected are dim slate.
- **Segmented views (Day / Calendar / History):** active segment raised graphite with frost text and a cool-steel border; inactive on the ground with edge-graphite borders.
- **Header:** at least 72dp; display title, dim date beside it, the wallet at right (bolt icon and count in cool steel, nut in gold, 22dp icons, Numeral). Screens without a bay put a 40x52dp robot badge before the title; on a phone the badge replaces the wallet.
- **Snackbar:** raised graphite with frost text and a ready-green Undo, for every log and purchase.

### Bolt slots (signature)
Each slot state has its own form, seen face on; colour only confirms it.
- **Done:** a bolt head driven home, solid cool steel with a joint-slate rim and the bolts currency's cross recessed in graphite.
- **Skipped:** a dashed dim-slate ring with a dash across it (the Skip button's minus).
- **Failed:** a sheared head, solid coral, split diagonally into two halves pushed apart.
- **Open:** an empty tapped hole: a rail-graphite disc with a dim-slate ring and a fainter inner thread ring.
- A slot logged on another day of the week shows at 45%. Row marks are 26dp, 4dp apart; tapping an own mark removes the newest mark of the day.

### The reactor bench and day plates (signature)
- **Bay:** the robot on its round platform (dashed green ring) steps left; at right, two joint-slate wires hang the Cherenkov light rail over a steel bench on two legs. Between the legs the reactor vessel (raised graphite, joint-slate casing) shows its blue core window crossed by three fuel rods, fed up to the bench by a short conduit; its glow and the rail brighten with the Core attribute and breathe slowly. One frame per habit running today, up to six, spaced evenly: a raised-graphite plate clamped upright whose height grows with its target, with one bolt hole per slot from the bottom up; its rim is joint slate if the habit failed today. With no habits the bench holds one dashed frame.
- **Drive:** Done screws the newest bolt on that habit's frame in over 700ms (cubic ease-out): the head grows to size while its cross turns three quarters of a turn, and the robot cheers.
- **Day plate:** a day as a small plate with three bolt holes, one, two or three driven home (any done, half, all); the last hole shows a sheared bolt when anything failed; a dashed blank plate while the day is ahead, a plain blank plate when nothing ran. 42dp in the week strip, 26dp in the calendar.

### Icons
One robot grammar: 24dp line art at 1.8 stroke, square caps, mitred joins, angular strokes and a small pivot-joint circle wherever a part would hinge. Tinted by context (cool steel, dim, ready green, gold, coral).

### Diary page
A true diary template on the owner's paper: the weekday large in Lexend with the date beneath, a 2.5-unit header rule, 37 evenly ruled lines (no grid), a BOLTS field of five hex holes to fill in by hand and a DAY count at the top right beside a small bolt emblem (side on, threaded), a large faint bolt near the foot, and the log's owner and catalogue number in tracked caps at the bottom. The toolbar is one thin rail-graphite line (44dp) in three parts: the calendar at left, the pen, the eraser, a hairline divider and the inks centred, undo, redo, paper and bin at right; on a narrow window the inks fold into the current ink's swatch and the groups spread instead of centring. Tool tiles are 44x40dp with 10dp corners, the tool in hand raised with a 14x2dp ready-green underline; ink swatches are 36dp targets with 22dp dots (26dp and a 2dp frost ring when chosen). Pen and eraser settings are labelled sliders with their value in Numeral at the right and a true-size preview (the nib's line in the current ink, the eraser's outline); the stabilizer shows its ten steps as ticks. The eraser's outline on the page is a white ring inside a dark one over a faint fill. The tab bar hides under an open page.

### Motion
One frame clock drives every animation and freezes when the system's animations are off. The robot idles, cheers on Done, droops on Fail and waves when its nameplate is tapped; bolts drive in and the reactor breathes as above.

## Do's and Don'ts

### Do:
- **Do** give each habit row exactly one ready-green Done pill (48dp, full pill) and keep Skip and Fail as 48dp outlined circles beside it.
- **Do** show every slot state with its own bolt form (driven, dashed, sheared, open hole) so state never depends on colour alone.
- **Do** set every screen in Lexend, counts and balances in Numeral with tabular figures.
- **Do** separate rows and calendar weeks with one hairline (hairline) and announce sections with a 13sp label tracked 0.14em.
- **Do** lift surfaces by one graphite step (rail, ground, raised) instead of shadows.
- **Do** keep Cherenkov blue inside the bay as the reactor and the light it powers.
- **Do** show selection in cool steel and offer Undo in the snackbar after every log and purchase.
- **Do** keep the diary page on the owner's papers and inks, ruled lines only.

### Don't:
- **Don't** use ready green for decoration, emphasis or non-place selection; it means pressable or here.
- **Don't** put Cherenkov blue on a control, label, mark or text.
- **Don't** use coral for anything but failure and destruction, or gold for anything but nuts.
- **Don't** wrap sections in cards, or build the dark-dashboard default of glowing stat cards and progress rings.
- **Don't** add ornaments beyond bolts and frames, or bring back plants.
- **Don't** put squared or grid paper anywhere; calendars use ruled rows, the diary ruled lines.
- **Don't** draw icons outside the robot grammar (24dp, 1.8 stroke, square caps, pivot joints) or substitute glyphs and emoji for them.

## Parts catalogue

The shop sells parts in six slots, drawn from the robot roles in Theme.kt.

**One bay, everywhere.** Today, Shop and the base tiles all draw the same room picture with the same framing (scene x 40..960, y 120..880 always visible), so trying a part on in the Shop shows it exactly as it will look on Today.

**Finishes.** The starter parts keep the white factory shell; every other part carries its own finish (shell gradient, flat panels, joints, eyes and glass): Copper, Cobalt, Signal orange, Mint enamel, Stealth, Brass, Violet, Uranium green, Chrome, Hazard yellow, Rose and Cherenkov navy. A robot built from the catalogue is a deliberate patchwork; the neck takes the body's joint colour.

**Rooms.** Each base is a room with its own wall, floor and light colour, which tints the skirting, the light rail and its cone:
- **Charging Bay:** graphite and Cherenkov blue; panelled walls and a charging station whose screen shows the Battery attribute in five cells, cabled to the platform.
- **Workshop:** warm wood and amber light; a pegboard with spanner, hammer and screwdriver, copper, teal and amber parts bins, hazard tape along the skirting.
- **Observatory:** violet night; a bolted porthole between the rail's wires onto a nebula and a ringed sunset planet.
- **Star Deck:** teal station light; a panoramic window onto a planet's limb and its moon, and a console of coloured keys.
- **Reactor Hall:** uranium green; an arched window onto cooling towers breathing steam at dusk, the radiation sign, and coolant pipes with an orange valve running into the reactor.

**The reactor** under the bench in every room: a domed steel vessel on a plinth with two bolted flanges, a viewport where four fuel rods stand dark against Cherenkov light (brighter with Core), a conduit up to the bench, and a pressure gauge whose needle reads the Core attribute. The workshop line added with the Reactor Workshop look:
- **Welding Mask** (head): a box head behind a dark riveted visor; its eyes are one Cherenkov slit that blinks like a pair.
- **Tokamak** (body): a rounded body with a torus window whose blue plasma ring circulates.
- **Wrenches** and **Arc Welders** (arms): open-end wrenches; torch nozzles with a pulsing blue arc.
- **Hydraulics** (legs): piston cylinders over flat feet.
- **Dosimeter** and **Hard Hat** (accessories): a chest badge with a glowing reading; a gold hard hat with a blue lamp.
- **Reactor Hall** (base, tier 5): two cooling towers breathing steam on the left of the bay.
- The Aquarium became the **Coolant Tank** (bubbles instead of plants), and the Sprout the **Isotope Antenna**. No plants anywhere.
