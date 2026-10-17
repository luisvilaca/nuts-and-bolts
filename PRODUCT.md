# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Native Android app in Kotlin and Jetpack Compose, one app module, built with Gradle on the Windows PC and run from Android Studio. Chosen by the owner on 2026-10-17 to replace the earlier PWA; the web code is retired and nothing is imported from it (the owner starts fresh). Data stays on the device.

## Users

One person: the owner, using their own Xiaomi Pad 7 (11.2", 3200x2136, 3:2) mostly in landscape, with a Xiaomi pen. Personal daily use, no accounts, no other audience. The app also runs on the tablet in portrait and on an Android phone in both orientations.

## Product Purpose

A personal habit tracker and diary where daily habits feed a small robot companion. Doing habits earns currency and raises the robot's attributes; failing them takes both away. Currency buys parts that evolve the robot and its base. Success is the owner opening it every day to log habits and write a diary entry.

## Positioning

The robot is the readout of the owner's real life: its parts, attributes, and base only change through logged habits.

## Operating Context

- Landscape tablet first, touch plus pen; tablet portrait and phone (portrait and landscape) also supported. Handwriting diary entries with the pen; on a phone the diary still works with a finger or a phone stylus.
- Four sections: Today, Shop, Habits, Diary. A left navigation rail on tablet landscape; a bottom bar on phone and tablet portrait (approved 2026-10-17 after the Intent review).
- Today (home): the robot on its platform with an evolving background, today's habits logged right there, attributes, the week, currency balances.
- Shop: item shop to evolve the character; categories Head, Body, Arms, Legs, Accessories, Base.
- Habits: planning and managing: calendar and history (each habit's share of slots done, fails, best and current run); add and edit habits. Every date, past and future, opens to show the habits that run on it and can be logged there.
- Settings, from the top bar: backup and restore, the daily reminder, the robot's name.
- The robot is big where it matters: on tablet landscape a bay on Today and Shop and a smaller one on Habits and Diary; on a phone the full robot on Today and Shop only, a small robot badge elsewhere.
- Each habit row has one clear Done; Skip and Fail are smaller, still one tap. Logging a habit or buying a part shows a message with Undo; tapping a filled nut also undoes.
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
- The diary page fills the screen width and scrolls. With the pen in use, a finger drags the page.
- Diary toolbar, one line in the spirit of StarNote's: undo, redo, pen, eraser, inks, paper, bin anchored left, the calendar alone on the right, no date and no separators. Tapping the pen in hand opens nib width and stabilizer; holding the eraser opens its width; the bin asks first. While erasing, a ring under the pen shows the eraser's size.
- Settings holds this device's tools: a backup file (all state and every diary page) to save and restore, and a daily reminder at a set time. A restore replaces everything after confirming; a file that does not check out changes nothing.
- The reminder is a native Android notification at the set time, skipped when the day already has a logged habit or a diary page.
- Interface language: English.

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
