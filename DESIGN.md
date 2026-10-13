---
name: Nuts & Bolts
description: A habit tracker and diary where every habit is a fastener tightened on a small robot.
colors:
  ink: "oklch(0.16 0.03 262)"
  ink-2: "oklch(0.205 0.035 262)"
  ink-3: "oklch(0.27 0.045 262)"
  line: "oklch(0.3 0.04 262)"
  line-2: "oklch(0.47 0.05 262)"
  cobalt: "oklch(0.42 0.17 264)"
  cobalt-deep: "oklch(0.31 0.14 264)"
  on-cobalt: "oklch(0.9 0.05 264)"
  ceramic: "oklch(0.97 0.008 250)"
  dim: "oklch(0.77 0.03 258)"
  zinc: "oklch(0.82 0.03 240)"
  brass: "oklch(0.81 0.13 85)"
  signal: "oklch(0.86 0.19 155)"
  coral: "oklch(0.72 0.18 25)"
  paper-black: "#393939"
  paper-cream: "#f4ecd8"
  paper-space: "#0B1020"
  ink-ice: "#E0F7FA"
  ink-green: "#00FF88"
  ink-cyan: "#00E5FF"
  ink-fuchsia: "#FF007F"
typography:
  display:
    fontFamily: "Oxanium, system-ui, sans-serif"
    fontSize: "2.1rem"
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: "normal"
  heading:
    fontFamily: "Oxanium, system-ui, sans-serif"
    fontSize: "1.25rem"
    fontWeight: 600
    lineHeight: 1.45
    letterSpacing: "normal"
  label:
    fontFamily: "Oxanium, system-ui, sans-serif"
    fontSize: "0.95rem"
    fontWeight: 600
    lineHeight: 1.45
    letterSpacing: "0.03em"
  body:
    fontFamily: "'Exo 2', system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.45
    letterSpacing: "normal"
  meta:
    fontFamily: "'Exo 2', system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.45
    letterSpacing: "normal"
rounded:
  control: "0.7rem"
  segment: "0.6rem"
  tile: "1rem"
spacing:
  row: "0.8rem"
  pane-x: "2.25rem"
  pane-y: "1.5rem"
  section: "2.25rem"
components:
  button:
    backgroundColor: "{colors.ink-2}"
    textColor: "{colors.ceramic}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "0 1.2rem"
    height: "3rem"
  button-primary:
    backgroundColor: "{colors.signal}"
    textColor: "{colors.ink}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "0 1.2rem"
    height: "3rem"
  button-fail:
    backgroundColor: "{colors.ink-2}"
    textColor: "{colors.coral}"
    rounded: "{rounded.control}"
    height: "3rem"
  segment-selected:
    backgroundColor: "{colors.ceramic}"
    textColor: "{colors.ink}"
    rounded: "{rounded.segment}"
    height: "2.5rem"
  tile:
    backgroundColor: "{colors.ink-2}"
    textColor: "{colors.ceramic}"
    rounded: "{rounded.tile}"
    padding: "0.6rem 0.6rem 0.75rem"
  hex-slot:
    size: "2.75rem"
---

# Design System: Nuts & Bolts

## Overview

**Creative North Star: "The charging bay at night"**

A small robot stands in a cobalt bay while its keeper sorts fasteners on a dark bench beside it. The app is used in the evening, on a tablet in landscape, lights low, pen in hand, so the ground is dark; the bay is the one saturated region and it always holds the robot.

**Key Characteristics:**

- A cobalt field on the left of every screen, with the robot in it; the work sits on ink to its right.
- Habits are hex nuts. Done, skipped and failed are states of the same mark everywhere it appears.
- Rows and hairlines carry lists. Tiles are reserved for shop parts and calendar days.
- Ceramic white, zinc and brass are materials with jobs, not decoration.

## Colors

The ground is a deep blue-black with a sparse star field and one faint atom, its orbits echoing the charging ring. No grid or squared paper anywhere.

### Primary

- **Cobalt** (`cobalt`, `cobalt-deep`): the bay. Owns a whole region; never used for small accents on ink.
- **Signal mint** (`signal`): primary actions, done marks, the active tab, the robot's eyes.

### Secondary

- **Zinc** (`zinc`): bolts, attribute icons, skipped marks.
- **Brass** (`brass`): nuts and anything bought with them.
- **Coral** (`coral`): failed marks, the fail and delete actions, the "break" tag.

### Neutral

- **Ink** (`ink`, `ink-2`, `ink-3`): ground, raised controls and tiles, hover.
- **Ceramic** (`ceramic`): primary text, the robot shell, selected segments, meter fill.
- **Dim** (`dim`): secondary text on ink. On cobalt, secondary text uses `on-cobalt` instead.
- **Paper** (`paper-black`, `paper-cream`, `paper-space`): the three default diary papers. The owner can replace any of them by hex; the printed template turns light or dark to suit.
- **Diary inks** (`ink-ice`, `ink-green`, `ink-cyan`, `ink-fuchsia`, plus `paper-black` as ink on cream): the owner's writing palette. The printed template follows their roles: green for the header, fuchsia for the annotation margin.

### Named Rules

**The Pressable Rule.** A mint fill means the control is the primary action in its group. A disabled primary loses the mint.

**The Two Metals Rule.** Zinc always means bolts and brass always means nuts, in the wallet, on prices and in the scene.

## Typography

Oxanium (self-hosted, variable 300 to 800) carries headings, labels, numerals and catalog numbers; its chamfered corners echo the hex nut. Running text uses Exo 2 (self-hosted, variable 300 to 700), a technical sans that keeps the same squared character at reading sizes.

### Hierarchy

- **Display** 2.1rem / 700: the nameplate at the top of each bay.
- **Heading** 1.25rem / 600: section headings in the pane; 1.6rem for a form or empty-state title.
- **Label** 0.95rem / 600, 0.03em: buttons and segments. Tabs and small labels are uppercase with 0.08 to 0.16em tracking.
- **Body** 1rem and **Meta** 0.875rem in Exo 2.

Numerals that change (wallet, meters, dates) use tabular figures. The root size scales with viewport width, 17px at 1600px.

## Layout

Three rows: a 3.25rem top strip (wordmark, date, wallet), the screen, a 4rem bottom tab bar. Each screen is two columns: the bay (58% on Base, 40% in Shop, 27% in Habits and Diary) and a scrolling pane with 1.5rem by 2.25rem padding. Pane sections get 2.25rem above their heading and 0.8rem below. The diary page editor takes the full width. Under 820px wide or in portrait, the bay stacks above the pane.

## Elevation & Depth

Flat. Depth comes from the cobalt field against ink and from one tone step (`ink-2`) for controls and tiles. The only shadow is under the toast. The robot's eyes and status lights carry a small light spill; nothing else glows.

## Shapes

Controls are 0.7rem rounded rectangles, segments 0.6rem inside a 0.85rem track, tiles 1rem. The hexagon is reserved for the fastener mark and the nut icon. Icons are authored 24px line SVG at 1.8 stroke with square caps and mitred corners. They share one robot grammar: angular outlines, and a small circle (a pivot joint) wherever a part would hinge, as on the chevrons, the undo and redo arms, the pen and the eraser.

## Components

### Buttons

3rem tall. Default: `ink-2` fill, `line-2` border. Primary: mint fill, ink text. Fail and danger: coral text and border. Pressed scales to 0.96. On the bay, secondary buttons use a translucent dark cobalt.

### Segmented control

A track with a ceramic pill on the selected segment. Used for view switches, shop categories and every radio group in forms and the pen toolbar.

### Tiles

Shop parts and base tiers: thumbnail on `cobalt-deep`, name, catalog number, price or state. The selected tile takes a ceramic outline; the equipped one states it in mint.

### Navigation

Four tabs across the bottom, icon plus uppercase label. The current tab is mint with a short bar on the top edge.

### Fastener mark

A 2.75rem hex nut per slot of a habit's day or week. Open: outline. Done: mint fill, turned home with a sixth-turn animation. Skipped: dashed zinc outline with a bar. Failed: coral outline with a cross. Marks logged on another day of the week are dimmed. Tapping a mark from the viewed day undoes it.

### Meter

Attributes are a 20-cell gauge, ceramic on `ink-3`.

### Diary page

A printed log sheet in A4 proportions that fills the width and scrolls: weekday and date top left, a five-nut charge gauge to fill in by hand, the day number and an atom mark top right, ruled lines with a fuchsia annotation margin, a large faint atom near the foot, and a catalog number in the footer. One pen, a fountain pen whose line swells with pressure and thins with speed.

### Diary toolbar

One line that never wraps: Calendar, the date, then paper (icon and three swatches), pen (icon and five inks), eraser, and undo, redo, bin, with hairline separators between groups. Settings drop from the icon that owns them: paper opens the hex editor, pen opens nib and stabilizer, and a long press on the eraser opens its width. A corner tick marks a button that has more behind a long press.

### Calendar day

A tile with the day number top left and one mark bottom right: a dot sized by completion (coral ring if anything failed) or a pen for a diary page. Today has a mint outline; future days are dashed and disabled.

## Do's and Don'ts

### Do:

- Do keep the robot on screen wherever the pane does not need the full width.
- Do show habit state with the fastener mark rather than a new symbol.
- Do give every part a catalog number in the `NB-` series.
- Do keep touch targets at 2.75rem or more.

### Don't:

- Don't add glow or blur to panels, borders or text.
- Don't use cobalt as an accent on ink, or mint as decoration.
- Don't wrap pane sections in cards.
- Don't use grid or squared paper, on the diary page or as a background.
