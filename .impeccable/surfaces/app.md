---
version: 1
slug: "app"
primary_target: "app"
related_targets: []
---

# Android app (Jetpack Compose)
Scope: the whole native app: Today, Shop, Habits, Diary, Settings, and the diary page editor. Visitor mode: Operate.
Audience and job: the owner, on a Xiaomi Pad 7 mostly in landscape, evenings, lights low, pen in hand; also tablet portrait and a phone in both orientations. Log today's habits in one tap each beside the robot, spend earnings in the shop, plan habits, handwrite a diary page.
Constraints: Material 3 structure (nav rail on expanded width, bottom bar on compact and medium, system Back, edge-to-edge insets, snackbars with Undo, Material date and time pickers); 48dp targets; sp type; works offline. Brief pins: clean and futuristic, robot theme, animated WALL-E/EVE-style robot from the owner's sketch, robot-themed icons, space or nuclear backgrounds, a true diary template on the owner's papers and inks, no grid paper.
Build path: code-led (Operate surface, no image generation).
Unresolved: robot name "Rivet" is a renamable placeholder.
## Direction contract
THESIS: Rivet tends a seedling for every habit: each Done unfurls a leaf, and the grow tray is the record of the day. Refuses the category default of a dark dashboard of glowing cards and progress rings.
OWN-WORLD: A hydroponic grow room after dark. Deep greenhouse teal #0B1A1A, a magenta grow light #E05AD0 whose glow washes the bay, leaf green #6EE787 only for pressable-primary and grown; coral #FF6B4A only for failed (a wilted leaf). Lexend throughout, rounded and open. Slots are leaves: unfurled solid, skipped dashed, wilted coral, open bud.
STORY: The owner opens the app to Rivet beside its tray under the grow light, taps Done and watches a leaf unfurl on that habit's sprout.
FIRST VIEWPORT (Today, tablet landscape 1280x854dp): dark teal rail with a sprout mark. Header: Today, the date, wallet. Left 46%: Rivet on its platform; to its right, under the hanging grow light, a glass tray with one sprout per habit running today and a leaf per slot. Right: TODAY · 3 OF 9 LEAVES OPEN; rows with leaf slots, green Done, Skip, Fail; steel gauges; the week as seven sprouts sized by how the day went.
SIGNATURE: Done unfurls the newest leaf on that habit's sprout over 700ms while Rivet cheers; instant under reduced motion. The leaf is the slot, the sprout is the day.
FORM: hydroponic grow room, Impeccable's pick in round two, candidate 1 of 7; seed:fc741dc7c2f05c81.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
