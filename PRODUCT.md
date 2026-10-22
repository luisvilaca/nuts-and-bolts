# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Native Android app in Kotlin and Jetpack Compose, one app module, built with Gradle on the Windows PC and run from Android Studio. Chosen by the owner on 2026-10-17 to replace the earlier PWA; the web code is retired and nothing is imported from it (the owner starts fresh). Data lives on each device and, once the owner signs in with Google in Settings, syncs between the tablet and the phone through Firebase (Firestore), offline first (2026-10-21). Builds reach both devices through Firebase App Distribution (tools/publish.ps1, SYNC.md).

## Users

One person: the owner, using their own Xiaomi Pad 7 (11.2", 3200x2136, 3:2) mostly in landscape, with a Xiaomi pen. Personal daily use, no accounts, no other audience. The app also runs on the tablet in portrait and on an Android phone in both orientations.

## Product Purpose

A personal habit tracker and diary where daily habits feed a small robot companion. Doing habits earns currency and raises the robot's attributes; failing them takes both away. Currency buys parts that evolve the robot and its base. Success is the owner opening it every day to log habits and write a diary entry.

## Positioning

The robot is the readout of the owner's real life: its parts, attributes, and base only change through logged habits.

## Operating Context

- Landscape tablet first, touch plus pen; tablet portrait and phone (portrait and landscape) also supported. Handwriting diary entries with the pen; on a phone the diary still works with a finger or a phone stylus.
- Four sections: Lab, Shop, Habits, Diary, on a compact bottom bar at every size (hidden under an open diary page).
- Lab (home, called Today until 2026-10-18): the robot on its platform with an evolving background; beside it today's habits, logged right there, in a list that scrolls on its own when long; the four attributes pinned at the bottom right (name, what it means, level, a slim gauge; no stage names), always in view; currency balances. No week strip, tally or next-level line (owner, 2026-10-18).
- Shop: item shop to evolve the character; categories Head, Body, Arms, Legs, Accessories, Base.
- Habits: planning and managing: calendar and history (each habit's share of slots done, fails, best and current run); add and edit habits. Every date, past and future, opens to show the habits that run on it and can be logged there.
- Settings, from the top bar: sync (Google sign-in), backup and restore, the daily reminder, sounds, the robot's name.
- The robot is big where it matters: on tablet landscape a bay on Today and Shop, and no robot on Habits, Diary or the habit form, so the planner and calendar get the full screen (2026-10-17 streamlining); on a phone the full robot on Today and Shop only, a small robot badge elsewhere.
- Each habit is a card with a progress counter (slots logged of the target, a segment per slot) and one clear Done; Skip, Fail, taking the last mark back, Edit and Delete sit in its menu (owner, 2026-10-18; Skip and Fail were one tap before). Logging a habit or buying a part shows a message with Undo; tapping a filled nut also undoes.
- Diary: calendar view; pen entry on a printed diary page (dated header, ruled lines).

## Capabilities and Constraints

- Habit: a target of X times per week or per day. Positive and negative habits. Each runs from a start date until an end date, or indefinitely, on chosen weekdays (every day by default). A day off neither breaks nor extends a streak.
- Each habit entry has three outcomes: Done, Skipped, Failed.
- Each habit gives currency and attribute points when done, and takes them when failed.
- Two currencies: Bolts (every completed habit, ordinary parts) and Nuts (rarer, from streaks, special parts and base evolutions).
- Four attributes, robot-named with a human reading: Battery (energy), Processor (mind), Chassis (body), Core (heart).
- Diary pen input needs pressure support and stroke stabilization. One pen only: a fountain pen (the owner's default in StarNote).
- Diary ink colours, with the owner's roles: Ice Blue #E0F7FA (primary text), Matrix Green #00FF88 (headers), Electric Cyan #00E5FF (vectors and equations), Neon Fuchsia #FF007F (annotations and emphasis), Matte Black #393939 (for cream paper).
- Diary paper: three colour slots, matte black #393939 (StarNote's paper colour), a yellowish off-white, and a third; the owner can replace any slot with a hex value. Each page keeps the colour it was written on.
- The diary page covers the space beside its tools: the screen width on a tablet, the screen height on a phone held upright (owner, 2026-10-21), and moves to show the rest. Two fingers pinch to zoom (out to the whole page) and move it; one finger writes, or moves the page with Move on in the tool rail. The pen always writes, and nib and eraser stay their size on the screen at any zoom.
- Diary tools (moved 2026-10-18): a rail down the right edge of the page, away from the writing hand, so they are not changed by accident. The calendar (back) at the top; pen, eraser and the inks in the middle; undo, redo, paper and bin at the foot. Tapping the pen in hand opens the nib width (a slider in mm, 0.2 to 2.0 by 0.1) and the stabilizer (a slider in percent, 0 to 100 by 10; it never holds back fast strokes); tapping the eraser in hand, or holding it, opens its width (a slider in mm, 1 to 15). Millimetres are true on this screen. The eraser shows its outline under the pen while it rubs and while the pen hovers; the bin asks first. The tab bar hides while a page is open.
- Settings has a Palm rejection switch (on by default): the pen's first stroke turns Move on, so only the pen writes and a finger moves the page (Move in the rail turns it back); while the pen is near (hovering or writing in the last 1.5 s), or for a contact broader than 9 mm, touches do nothing, on the page or on the tools. Off, fingers write too.
- Pen buttons (Settings): each of the pen's two side buttons does one thing on a diary page: pen and eraser in turn (button 1 by default), the next ink (button 2 by default), undo, or nothing. A test pad shows which button is which. The Xiaomi Focus Pen sends its buttons as Page Down (button 1) and Page Up (button 2) from its own keyboard device; those count only when they come from a pen device, and the standard stylus button codes work too (confirmed working on the owner's tablet, 2026-10-18); a button the tablet keeps for a system shortcut has to be freed in the tablet's own pen settings.
- Writing latency: the pen's samples are delivered unbuffered, the live stroke reaches ahead to the platform's motion prediction (Android 14+), and the stabilizer lets go on fast strokes.
- Settings holds this device's tools: a backup file (all state and every diary page) to save and restore, and a daily reminder at a set time. A restore replaces everything after confirming; a file that does not check out changes nothing.
- The reminder is a native Android notification at the set time, skipped when the day already has a logged habit or a diary page. Tapping it opens Habits on today.
- Each habit can have its own reminder (habit form, 2026-10-21): a notification at its time on the days it runs, only while its period has open slots, with a Done (Resisted) button that logs it without opening the app. Alarms are exact and local to each device, so they work offline; notifications play the robot's own call.
- Sync (2026-10-21): the same Google account on both devices. Every change is written to the cloud at once (queued while offline), field by field, so changes made apart on the two devices merge; the wallet is counted from the marks and parts on each device, so it never counts a Done twice. Pen, sounds and the last-backup date stay per device. The first sign-in on a device with an existing cloud copy takes the cloud copy and keeps what was on the device in a backup file.
- Sound effects (2026-10-21) play at the media volume, in the workshop's own sounds: servos, ratchets, relays, metal, Geiger ticks and reactor hum, for Done, a frame going critical, Skip, Fail, a level gained or lost, a nut, a purchase, a part fitted, a habit added or deleted, the robot answering a tap, a tab change and a binned page.
- Interface languages (2026-10-22): English, Portuguese (Portugal) and Russian. The phone's language by default (Android's per-app language setting included), or a pick in Settings; the choice stays on each device. Everything is translated, the robot's default name, parts, attributes and levels included; habit names and notes stay as written. Russian is set in Manrope, as Lexend has no Cyrillic.
- Days off (2026-10-22): any day can be taken off from Habits; nothing runs, no streak breaks or grows, and the daily reminder stays quiet.
- Notes on bolts (2026-10-22): a few words on the last bolt of a day ("8 glasses", "5 km"), from the card's menu, shown in the habit's log.
- Achievements (2026-10-22): six, each giving a part for free when reached (Scarf, Bow Tie, Halo, Crown, Pet Moon, Sidekick Drone), listed in History.
- The week in review (2026-10-22): bolts in, nuts, pages, the best habit and the one that needs care; at the top of History, and on the Lab on Sundays.
- Diary export (2026-10-22): the written pages of a date range as one A4 PDF, each page whole with its paper and template.
- Home-screen widget (2026-10-22): today's habits with Done, logging without opening the app.

## Brand Commitments

- The app is named Nuts And Bolts, written exactly that way (not "Nuts & Bolts"), confirmed 2026-10-17. The robot's default name, Rivet, is a renamable placeholder.
- Clean and futuristic, with the robot theme strongly present.
- The robot is a cute, friendly mini-robot in the spirit of WALL-E or EVE, built on the owner's sketch: boxy head on a neck, body, two arms, two legs, standing on a round platform. It must be animated.
- Every icon is robot themed, and the typefaces share that character without looking crude or generated.
- No squared or grid paper anywhere, in the app background or the diary. Backgrounds are space, nuclear or robot themed and coherent with the rest of the app.
- The diary page is a true diary template, not a grid.

## Evidence on Hand

- The owner's hand sketch of the app (robot, tab bar, shop grid, habit rules, diary notes).
- No real habit data, diary entries, or part artwork exist yet; shop parts and starter habits are authored placeholders.

## Product Principles

- The robot reflects reality: nothing changes it except logged habits and purchases made with earned currency.
- Logging a habit takes one tap.
- Failing costs something, so the numbers stay honest.
- The diary is for handwriting first.
