---
name: Nuts And Bolts
description: A habit tracker and diary where a small robot tends a seedling for every habit in a night grow room.
colors:
  greenhouse-teal: "#0B1A1A"
  rail-teal: "#081313"
  raised-teal: "#15302B"
  hairline: "#1B3632"
  edge-teal: "#24443E"
  bay-sky: "#0F2424"
  bay-floor: "#0E2020"
  frost: "#E6F2EE"
  dim-sage: "#8FB0A8"
  mint-steel: "#CFE3DE"
  joint-sage: "#6C8F87"
  leaf-green: "#6EE787"
  on-leaf: "#06281A"
  grow-magenta: "#E05AD0"
  nut-gold: "#F2C14E"
  wilt-coral: "#FF6B4A"
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
    backgroundColor: "{colors.leaf-green}"
    textColor: "{colors.on-leaf}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
    height: "48dp"
  button-done-disabled:
    backgroundColor: "{colors.raised-teal}"
    textColor: "{colors.dim-sage}"
    rounded: "{rounded.pill}"
    height: "48dp"
  button-done-lead:
    backgroundColor: "{colors.leaf-green}"
    textColor: "{colors.on-leaf}"
    rounded: "{rounded.pill}"
    height: "64dp"
  icon-button-skip:
    textColor: "{colors.mint-steel}"
    rounded: "{rounded.pill}"
    size: "48dp"
  icon-button-fail:
    textColor: "{colors.wilt-coral}"
    rounded: "{rounded.pill}"
    size: "48dp"
  button-outlined:
    textColor: "{colors.frost}"
    rounded: "{rounded.pill}"
    height: "48dp"
  fab:
    backgroundColor: "{colors.leaf-green}"
    textColor: "{colors.on-leaf}"
  nav-container:
    backgroundColor: "{colors.rail-teal}"
    textColor: "{colors.dim-sage}"
  nav-item-selected:
    backgroundColor: "{colors.raised-teal}"
    textColor: "{colors.frost}"
  segment-active:
    backgroundColor: "{colors.raised-teal}"
    textColor: "{colors.frost}"
  chip-selected:
    backgroundColor: "{colors.raised-teal}"
    textColor: "{colors.frost}"
  part-tile:
    backgroundColor: "{colors.raised-teal}"
    rounded: "{rounded.tile}"
  bay-panel:
    backgroundColor: "{colors.bay-sky}"
    rounded: "{rounded.bay}"
    padding: "20dp"
  snackbar:
    backgroundColor: "{colors.raised-teal}"
    textColor: "{colors.frost}"
  snackbar-action:
    textColor: "{colors.leaf-green}"
  menu:
    backgroundColor: "{colors.raised-teal}"
    rounded: "{rounded.menu}"
    padding: "16dp"
---

# Design System: Nuts And Bolts

## Overview

**Creative North Star: "Night Grow Room"**

A hydroponic bay after dark. The app is a deep greenhouse-teal room in which the robot stands on its round platform beside a glass tray under a hanging magenta grow light. Every habit running today is a sprout in that tray and every slot is a leaf on it: Done unfurls a leaf, Skip leaves a dashed outline, Fail wilts a coral leaf, and an open slot is still a bud. The week is seven small sprouts that stand taller and carry more leaves the better the day went. The grow light's magenta belongs to the bay alone; the interface itself stays teal, mint steel and frost, with leaf green as the one live colour.

Hierarchy comes from type scale and hairlines, not boxes. Rows are separated by one-pixel teal rules, sections are announced by small tracked caps, and the only filled surfaces are controls, shop part tiles and the framed bay. The leaf is the single ornament and always carries meaning: the app mark (a sprout with two leaves in a tray), the slot, the day, the five buds of the diary page's gauge and the leaf emblem printed on every page.

Density is calm and evening-friendly: 48dp targets, 28dp gutters on wide windows, one clear Done per row with Skip and Fail quieter beside it. One rounded, open face (Lexend) carries everything.

**Key Characteristics:**
- Greenhouse-teal ground with a deeper rail; tonal steps, no shadows.
- One live accent (leaf green); gold for nuts, coral for failure, mint steel for selection and metal.
- Magenta only as the grow light inside the bay.
- Lexend throughout: bold display, semibold titles, tracked small caps for labels.
- Leaves and sprouts as the one ornament and the one state language.
- Hairline rows, no cards around sections.
- Robot-grammar line icons: 24dp, 1.8 stroke, square caps, pivot-joint circles.

## Colors

A dark greenhouse lit by one magenta grow lamp, where green means grown or pressable and every other colour holds a single job.

### Primary
- **Leaf Green** (leaf-green): the Done pill, primary buttons and the FAB, unfurled leaves and live stems, and the place markers (selected rail and bar icon, the shop tab indicator, today's ring on the calendar, the active diary tool's underline, the diary calendar's page mark). Also field focus, the reminder switch track, confirm text actions and the snackbar's Undo. In the robot it lights the eyes, the smile and the dashed platform ring. Text on it is **On-Leaf** (on-leaf), a near-black green.

### Secondary
- **Mint Steel** (mint-steel): bolts and their counts, attribute icons and gauge fills, the tray's glass rim, and every selected state that is not a place: the active segment's border, selected chips' border, the selection ring on a shop part, the robot's nameplate.
- **Joint Sage** (joint-sage): the robot's joints and claws, the grow light's hanging wires, and the stem of a sprout whose habit failed today.

### Tertiary
- **Grow Magenta** (grow-magenta): the grow light only: the lamp bar (solid, with a 30% halo), its radial glow washing the bay at 22%, the faint light cone, and the base's orbiting rings at higher tiers. Never in the interface chrome.
- **Nut Gold** (nut-gold): nuts, their counts, and gold parts of the robot's world. Never a general highlight.
- **Wilt Coral** (wilt-coral): wilted leaves on rows, the tray and the week; the Fail button's cross and its 45% border; destructive text actions; form errors.

### Neutral
- **Greenhouse Teal** (greenhouse-teal): the ground of every screen.
- **Rail Teal** (rail-teal): the navigation rail, the bottom bar, the diary toolbar, the robot's face glass and the bay floor's lower edge; a step deeper than the ground.
- **Raised Teal** (raised-teal): controls at rest, the selected nav indicator, active segments and selected chips, disabled Done, shop part tiles, menus, the snackbar.
- **Bay Sky** (bay-sky) to **Bay Floor** (bay-floor): the bay's vertical gradient (sky into ground) and its floor band (floor into rail), with a 10% mint-steel skirting line where they meet.
- **Hairline** (hairline): row dividers, calendar week rules, empty gauge and run tracks.
- **Edge Teal** (edge-teal): outlines at rest (outlined buttons, fields, chips, segments, Skip), disabled glyphs, future-day seeds.
- **Frost** (frost): primary text. **Dim Sage** (dim-sage): secondary text, metadata, labels, unselected nav, buds and skipped leaves.

### Diary papers and inks
The diary page keeps the owner's own materials, separate from the app palette (owner-pinned). Papers (three replaceable slots, each page keeps the paper it was written on): Matte Black (paper-matte-black), Cream (paper-cream), Night (paper-night). Inks with the owner's roles: Ice Blue (ink-ice-blue) for text, Matrix Green (ink-matrix-green) for headers, Electric Cyan (ink-electric-cyan) for vectors and equations, Neon Fuchsia (ink-neon-fuchsia) for annotations, Matte Black (ink-matte-black) for cream paper. The printed template follows the paper: on dark paper the weekday, header rule and small leaf emblem are Matrix Green, the date and field labels Ice Blue at 72%, the rules white at 15% and the large foot emblem Electric Cyan at 20%; on light paper all of it is Matte Black at matching strengths.

### Named Rules
**The Grown Or Pressable Rule.** Leaf green appears only where something can be pressed, has grown, or marks where the owner is (plus the robot's eyes, smile and platform ring). Selection that is not a place is mint steel.

**The Lamp Stays In The Bay Rule.** Magenta is light, not paint: it lives inside the bay as the grow lamp and its glow, and never colours a control, a label or a mark.

**The One Meaning Rule.** Gold is nuts, coral is failed or destructive. Neither decorates, warns generally, or adds variety.

## Typography

**Display Font:** Lexend, variable, weights 600/700 (bundled, with sans-serif fallback)
**Body Font:** Lexend, variable, weights 400/500/600
**Label Font:** Lexend Medium, tracked uppercase

**Character:** One rounded, open, highly legible face throughout; weight and tracking do the work a second family would.

### Hierarchy
- **Display** (700, 32sp, 40sp, -0.01em): screen titles in the header; on a phone, the name of the next habit to log on Today.
- **Headline** (600, 22sp, 28sp): the empty-state line ("Nothing planted yet").
- **Title Large** (600, 21sp, 28sp): a shop part's name in its detail, the selected day's name on Habits.
- **Title** (600, 18sp, 24sp): habit names in rows and history, the month and day stepper.
- **Body Large** (400, 16sp, 24sp): header subtitle (the date), notes, part names in the grid, attribute names (at 500).
- **Body** (400, 14sp, 20sp): habit metadata joined by " · ", summaries, helper text.
- **Label Large** (600, 16sp, 20sp): button text, shop tabs, calendar day numbers.
- **Label Medium** (500, 13sp, 16sp): weekday names under the week's sprouts, attribute glosses.
- **Label** (500, 13sp, 0.14em tracking, uppercase, dim sage): section labels ("ATTRIBUTES", "THIS WEEK"), the day's status line ("TODAY · 4 OF 9 LEAVES UNFURLED · 1 FAILED"), form field labels, calendar weekday heads, part catalogue numbers, the robot's nameplate (at body-large size in mint steel).
- **Numeral** (600, 18sp, tabular figures): wallet balances, attribute values and percentages, the target stepper, the reminder time. At small sizes it drops to 14sp.

### Named Rules
**The One Face Rule.** Lexend sets everything, including the printed diary template. Contrast comes from weight (400 to 700), size and tracked caps, never from a second family.

**The Tabular Count Rule.** Every count and balance uses Numeral with tabular figures so columns of numbers hold still as they change.

## Layout

Two window classes drive everything. A window is **wide** when it is landscape and at least 600dp wide: the Material navigation rail sits at the left and screens split into two panes. Otherwise the Material navigation bar sits at the foot and screens stack in one scroll. A window whose short side is under 600dp is a **phone**.

- **Wide Today and Shop:** header across the top with 28dp side gutters; the bay takes 46% at left, the list or catalogue 54% at right (8dp start, 32dp end padding). Shop's part detail sits under its bay.
- **Wide Habits and Diary:** a 30% column with the day or month summary over a smaller bay, content at right (70%).
- **Stacked:** 20dp side gutters; the bay sits under the header (560dp tall on a tablet in portrait, 360dp on a phone; 300dp on Shop). Habits, Diary and the form show a robot badge instead of a bay.
- **Forms and Settings:** one column capped at 720dp, 40dp gutters wide, 20dp stacked.
- **Rows:** 12dp vertical padding, a hairline under each. A habit row whose column is under 560dp puts its buttons on a second line with Done filling the width.
- **Phone Today:** the next habit still to log leads the list at display size with a 64dp Done; the rest follow at normal size.
- **Section rhythm:** section labels sit 28dp below what precedes them and 10dp above what they label (22dp and 8dp in the form). Gaps between controls are 8dp or 12dp.
- **Shop grid:** as many columns as fit at 156dp each (at least two), 12dp gaps.
- **Calendars:** a hairline grid: each week a ruled row, each day a cell at least 64dp tall with its number above and its mark (a day sprout, or the pen icon for a diary page) beneath.

### Named Rules
**The No Card Rule.** Sections are never boxed. Hierarchy is type scale, tracked labels and hairlines; the only filled surfaces are controls, shop part tiles, menus and the bay.

## Elevation & Depth

Flat. Depth is tonal: rail teal sits below the greenhouse-teal ground and raised teal lifts controls and tiles. The one atmospheric effect is light, inside the bay: a vertical sky gradient, the floor band graded into the rail, and the grow lamp's magenta radial glow. The robot's shell carries the only gradient on a figure (white to pale mint). Material's own FAB and snackbar keep their default elevation; nothing else casts a shadow.

### Named Rules
**The Tonal Step Rule.** To lift a surface, move it one teal step (rail, ground, raised). Never add a shadow; the only glow in the app is the grow lamp's.

## Shapes

Soft and organic against straight hairlines. Every pressable action is a pill or a circle; the bay is framed with 20dp corners; part tiles, week days and diary tools have 12dp corners, calendar cells 10dp, menus 16dp, paper swatches 6dp. Progress is a pill track: the attribute gauge is 8dp tall, a habit's run bar 14dp with the best run in mint steel at 55% and the current run in leaf green, drawn exactly its days long. The leaf family is drawn from three fixed silhouettes anchored on the stem: an unfurled leaf, a slim closed bud, and a short drooping wilt.

## Components

### Buttons
Confident on the primary, quiet on the rest.
- **Shape:** full pill (rounded.pill), 48dp tall; the phone's lead habit uses 64dp.
- **Done / primary:** leaf green with on-leaf text in Label Large. Disabled when the period is full: raised teal with dim text. Bad habits relabel the trio Resisted / Skip / Gave in.
- **Skip:** a 48dp outlined circle, 1dp edge-teal border, a mint-steel minus.
- **Fail:** a 48dp outlined circle, coral cross, border coral at 45%; both fall to edge teal when disabled.
- **Outlined:** 1dp edge-teal border, frost text (Cancel, Take off, backup, date pickers).
- **Text actions:** leaf green for confirm, coral for destructive, frost for dismiss.
- **FAB:** extended, leaf green with on-leaf content, bottom-end with a 24dp inset (Add habit).

### Chips
- **Style:** Material filter chips, 1dp edge-teal border, frost label.
- **State:** selected fills raised teal with a 1.5dp mint-steel border. Starter-habit suggestion chips are 40dp tall with an edge-teal border.

### Cards / Containers
- **Bay panel:** the bay clipped to 20dp corners inside 12dp of padding, with the robot's nameplate (name in tracked caps, mood and "tending N sprouts" in dim body) 20dp inside its top corner on Today.
- **Shop part tile:** one raised-teal tile per part, 12dp corners, square art; the selection ring (2dp mint steel) sits on the tile itself. Name, catalogue number and price or status sit beneath it, unboxed.
- **Menus:** raised teal, 16dp corners, 16dp padding, 300dp wide.
- **Shadow Strategy:** none (see Elevation & Depth).

### Inputs / Fields
- **Style:** Material outlined text fields, edge-teal border at rest.
- **Focus:** border, label and cursor turn leaf green.
- **Error:** coral body text below the form.

### Navigation
- **Rail (wide):** rail teal; the sprout mark (a mint-steel stem with two green leaves over a small steel tray, 40dp) at the head when the window is at least 560dp tall; Today, Shop, Habits, Diary; Settings alone at the foot.
- **Bar (stacked):** the same four tabs on rail teal; Settings moves to an icon in the header.
- **States:** selected item gets a raised-teal indicator, leaf-green icon and frost label; unselected are dim sage.
- **Segmented views (Day / Calendar / History):** active segment raised teal with frost text and a mint-steel border; inactive on the ground with edge-teal borders.
- **Header:** at least 72dp; display title, dim date beside it, the wallet at right (bolt icon and count in mint steel, nut in gold, 22dp icons, Numeral). Screens without a bay put a 40x52dp robot badge before the title; on a phone the badge replaces the wallet.
- **Snackbar:** raised teal with frost text and a leaf-green Undo, for every log and purchase.

### Leaf slots (signature)
Each slot state has its own form; colour only confirms it.
- **Done:** an unfurled leaf, solid leaf green.
- **Skipped:** the same leaf outlined in dim sage, dashed.
- **Failed:** a short drooping wilt, solid coral.
- **Open:** a slim bud outlined in dim sage.
- A slot logged on another day of the week shows at 45%. Row marks are 26dp, 4dp apart; tapping an own mark removes the newest mark of the day.

### The grow tray and day sprouts (signature)
- **Bay:** the robot on its round platform (dashed green ring) steps left; at right, two joint-sage wires hang the magenta lamp bar over a glass tray (water fill, 70% mint-steel rim). One sprout per habit running today, up to six, spaced evenly: a green stem (joint sage if the habit failed today) whose height grows with its target, carrying one leaf per slot, alternating sides. With no habits the tray holds one dashed seed.
- **Unfurl:** Done grows the newest leaf on that habit's sprout from nothing over 700ms (cubic ease-out) while the robot cheers.
- **Day sprout:** a day as a small sprout that stands taller with the share done and carries one, two or three leaves (any done, half, all); a wilted coral leaf at its foot when anything failed; a dashed seed while the day is ahead, a plain seed when nothing ran. 42dp in the week strip, 26dp in the calendar.

### Icons
One robot grammar: 24dp line art at 1.8 stroke, square caps, mitred joins, angular strokes and a small pivot-joint circle wherever a part would hinge. Tinted by context (mint steel, dim, leaf green, gold, coral).

### Diary page
A true diary template on the owner's paper: the weekday large in Lexend with the date beneath, a 2.5-unit header rule, 37 evenly ruled lines (no grid), a LEAVES field of five buds to fill in by hand and a DAY count at the top right beside a small leaf emblem, a large faint leaf near the foot, and the log's owner and catalogue number in tracked caps at the bottom. The toolbar is one rail-teal line: 48dp tool tiles with 12dp corners, the tool in hand raised with a 16x2dp leaf-green underline; inks and papers as 44dp targets with circular and 6dp-rounded swatches, ringed 2dp frost when chosen.

### Motion
One frame clock drives every animation and freezes when the system's animations are off. The robot idles, cheers on Done, droops on Fail and waves when its nameplate is tapped; leaves unfurl as above.

## Do's and Don'ts

### Do:
- **Do** give each habit row exactly one leaf-green Done pill (48dp, full pill) and keep Skip and Fail as 48dp outlined circles beside it.
- **Do** show every slot state with its own leaf form (unfurled, dashed, wilted, bud) so state never depends on colour alone.
- **Do** set every screen in Lexend, counts and balances in Numeral with tabular figures.
- **Do** separate rows and calendar weeks with one hairline (hairline) and announce sections with a 13sp label tracked 0.14em.
- **Do** lift surfaces by one teal step (rail, ground, raised) instead of shadows.
- **Do** keep magenta inside the bay as the grow lamp and its glow.
- **Do** show selection in mint steel and offer Undo in the snackbar after every log and purchase.
- **Do** keep the diary page on the owner's papers and inks, ruled lines only.

### Don't:
- **Don't** use leaf green for decoration, emphasis or non-place selection; it means pressable, grown, or here.
- **Don't** put magenta on a control, label, mark or text.
- **Don't** use coral for anything but failure and destruction, or gold for anything but nuts.
- **Don't** wrap sections in cards, or build the dark-dashboard default of glowing stat cards and progress rings.
- **Don't** add ornaments beyond leaves and sprouts.
- **Don't** put squared or grid paper anywhere; calendars use ruled rows, the diary ruled lines.
- **Don't** draw icons outside the robot grammar (24dp, 1.8 stroke, square caps, pivot joints) or substitute glyphs and emoji for them.
