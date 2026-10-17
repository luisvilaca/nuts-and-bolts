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
THESIS: The robot lives inside the atom of the owner's day: each habit is an orbit around it and each slot an electron that Done lights. Refuses the category default of a dark dashboard of neon-glow stat cards and progress rings.
OWN-WORLD: Expo 58 at night. Night-indigo ground with a deeper rail; polished steel for bolts, the robot shell and gauges; atomic turquoise only for pressable-primary, done and the robot's eyes; starburst gold for nuts; flame coral only for failed. Michroma (Microgramma-era extended) for display, numerals and spaced labels; Jost (Futura revival, the Apollo 11 plaque face) for body. Orbit ellipses are the one ornament. Hairline rows, no cards around sections.
STORY: The owner opens the app, sees the robot ringed by today's orbits, taps Done per habit and watches an electron light, the robot cheer and the wallet tick, with Undo in the snackbar; then shops or writes the day's page.
FIRST VIEWPORT (Today, tablet landscape 1280x854dp): 92dp nav rail (atom mark; Today, Shop, Habits, Diary; Settings at the foot). Header: "Today" in Michroma, the date, wallet at right. Left 44%: the robot about 340dp tall on its platform at the nucleus of one orbit per habit running today, an electron per slot. Right: "TODAY · 3 OF 9 LIT", habit rows (name, meta, electron slots, turquoise Done pill, small Skip and Fail icon buttons), four steel gauges in two columns, the week as seven mini-atoms. Phone portrait: compact atom at top, the next habit set monumental, the list below, bottom bar.
SIGNATURE: Done lights an electron: it runs along its orbit to its slot and flashes a short starburst while the robot cheers. The same electron mark is the state in rows, calendar day atoms and history. Raises: on a phone, Today leads with the one next habit, monumental (Terminal Wayfinding); a run is drawn as a bar exactly its days long (Labanotation); each state has its own mark form, lit, hollow, struck (Centre-Rail); hierarchy by type scale alone, no card boxes (Type Specimen); turquoise only where something can be pressed or is done (Warm Consumer).
FORM: atomic-age space-age world (Expo 58, the Atomium), candidate 3 of 7 on the grounded list; seed:fc741dc7c2f05c81.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
