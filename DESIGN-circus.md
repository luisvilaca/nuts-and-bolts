---
name: Five Nights at the Circus
description: The circus flavour of Nuts And Bolts. Each habit is a marquee sign over a circus ring after close, and every Done lights a bulb while an animatronic performs in the one spotlight.
colors:
  ground: "#120A14"
  rail: "#0B060D"
  card: "#1D1020"
  raised: "#2E1630"
  border: "rgba(255,255,255,0.07)"
  line: "#2B1628"
  edge: "#4A2541"
  canvas-cream: "#F3E6D3"
  dim: "#BBA4B0"
  faded-canvas: "#E8D5C4"
  joint: "#7A5A6E"
  selection: "rgba(232,213,196,0.55)"
  selection-fill: "rgba(232,213,196,0.06)"
  bulb-amber: "#FFC94A"
  on-bulb: "#2A1404"
  bulb-glow: "#FFB938"
  filament: "#FFE6A8"
  faz-token-pink: "#FF7AAE"
  smashed-coral: "#FF5C5C"
  tent-red-drum: "#7A1426"
  tent-red-stripe: "#8A1A2E"
  tent-cream-stripe: "#D9C7B0"
  brass-trim: "#F2C14E"
  sign-board: "#2A1426"
  riser-plum: "#3A1F33"
typography:
  display:
    fontFamily: "Rye, Kelly Slab (Russian), serif"
    fontSize: "28sp"
    fontWeight: 700
    lineHeight: "34sp"
    letterSpacing: "-0.01em"
  headline:
    fontFamily: "Rye, Kelly Slab (Russian), serif"
    fontSize: "22sp"
    fontWeight: 600
    lineHeight: "28sp"
  title-large:
    fontFamily: "Manrope, sans-serif"
    fontSize: "21sp"
    fontWeight: 600
    lineHeight: "28sp"
  title:
    fontFamily: "Manrope, sans-serif"
    fontSize: "18sp"
    fontWeight: 600
    lineHeight: "24sp"
  body-large:
    fontFamily: "Manrope, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
  body:
    fontFamily: "Manrope, sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
  label-large:
    fontFamily: "Manrope, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "20sp"
  label-medium:
    fontFamily: "Manrope, sans-serif"
    fontSize: "13sp"
    fontWeight: 500
    lineHeight: "16sp"
  label:
    fontFamily: "Manrope, sans-serif"
    fontSize: "13sp"
    fontWeight: 500
    letterSpacing: "0.14em"
  numeral:
    fontFamily: "Rye, Kelly Slab (Russian), serif"
    fontSize: "18sp"
    fontWeight: 600
    fontFeature: "tnum"
rounded:
  field: "10dp"
  card: "12dp"
  menu: "12dp"
  segment: "22dp"
  pill: "9999px"
spacing:
  inline: "6dp"
  card-gap: "12dp"
  card-inside: "16dp"
  gutter: "28dp"
  control: "44dp"
components:
  button-done:
    backgroundColor: "{colors.bulb-amber}"
    textColor: "{colors.on-bulb}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
    height: "44dp"
  button-done-disabled:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.dim}"
  button-menu:
    backgroundColor: "transparent"
    textColor: "{colors.faded-canvas}"
    rounded: "{rounded.pill}"
    size: "44dp"
  button-outlined:
    backgroundColor: "transparent"
    textColor: "{colors.canvas-cream}"
    rounded: "{rounded.pill}"
    height: "44dp"
  button-text:
    backgroundColor: "transparent"
    textColor: "{colors.bulb-amber}"
    typography: "{typography.label-large}"
  chip-finished:
    backgroundColor: "transparent"
    textColor: "{colors.faded-canvas}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pill}"
    height: "44dp"
  card:
    backgroundColor: "{colors.card}"
    textColor: "{colors.canvas-cream}"
    rounded: "{rounded.card}"
    padding: "16dp"
  card-selected:
    backgroundColor: "{colors.selection-fill}"
    textColor: "{colors.canvas-cream}"
    rounded: "{rounded.card}"
    padding: "16dp"
  field:
    backgroundColor: "rgba(255,255,255,0.05)"
    textColor: "{colors.canvas-cream}"
    rounded: "{rounded.field}"
    padding: "12dp 16dp"
  segment-active:
    backgroundColor: "{colors.selection-fill}"
    textColor: "{colors.canvas-cream}"
    typography: "{typography.label-large}"
    rounded: "{rounded.segment}"
    height: "44dp"
  nav-container:
    backgroundColor: "{colors.rail}"
    textColor: "{colors.dim}"
    typography: "{typography.label-medium}"
  nav-item-active:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.bulb-amber}"
    rounded: "{rounded.pill}"
    width: "64dp"
    height: "32dp"
  menu:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.canvas-cream}"
    rounded: "{rounded.menu}"
  snackbar:
    backgroundColor: "{colors.raised}"
    textColor: "{colors.canvas-cream}"
---

# Design System: Five Nights at the Circus

<!-- Scope: the circus product flavour only (app/src/circus). Screens, layout and behaviour are shared with the workshop flavour and documented in DESIGN.md; this file records what the circus skin changes. Diary papers and inks are owner-pinned and shared: see DESIGN.md. -->

## Overview

**Creative North Star: "Midnight Big Top"**

A circus tent after close. The show is long over and the animatronics are still awake. The ground is plum-black canvas, and the only light in the room is bulb amber: the bulbs on each habit's sign, the one spotlight on the ring box, the gauges, and the Done button. Everything else stays in the dark. Each habit is a marquee sign. Done lights a bulb that buzzes on, flickering twice before it holds. When every bulb on a sign is lit, its border lights chase and the animatronic's eyes flare.

The world is drawn, not photographed. Every figure, room and prop is vector art painted from Inks.kt roles over a 400 x 520 robot space and a 1000 x 900 ring scene. The art is inspired by the classic and withered animatronics and Sister Location's circus, but no game assets are copied. Tent red and cream are materials (the drum stripes, the riser skirt, the valance and the calliope). They never act as signals. The interface around the ring is quiet: plum cards, cream text, one amber action per card.

Density and structure are the same as the workshop flavour, with the same screens, breakpoints and rhythm. The circus changes only colour, faces, icons, art, words and sound.

**Key Characteristics:**
- Plum-black tonal ladder (rail < ground < card < raised) with cream text.
- Bulb amber is the single light and the single action colour.
- Habit slots are bulbs, and each state has its own form: lit, dark, unscrewed, smashed.
- Rye poster wood type for titles and numerals, with Manrope for everything read.
- One spotlight. Outside its cone the ring falls to near-black.
- A Fail flips the ring to its negative for one frame, under camera static.

## Colors

A plum-black canvas lit by warm amber bulbs. Pink marks tokens, coral marks breakage, and tent red stays in the material.

### Primary
- **Bulb Amber** (bulb-amber): a lit bulb as an action. Used for the Done, Save and Buy fill, text actions, the field's focus outline and cursor, the active tab icon, the settings icon while Settings is open, and today's ring on the day marks.
- **On Bulb** (on-bulb): the scorched brown-black used for text on an amber fill.
- **Bulb Glow** (bulb-glow): every halo. Used for a lit bulb's radial bloom, the gauges' fill and haze, a finished card's lit edge, a fully lit sign's chase and bloom, the tracker wire once every bulb is lit, and the calliope's pipes and lamps.
- **Filament** (filament): the white-hot body of a lit bulb, and the lit bulbs on the marquee swag and calliope lamps.

### Secondary
- **Faded Canvas** (faded-canvas): metal and selection. Used for a selected card's or segment's edge, the finished "All in" chip and its check, the ticket balance, the menu button's dots, and the cool end of a gauge's gradient.
- **Joint** (joint): the wire the marquee bulbs hang on, a failed sign's border, the day plate's rim.

### Tertiary
- **Faz-Token Pink** (faz-token-pink): Faz-Tokens only in the interface (the wallet count and its coin, the reward icon).
- **Smashed Coral** (smashed-coral): a smashed bulb's shards and core. It doubles as Material's error role and the destructive "Delete habit" text.

### Neutral
- **Ground** (ground): the screen behind everything. It is also the launcher's background colour.
- **Rail** (rail): the bottom bar and side rail, the floor gradient's foot, a smashed bulb's dark socket.
- **Card** (card): every card.
- **Raised** (raised): a control on a card. Used for the disabled button, the active tab pill, menus, the snackbar, and a dark bulb.
- **Line / Edge** (line, edge): hairlines and outlines. Edge outlines the menu button, outlined buttons, segments, the dashed future day plate and an unlit sign.
- **Border** (border): a card's 1dp rim at white 7%.
- **Canvas Cream** (canvas-cream): all primary text.
- **Dim** (dim): secondary text, inactive tabs, an unscrewed bulb's dashed socket. It reaches 7:1 on raised.

### Material (art only)
- **Tent Red Drum / Stripe, Tent Cream Stripe** (tent-red-drum, tent-red-stripe, tent-cream-stripe): the ring box drum, the riser's striped skirt, the calliope body and the launcher icon's scallop. The valance uses darker versions of the same pair (#5A1428 and #6E5A54) so it reads as grey in the dark.
- **Brass Trim** (brass-trim): the riser edge and studs, the calliope rim and crank, the show sign's frame, and foil stars.
- **Sign Board, Riser Plum** (sign-board, riser-plum): the dark boards the marquee signs are mounted on, and their poles.

### Named Rules
**The One Light Rule.** In the interface, amber is the only colour that glows or invites a press. Done is the one amber fill on a card, and every glow uses bulb-glow.

**The Tent Is Material Rule.** Tent red and cream stripe the drum, the riser, the valance and the calliope. They never mark state, never fill a control, and never colour text.

**The Pink Means Tokens Rule.** In the interface chrome, pink appears only on Faz-Tokens. Pink in the art (bows, blush, pom-poms, the Circus Gallery's neon) belongs to the costumes and rooms and is allowed.

## Typography

**Display Font:** Rye (with Kelly Slab for Russian, serif fallback)
**Body Font:** Manrope (variable, 400 / 500 / 600)
**Numerals:** Rye (Kelly Slab in Russian)

**Character:** Rye is circus-poster wood type, blunt and ornamental. It is used only for titles and numbers, where it reads as a painted sign. Manrope is a calm, open sans that carries everything meant to be read. Rye has no Cyrillic, so Russian titles and numerals are set in Kelly Slab, a slab face with the same poster bluntness.

### Hierarchy
- **Display** (Rye, 28sp / 34sp): screen titles in the header (The Ring, Prize Corner, Acts, Night Log).
- **Headline** (Rye, 22sp / 28sp): the empty-state line and the picked habit in the detail pane.
- **Title Large / Title** (Manrope 600, 21sp and 18sp): part names, habit names on cards.
- **Body Large / Body** (Manrope 400, 16sp and 14sp): notes, field text, and habit metadata ("3 a day · Power").
- **Label Large** (Manrope 600, 16sp): buttons, segments, attribute names.
- **Label Medium** (Manrope 500, 13sp): tab labels and attribute meanings.
- **Label** (Manrope 500, 13sp, 0.14em, uppercase): the animatronic's nameplate and group labels.
- **Numeral** (Rye, 18sp, tnum): wallet balances, levels ("LV 3"), and counters ("2 / 3").

### Named Rules
**The Poster Face Is For Signs Rule.** Rye sets only titles and numbers, never a sentence. Anything read in full is Manrope.

**The Cyrillic Swap Rule.** When the language is Russian, every Rye role changes to Kelly Slab. The swap is never left to a system fallback.

## Layout

Layout, breakpoints and rhythm are shared with the workshop flavour (DESIGN.md, Layout), with the same wide, phone and split thresholds, the 72dp side rail on a phone held sideways, 16dp inside cards, 10dp to 12dp between cards, and 28dp gutters. The circus changes the picture inside the stage frame. Its scene space is 1000 x 900, and x 40..960 / y 120..880 always shows, so the Ring, the Prize Corner and the stage tiles frame the same picture. On a canvas narrower than the scene, the picture crops to x 60..940. The animatronic stands on the ring box at left, shifted 140 units left of the workshop's robot. The signs stand on a riser at right (x 620..910, up to six signs), with the marquee swag over them and the calliope under them. The scalloped valance runs across the top.

## Elevation & Depth

The interface is flat. Depth comes from tone: rail, then ground, then card, then raised, each a visible step. The only depth inside the stage is light. A vertical sky-to-wall gradient, a floor graded into the rail, and a radial vignette (transparent at 0, black 45% at 0.3, 78% at 0.6, 92% at the edge) push everything except the spotlight into near-black. Under that sit one spotlight cone (light 8% to 26%) with an oval pool (42%) on the ring box. Halos are radial gradients of bulb-glow and are the only glow outside the stage. Material's FAB and snackbar keep their default elevation. Nothing else casts a shadow.

### Named Rules
**The One Spotlight Rule.** Each room has exactly one light colour (Center Ring amber, Parts & Service sick green #B8F0A0, Pirate Cove violet #B48CFF, Show Stage warm white #FFE3B0, Circus Gallery pink #FF6FA3). It feeds the spotlight and the marquee swag. Everything outside the cone stays dark.

## Shapes

Corners are soft, and all pressable controls are pills. Cards and menus use 12dp corners, fields 10dp, and segment tracks 22dp with a pill thumb. Done, outlined buttons, the finished chip and the menu button are full circles or pills. Icons are 24dp round-ended strokes in the circus's own grammar: a ticket stub (tickets), a starred coin (Faz-Tokens), a bulb, a music-box crank, a bow tie and a ringed star (attributes), and a round stage, a prize box, a marquee sign and a quill (tabs). Where the workshop draws a pivot joint, the circus draws a small ring. In the art, signs are 7-unit rounded boards, day plates are rounded marquee plates (corner 30% of their height), and bulbs are circles.

## Components

### Buttons
- **Shape:** full pill (CircleShape), 44dp high.
- **Done (primary):** an amber fill with on-bulb text in Label Large. Pressing it scales the button to 0.96 over 150ms. A long press opens the habit's menu. When disabled it fills with raised and its text turns dim.
- **Menu button:** a 44dp outlined circle (1dp edge) with faded-canvas dots. Its menu is raised with 12dp corners.
- **Outlined:** a 1dp edge outline with cream text, used for dates, the reminder time (in Numeral) and Cancel.
- **Text:** amber text for Save, OK, Edit and Back to today. Cream is used for Cancel and Keep it, and coral for Delete habit.

### Finished Chip
- **Style:** the Done button's place turns into a 1dp outlined pill. When every slot is lit it shows faded-canvas text with a check ("All in" / "Held"). A period closed by a skip or a smash shows a dim edge and the text "Closed". The change crossfades over 250ms.

### Cards / Containers
- **Corner Style:** 12dp.
- **Background:** card. A selected card adds a selection-fill wash and a 1.5dp selection edge.
- **Border:** a 1dp rim at white 7%.
- **Accent:** a finished habit's card lights its whole edge in bulb-glow, with a 1.5dp line at 90% over a 6dp haze at 25%, the way a fully lit sign glows.
- **Internal Padding:** 16dp.

### Inputs / Fields
- **Style:** a white-5% fill, a 1dp white-7% outline, 10dp corners, 12 by 16dp inside.
- **Focus:** the outline and cursor turn bulb amber.

### Segments
- A 44dp pill track with a 1dp edge outline. The selection is a pill with a selection-fill wash and a 1.5dp selection edge, and it slides on a spring (damping 0.8, stiffness 500). Labels change from dim to cream.

### Navigation
- **Bottom bar / side rail:** a rail background with four tabs: The Ring, Prize Corner, Acts, Night Log. The active tab sits in a 64 x 32dp raised pill, with an amber icon and a cream label. Inactive tabs are dim. Switching tabs plays the camera-switch click.

### Marquee Bulb (signature)
The slot mark everywhere (trackers, ring signs, day plates, the show sign). Each state has its own form, and colour only confirms it:
- **Lit (Done):** a bulb-glow halo (radius 2.1x, 55% at its centre), a filament body, and a white-hot core.
- **Dark (open):** a raised bulb with a dim rim and a faint glass highlight arc.
- **Unscrewed (Skip):** an empty dashed dim socket crossed by a diagonal.
- **Smashed (Fail):** a rail-dark socket ringed by a 14-point coral shard star, with a coral core.
- **Buzz-on:** over 700ms a fresh bulb flickers twice at partial power before it holds. While the bulb is still fresh, it keeps a pulsing flare (halo up to 3.4x) until the screen is left.
- Slots that belong to another day are drawn at 45%.

### Tracker
A habit card's progress: one bulb per slot strung on a 1.5dp edge-coloured wire. Once every bulb is lit, the wire turns bulb-glow with a soft 18% haze.

### Marquee Sign (ring scene)
A sign-board rectangle with 7-unit corners on a riser-plum pole, its bulbs stacked bottom to top. The border is 2.5 units, in edge (joint after a fail). When every bulb is lit, the border turns bulb-glow, gains a radial bloom, and 14 border lights chase in thirds every 120ms. On the sign just completed, the bloom ignites over 700ms after a 500ms beat.

### Gauge
Each attribute's level appears as a round-capped bar on a plum track. The fill runs from bulb-glow to faded-canvas, sits under two glow hazes (2.6x at 6%, 1.8x at 12%), and ends in a white-hot leading spark. The level is set in Numeral.

### Day Plate
A day on the calendar is a small marquee plate with three bulbs: one, two or three lit for any, half or all done, with a smashed third bulb when something failed. A day ahead is a dashed edge outline. Today carries a 1.5dp amber ring.

### Fail Negative-Flash
When a slot fails, the whole stage inverts (a white Difference blend) for 70ms, under 160ms of camera static (420 white specks at 25% to 75%). It never plays when animations are off.

### Sound
The circus after dark, generated by tools/sfx_circus.py (44.1kHz, original synthesis). Done is a bulb buzzing on, with one flicker, a filament tink and a calliope note. A fully lit sign plays a slightly out-of-tune calliope fanfare. Skip is a bulb squeaking out of its socket. Fail is glass smashing and a servo screech falling into static. Level up is a music box climbing, and level down is a music box winding flat. A Faz-Token is a coin and a prize bell. A prize won is the claw, a thunk and two bells. Equip is a servo turn and a jaw clack, and undo is the servo run backwards. A sign hung is three hammer taps and a rising blip. A sign taken down is a wooden clatter. The animatronic answers a tap with a clown-horn honk-honk. A tab switch is a camera click with static. Binning a page is a static swell. The notification is three lullaby notes.

## Do's and Don'ts

### Do:
- **Do** keep bulb amber (bulb-amber, bulb-glow) as the only light and the only action colour in the interface. Use one amber Done per card.
- **Do** draw every slot as a marquee bulb in one of its four forms, so state is legible without colour.
- **Do** set titles and numbers in Rye (Kelly Slab in Russian) and everything read in Manrope.
- **Do** keep the stage dark outside the room's one spotlight, using one light colour per room.
- **Do** let a fresh bulb keep its flare until the screen is left, and let a fully lit sign chase its border lights.
- **Do** freeze every loop, the buzz-on and the negative-flash when the animator duration scale is 0.
- **Do** use the circus's word overlay (Tickets, Faz-Tokens, Acts, Night Log, Animatronic, smashed) in EN, PT-PT and RU.

### Don't:
- **Don't** use tent red or cream stripe as a signal, a control fill or a text colour. They are canvas and wood.
- **Don't** put pink in the interface chrome for anything but Faz-Tokens, or coral for anything but a smashed bulb and a destructive action.
- **Don't** set sentences, labels or buttons in Rye.
- **Don't** light the stage evenly. The scene is one spotlight cut out of the dark, not a lit dashboard of glow rings.
- **Don't** copy game sprites, screenshots or audio. Every animatronic, room and sound is original.
- **Don't** change the diary's papers or inks for the circus. They are owner-pinned and shared with the workshop.
