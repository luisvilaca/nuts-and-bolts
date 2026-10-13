# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

delegated: installable web app (PWA) in plain HTML/CSS/JS, no framework and no build step. Chosen because it can be built and tested on the Windows PC, runs fullscreen in landscape on the tablet, and reaches pen pressure through Pointer Events. Data is stored on the device (localStorage). Needs an HTTPS host (e.g. GitHub Pages) to install on the tablet; can be wrapped as an APK later without a rewrite.

## Users

One person: the owner, using their own Xiaomi Pad 7 (11.2", 3200x2136, 3:2) in landscape, with a Xiaomi pen. Personal daily use, no accounts, no other audience.

## Product Purpose

A personal habit tracker and diary where daily habits feed a small robot companion. Doing habits earns currency and raises the robot's attributes; failing them takes both away. Currency buys parts that evolve the robot and its base. Success is the owner opening it every day to log habits and write a diary entry.

## Positioning

The robot is the readout of the owner's real life: its parts, attributes, and base only change through logged habits.

## Operating Context

- Landscape tablet, touch plus pen. Handwriting diary entries with the pen.
- Four sections on a bottom category bar: Base, Shop, Habits, Diary.
- Base: the robot on its platform with an evolving background, currency balances, attribute bars.
- Shop: item shop to evolve the character; categories Head, Body, Arms, Legs, Accessories.
- Habits: day-to-day view with a switch to calendar view; add habit.
- Diary: calendar view; pen entry on a printed diary page (dated header, ruled lines, annotation margin).

## Capabilities and Constraints

- Habit: a target of X times per week or per day. Positive and negative habits.
- Each habit entry has three outcomes: Done, Skipped, Failed.
- Each habit gives currency and attribute points when done, and takes them when failed.
- Two currencies: Bolts (every completed habit, ordinary parts) and Nuts (rarer, from streaks, special parts and base evolutions).
- Four attributes, robot-named with a human reading: Battery (energy), Processor (mind), Chassis (body), Core (heart).
- Diary pen input needs pressure support and stroke stabilization. One pen only: a fountain pen (the owner's default in StarNote).
- Diary ink colours, with the owner's roles: Ice Blue #E0F7FA (primary text), Matrix Green #00FF88 (headers), Electric Cyan #00E5FF (vectors and equations), Neon Fuchsia #FF007F (annotations and emphasis), Matte Black #393939 (for cream paper).
- Diary paper: three colour slots, matte black #393939 (StarNote's paper colour), a yellowish off-white, and a third; the owner can replace any slot with a hex value. Each page keeps the colour it was written on.
- The diary page fills the screen width and scrolls. With the pen in use, a finger drags the page.
- Diary toolbar, one line, in this order: calendar, date, paper, pen and inks, eraser, undo, redo, bin. Tapping the pen opens nib width and stabilizer; holding the eraser opens its width; the bin asks first.
- Interface language: English.

## Brand Commitments

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
