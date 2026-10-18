---
version: 1
slug: "app"
primary_target: "app"
related_targets: []
---

# Android app (Jetpack Compose)
Scope: the whole native app: Lab, Shop, Habits, Diary, Settings, and the diary page editor. Visitor mode: Operate.
Audience and job: the owner, on a Xiaomi Pad 7 mostly in landscape, evenings, lights low, Xiaomi Focus Pen in hand; also tablet portrait and a phone in both orientations. Log today's habits in one tap each beside the robot, spend earnings in the shop, plan habits, handwrite a diary page.
Constraints: Material 3 structure with a bottom bar at every size (never a side rail; hidden under an open diary page), system Back, edge-to-edge insets, snackbars with Undo, Material date and time pickers; 44dp controls (52dp lead Done); sp type; works offline. Brief pins: clean and futuristic, robot theme, animated WALL-E/EVE-style robot from the owner's sketch, robot-themed icons, space or nuclear backgrounds, a true diary template on the owner's papers and inks with the atom emblem, no grid paper, no plants.
Build path: code-led (Operate surface, no image generation).
Unresolved: robot name "Rivet" is a renamable placeholder.
## Direction contract
THESIS: Rivet bolts together a frame for every habit at a reactor-powered bench: each Done drives a bolt home, and a habit with every bolt in goes critical. Refuses the category default of a dark dashboard of glowing stat cards and progress rings.
OWN-WORLD: A robot's lab after dark. Greenhouse-teal ground #0D1615 with cards #152220; uranium glow #A6FF4D as the only light (the reactor, its rail, gauges, completed work); ready green #6EE787 only for pressable and here; cool steel #CFE3DE for metal, driven bolts and selection; coral #FF6B4A only for failed (a sheared bolt); gold #F2C14E only for nuts. Lexend throughout, rounded and open. Slots are bolts: driven, dashed, sheared, open hole.
STORY: The owner opens the app to Rivet beside its bench and reactor, taps Done on a habit card beside it and watches the bolt screw into that habit's frame; when the last bolt seats, the frame glows and positrons rise.
FIRST VIEWPORT (Lab, tablet landscape 1280x854dp): header: Lab, the date, wallet, settings. Left 46%: Rivet on its platform, the charging station, the bench with one frame per habit under the light rail, the fusion capsule beneath. Right: today's habit cards (counter over a tracker, Done, a menu), scrolling on their own; the four attribute cards pinned at the foot. Bottom bar: Lab, Shop, Habits, Diary.
SIGNATURE: Done screws the newest bolt into that habit's frame over 700ms while Rivet cheers and a ratchet sounds; the frame goes critical when complete. Instant under reduced motion.
FORM: Reactor Workshop, the owner's choice after the Grow Room (its teal tint kept, its plants removed), 2026-10-18.
FINISH: unreviewed and undocumented is unfinished; each change ends with the ScreensTest renders checked at all four window classes and DESIGN.md describing what they show.
