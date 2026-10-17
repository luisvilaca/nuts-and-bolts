---
name: Nuts And Bolts
description: A habit tracker and diary where a small robot lives inside the atom of the owner's day.
colors:
  night-indigo: "#0E1430"
  rail-indigo: "#0A0F25"
  raised-indigo: "#172050"
  hairline: "#1E2750"
  orbit-blue: "#2B3870"
  bay-floor: "#121A3D"
  glass: "#080C1E"
  starlight: "#E8EDF5"
  dim-steel: "#97A3C2"
  polished-steel: "#C9D3E0"
  joint-grey: "#7F8BA8"
  atomic-turquoise: "#3FE0C5"
  on-turquoise: "#052823"
  deep-turquoise: "#0B6B5D"
  starburst-gold: "#F2C14E"
  flame-coral: "#FF6B4A"
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
    fontFamily: "Michroma, sans-serif"
    fontSize: "28sp"
    fontWeight: 400
    lineHeight: "36sp"
  headline:
    fontFamily: "Michroma, sans-serif"
    fontSize: "20sp"
    fontWeight: 400
    lineHeight: "28sp"
  title-large:
    fontFamily: "Jost, sans-serif"
    fontSize: "22sp"
    fontWeight: 500
    lineHeight: "28sp"
  title:
    fontFamily: "Jost, sans-serif"
    fontSize: "19sp"
    fontWeight: 500
    lineHeight: "26sp"
  body-large:
    fontFamily: "Jost, sans-serif"
    fontSize: "17sp"
    fontWeight: 400
    lineHeight: "24sp"
  body:
    fontFamily: "Jost, sans-serif"
    fontSize: "15sp"
    fontWeight: 400
    lineHeight: "21sp"
  label-large:
    fontFamily: "Jost, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "20sp"
  label-medium:
    fontFamily: "Jost, sans-serif"
    fontSize: "13sp"
    fontWeight: 500
    lineHeight: "16sp"
  label:
    fontFamily: "Michroma, sans-serif"
    fontSize: "11sp"
    fontWeight: 400
    letterSpacing: "0.14em"
  numeral:
    fontFamily: "Jost, sans-serif"
    fontSize: "18sp"
    fontWeight: 500
    fontFeature: "tnum"
rounded:
  swatch: "6dp"
  cell: "10dp"
  panel: "12dp"
  menu: "16dp"
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
    backgroundColor: "{colors.atomic-turquoise}"
    textColor: "{colors.on-turquoise}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
    height: "48dp"
  button-done-disabled:
    backgroundColor: "{colors.raised-indigo}"
    textColor: "{colors.dim-steel}"
    rounded: "{rounded.pill}"
    height: "48dp"
  button-done-lead:
    backgroundColor: "{colors.atomic-turquoise}"
    textColor: "{colors.on-turquoise}"
    rounded: "{rounded.pill}"
    height: "64dp"
  icon-button-skip:
    textColor: "{colors.polished-steel}"
    rounded: "{rounded.pill}"
    size: "48dp"
  icon-button-fail:
    textColor: "{colors.flame-coral}"
    rounded: "{rounded.pill}"
    size: "48dp"
  button-outlined:
    textColor: "{colors.starlight}"
    rounded: "{rounded.pill}"
    height: "48dp"
  nav-container:
    backgroundColor: "{colors.rail-indigo}"
    textColor: "{colors.dim-steel}"
  nav-item-selected:
    backgroundColor: "{colors.raised-indigo}"
    textColor: "{colors.starlight}"
  segment-active:
    backgroundColor: "{colors.polished-steel}"
    textColor: "{colors.night-indigo}"
  chip-selected:
    backgroundColor: "{colors.polished-steel}"
    textColor: "{colors.night-indigo}"
  part-panel:
    backgroundColor: "{colors.raised-indigo}"
    rounded: "{rounded.panel}"
  snackbar:
    backgroundColor: "{colors.polished-steel}"
    textColor: "{colors.night-indigo}"
  snackbar-action:
    textColor: "{colors.deep-turquoise}"
  menu:
    backgroundColor: "{colors.raised-indigo}"
    rounded: "{rounded.menu}"
    padding: "16dp"
---

# Design System: Nuts And Bolts

## Overview

**Creative North Star: "Atomium Night"**

Expo 58 after dark. The app is a night-indigo hall in which the owner's robot stands on a round platform at the nucleus of an atom: every habit running today is an orbit around it, every slot an electron, and logging Done lights one. Polished steel carries the robot, the bolts and the gauges; atomic turquoise is the one live colour, reserved for what can be pressed, what is done, and where the owner is; starburst gold belongs to nuts; flame coral means failed and nothing else.

Hierarchy comes from type scale and hairlines, not boxes. Rows are separated by one-pixel indigo rules, sections are announced by small tracked Michroma labels, and the only filled surfaces are controls, shop part panels and the bay. The orbit ellipse is the single ornament and it is never decoration alone: it is the app mark, the habit, the day's mini-atom in the week and the calendar, the faint atom on the diary page, and the dashed orbit that waits around the robot before any habit exists.

Density is calm and evening-friendly: 48dp targets, generous 28dp gutters on wide windows, and one clear Done per row with Skip and Fail quieter beside it.

**Key Characteristics:**
- Night-indigo ground with a deeper rail; tonal steps, no shadows.
- One live accent (turquoise); gold for nuts, coral for failure, steel for selection.
- Michroma for display and tracked uppercase labels; Jost for everything read or counted.
- Orbits and electrons as the one ornament and the one state language.
- Hairline rows, no cards around sections.
- Robot-grammar line icons: 24dp, 1.8 stroke, square caps, pivot-joint circles.

## Colors

A deep, cold indigo hall lit by one turquoise signal, with steel, gold and coral each holding a single meaning.

### Primary
- **Atomic Turquoise** (atomic-turquoise): the Done pill, primary buttons and the FAB, lit electrons, done orbits, the robot's eyes and platform ring, and the place markers (selected rail and bar icon, shop tab indicator, today's ring on the calendar, the active diary tool's underline). Text on it is **On-Turquoise** (on-turquoise), a near-black green.
- **Deep Turquoise** (deep-turquoise): the snackbar's Undo action only, where turquoise must sit on light steel and keep contrast.

### Secondary
- **Polished Steel** (polished-steel): bolts and their counts, the robot's shell family, attribute icons, skipped electrons, and every selected state that is not a place: the active segment, selected chips, the selection ring on a shop part, the snackbar surface.
- **Joint Grey** (joint-grey): the robot's joints and claws, and the dark end of the attribute gauge gradient (joint-grey to starlight).

### Tertiary
- **Starburst Gold** (starburst-gold): nuts, their counts and the gold parts of the robot's world. Never a general highlight.
- **Flame Coral** (flame-coral): failed electrons, failed orbits and days, the Fail button's glyph and its 45% border, destructive text actions, form errors. Nothing else.

### Neutral
- **Night Indigo** (night-indigo): the ground of every screen.
- **Rail Indigo** (rail-indigo): the navigation rail, the bottom bar and the diary toolbar; a step deeper than the ground.
- **Raised Indigo** (raised-indigo): controls at rest, the selected nav indicator, disabled Done, shop part panels, menus.
- **Bay Floor** (bay-floor): the floor of the bay, graded down into the ground.
- **Glass** (glass): the robot's face glass and the space behind the bay.
- **Hairline** (hairline): row dividers, calendar week rules, empty gauge tracks.
- **Orbit Blue** (orbit-blue): orbit lines at rest, open and disabled outlines, outlined button and field borders.
- **Starlight** (starlight): primary text. **Dim Steel** (dim-steel): secondary text, metadata, labels, unselected nav.

### Diary papers and inks
The diary page keeps the owner's own materials, separate from the app palette. Papers (three replaceable slots, each page keeps the paper it was written on): Matte Black (paper-matte-black), Cream (paper-cream), Night (paper-night). Inks with the owner's roles: Ice Blue (ink-ice-blue) for text, Matrix Green (ink-matrix-green) for headers, Electric Cyan (ink-electric-cyan) for vectors and equations, Neon Fuchsia (ink-neon-fuchsia) for annotations, Matte Black (ink-matte-black) for cream paper. The printed template follows the paper: on dark paper the day name and rule are Matrix Green and the date Ice Blue; on light paper both are Matte Black.

### Named Rules
**The One Signal Rule.** Turquoise appears only where something can be pressed, is done, or marks where the owner is (plus the robot's eyes). Selection that is not a place is steel.

**The One Meaning Rule.** Gold is nuts, coral is failed. Neither is used to decorate, warn generally, or add variety.

## Typography

**Display Font:** Michroma (bundled, with sans-serif fallback)
**Body Font:** Jost, variable, weights 400/500/600 (bundled, with sans-serif fallback)
**Label Font:** Michroma, tracked uppercase

**Character:** Michroma's extended Microgramma-era capitals give the Expo-58 instrument voice; Jost, a Futura revival, does the reading and the counting.

### Hierarchy
- **Display** (Michroma 400, 28sp, 36sp): screen titles in the header; on a phone, the name of the next habit to log on Today.
- **Headline** (Michroma 400, 20sp, 28sp): the empty-state line ("Nothing in orbit yet").
- **Title Large** (Jost 500, 22sp, 28sp): a shop part's name in its detail, the selected day's name on Habits.
- **Title** (Jost 500, 19sp, 26sp): habit names in rows, the month and day navigator.
- **Body Large** (Jost 400, 17sp, 24sp): header subtitle (the date), notes, part names in the grid.
- **Body** (Jost 400, 15sp, 21sp): habit metadata joined by " · ", summaries, helper text.
- **Label Large** (Jost 600, 16sp, 20sp): button text, shop tabs, calendar day numbers.
- **Label Medium** (Jost 500, 13sp, 16sp): weekday names under mini-atoms, attribute glosses.
- **Label** (Michroma 400, 11sp, 0.14em tracking, uppercase, dim steel): section labels ("ATTRIBUTES", "THIS WEEK"), the day's status line ("TODAY · 3 OF 5 LIT · 1 FAILED"), calendar weekday heads, part catalogue numbers, the robot's nameplate (at body-large size in steel).
- **Numeral** (Jost 500, 18sp, tabular figures): wallet balances, attribute values and percentages, the target stepper. At small sizes it drops to 15sp.

### Named Rules
**The Jost Counts Rule.** Every count and balance is set in Jost Medium with tabular figures, never Michroma: Michroma's squared zero reads as a missing glyph when it stands alone. Michroma keeps display titles and tracked labels.

**The Wide Face Rule.** Michroma is wide, so its display sizes stay modest (28sp at most in the app chrome) and its labels carry tracking, never weight.

## Layout

Two window classes drive everything. A window is **wide** when it is landscape and at least 600dp wide: the Material navigation rail sits at the left and screens split into two panes. Otherwise the Material navigation bar sits at the foot and screens stack in one scroll. A window whose short side is under 600dp is a **phone**.

- **Wide Today and Shop:** header across the top with 28dp side gutters; the bay takes 46% at left, the list or catalogue 54% at right (8dp start, 32dp end padding).
- **Wide Habits and Diary:** a smaller bay or summary column at left, content at right (70%).
- **Stacked:** 20dp side gutters; the bay sits under the header (560dp tall on a tablet in portrait, 360dp on a phone; 300dp on Shop).
- **Forms and Settings:** one column capped at 720dp, 40dp gutters wide, 20dp stacked.
- **Rows:** 12dp vertical padding, a hairline under each. A habit row whose column is under 560dp puts its buttons on a second line with Done filling the width.
- **Phone Today:** the next habit still to log leads the list at display size with a 64dp Done; the rest follow at normal size.
- **Section rhythm:** section labels sit 28dp below what precedes them and 10dp above what they label. Gaps between controls are 8dp or 12dp.
- **Shop grid:** as many columns as fit at 156dp each (at least two), 12dp gaps.
- **Calendars:** a hairline grid: each week a ruled row, each day a cell at least 64dp tall with its number above and its mark (mini-atom or diary glyph) beneath.

### Named Rules
**The No Card Rule.** Sections are never boxed. Hierarchy is type scale, tracked labels and hairlines; the only filled surfaces are controls, shop part panels, menus and the bay.

## Elevation & Depth

Flat. Depth is tonal: rail indigo sits below the night-indigo ground, raised indigo lifts controls and panels, and the bay floor grades from its own indigo into the ground with a faint steel skirting line. The robot's shell carries the only gradient on a figure (white to pale blue steel). Material's own FAB and snackbar keep their default elevation; nothing else casts a shadow.

### Named Rules
**The Tonal Step Rule.** To lift a surface, move it one indigo step (rail, ground, raised). Never add a shadow or a glow.

## Shapes

Round and orbital against straight hairlines. Every pressable action is a pill or a circle; panels and tappable cells have gently rounded corners (12dp for part panels and week days, 10dp for calendar cells, 16dp for menus, 6dp for paper swatches). Progress is a pill track: the attribute gauge is 8dp tall, a habit's run bar 14dp with best run in steel and the current run in turquoise drawn exactly its days long. Orbits are ellipses at about 0.36 to 0.37 aspect, tilted; future days and the waiting orbit are dashed.

## Components

### Buttons
Confident on the primary, quiet on the rest.
- **Shape:** full pill (rounded.pill), 48dp tall; the phone's lead habit uses 64dp.
- **Done / primary:** turquoise with on-turquoise text in Label Large. Disabled when the period is full: raised indigo with dim text. Bad habits relabel it Resisted / Skip / Gave in.
- **Skip:** a 48dp outlined circle, 1dp orbit-blue border, a steel minus.
- **Fail:** a 48dp outlined circle, coral cross, border coral at 45%; both fall to orbit blue when disabled.
- **Outlined:** 1dp orbit-blue border, starlight text (Cancel, Take off, date pickers).
- **Text actions:** turquoise for confirm, coral for destructive, starlight for dismiss.
- **FAB:** extended, turquoise with on-turquoise content, bottom-end with 24dp inset (Add habit).

### Chips
- **Style:** Material filter chips, 1dp orbit-blue border, starlight label.
- **State:** selected fills polished steel with night-indigo text. Starter-habit suggestion chips are 40dp tall with an orbit-blue border.

### Cards / Containers
- **Shop part panel:** one panel per part, raised indigo, 12dp corners, square art; the selection ring (2dp polished steel) sits on the panel itself. Name, catalogue number and price or status sit beneath it, unboxed.
- **Menus:** raised indigo, 16dp corners, 16dp padding, 300dp wide.
- **Shadow Strategy:** none (see Elevation & Depth).

### Inputs / Fields
- **Style:** Material outlined text fields, orbit-blue border at rest.
- **Focus:** border, label and cursor turn turquoise.
- **Error:** coral body text below the form.

### Navigation
- **Rail (wide):** rail indigo; the atom mark (three steel orbits, turquoise nucleus, 40dp) at the head when the window is at least 560dp tall; Today, Shop, Habits, Diary; Settings alone at the foot.
- **Bar (stacked):** the same four tabs on rail indigo; Settings moves to an icon in the header.
- **States:** selected item gets a raised-indigo indicator, turquoise icon and starlight label; unselected are dim steel.
- **Segmented views (Day / Calendar / History):** active segment polished steel with night-indigo text, orbit-blue borders.
- **Header:** at least 72dp; Michroma title, dim date beside it, the wallet at right (bolt icon and count in steel, nut in gold, 22dp icons, Numeral). Screens without a bay put a 40x52dp robot badge before the title; on a phone the badge replaces the wallet.
- **Snackbar:** polished steel with night-indigo text and a deep-turquoise Undo, for every log and purchase.

### Electron marks (signature)
Each slot state has its own form; colour only confirms it.
- **Lit (done):** a turquoise disc with a 45% turquoise halo ring.
- **Skipped:** a dashed steel ring crossed by a horizontal bar.
- **Failed:** a coral ring struck through on the diagonal.
- **Open:** a plain dim ring.
- A slot logged on another day of the week shows at 45%. Row marks are 26dp, 4dp apart; tapping an own mark removes the newest mark of the day.
- **Lighting:** the new electron runs home along its orbit over 900ms (cubic ease-out from the far side), then throws an eight-ray turquoise starburst for 500ms while the robot cheers.

### The bay and mini-atoms (signature)
- **Bay:** the base scene, the robot on its round platform with a dashed turquoise ring, and one tilted orbit per habit running today (up to six) drifting a full turn every 90 seconds. An orbit is orbit blue at rest, turquoise when all its slots are done, coral if any failed. The nameplate (robot's name in tracked Michroma, mood in dim Jost) sits in its corner. With no habits, one dashed orbit-blue orbit waits around the robot, its dashes slowly marching.
- **Mini-atom:** a day as one tilted ellipse whose colour says how it went (coral any fail, turquoise any done, orbit blue otherwise) and a turquoise nucleus that grows with the share done; future days are dashed. 42dp in the week strip, 26dp in the calendar.

### Icons
One robot grammar: 24dp line art at 1.8 stroke, square caps, mitred joins, angular strokes and a small pivot-joint circle wherever a part would hinge. Tinted by context (steel, dim, turquoise, gold, coral).

### Diary page
A true diary template on the owner's paper: the weekday large in Michroma with the date beneath, a 2.5-unit header rule, evenly ruled lines (no grid), a five-ring CHARGE field and DAY count at the top right beside a small atom, a large faint atom near the foot, and the log's owner and catalogue number in tracked Michroma at the bottom. The toolbar is one rail-indigo line: 48dp tool tiles with 12dp corners, the active tool raised with a 16x2dp turquoise underline; inks and papers as 44dp circular and rounded swatches ringed 2dp starlight when chosen.

### Motion
One frame clock drives every animation and freezes when the system's animations are off. The robot idles, cheers on Done, droops on Fail and waves when its nameplate is tapped.

## Do's and Don'ts

### Do:
- **Do** give each habit row exactly one turquoise Done pill (48dp, full pill) and keep Skip and Fail as 48dp outlined circles beside it.
- **Do** show every slot state with its own electron form (lit, hollow with bar, struck) so state never depends on colour alone.
- **Do** set counts and balances in Jost Medium with tabular figures (Numeral); keep Michroma for display titles and tracked uppercase labels.
- **Do** separate rows and calendar weeks with one hairline (hairline) and announce sections with an 11sp Michroma label tracked 0.14em.
- **Do** lift surfaces by one indigo step (rail, ground, raised) instead of shadows.
- **Do** show selection in polished steel and offer Undo in the snackbar after every log and purchase.
- **Do** keep the diary page on the owner's papers and inks, ruled lines only.

### Don't:
- **Don't** use turquoise for decoration, emphasis or non-place selection; it means pressable, done, or here.
- **Don't** use coral for anything but failure and destruction, or gold for anything but nuts.
- **Don't** set a lone number in Michroma.
- **Don't** wrap sections in cards, or build the dark-dashboard default of neon-glow stat cards and progress rings.
- **Don't** add ornaments beyond orbits and electrons.
- **Don't** put squared or grid paper anywhere; calendars use ruled rows, the diary ruled lines.
- **Don't** draw icons outside the robot grammar (24dp, 1.8 stroke, square caps, pivot joints) or substitute glyphs and emoji for them.
