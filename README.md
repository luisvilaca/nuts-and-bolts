# Nuts And Bolts

A habit tracker and handwritten diary for Android, where the habits you actually keep build a small robot companion.
Doing a habit earns currency and raises the robot's attributes; failing it takes both away. The currency buys parts
that change the robot and its workshop.

<!-- Screenshots: put 3–4 images in docs/screenshots/ and uncomment this table.
| Lab | Habits | Diary |
|---|---|---|
| ![Lab](docs/screenshots/lab.png) | ![Habits](docs/screenshots/habits.png) | ![Diary](docs/screenshots/diary.png) |
-->

## Features

- **Habits:** daily or weekly targets, positive and negative habits, chosen weekdays, start and end dates. Each slot is
  Done, Skipped or Failed; streaks, days off and short notes ("5 km") are tracked per habit.
- **Progress you can see:** two currencies (Bolts for every completed habit, Nuts for streaks), four robot attributes
  that rise and fall with what you log, a shop of parts, achievements and a weekly review.
- **Handwritten diary:** pen input with a stroke stabiliser and motion prediction for low latency, palm rejection,
  stylus side buttons, pinch to zoom, paper colours and templates, and export of a date range to PDF.
- **Sync between devices:** offline first, with Google sign-in and Cloud Firestore. Changes are written field by field,
  so edits made apart on a phone and a tablet merge instead of overwriting each other.
- **Reminders and widget:** a daily reminder, per-habit reminders with a Done button in the notification, and a
  home-screen widget to log habits without opening the app.
- **Everything else:** backup and restore to a file, sound effects, phone and tablet layouts in both orientations, and
  the interface in English, Portuguese (Portugal) and Russian.

## Tech

- Kotlin and Jetpack Compose, one app module, Android 8.0 (API 26) and up
- Firebase Authentication (Google sign-in through Credential Manager) and Cloud Firestore for sync
- Jetpack Glance for the home-screen widget; exact alarms for reminders
- Local state stored as JSON files, with diary pages saved separately
- Unit and screen tests with JUnit and Robolectric (logic, sync, translations, screens)
- Two product flavours that share every screen and differ only in their visual skin

## Building

Open the project in Android Studio and run the `workshopDebug` variant, or from a terminal:

```bash
./gradlew assembleWorkshopDebug
```

Sync is optional. Without `app/google-services.json` the app builds and works fully on one device, and Settings says
sync is not set up. [SYNC.md](SYNC.md) (in Portuguese) explains how to connect a Firebase project.

## How it was made

I designed the app and built it with Claude Code as an AI pair programmer. I made the product and design
decisions (recorded in [PRODUCT.md](PRODUCT.md) and [DESIGN.md](DESIGN.md)), directed the implementation, and tested
the builds on my own tablet and phone.
