# File Organizer

A high-speed Android file organizer. Core idea: never leave the folder you're
organizing. Tap files to select them, tap a target folder to move them there
instantly — no confirmation dialogs, no auto-navigation to the target.

## What's implemented (v1)

- **Organizer Mode**: tapping a file selects/deselects it instead of opening
  it. Tapping a folder in the target tree moves all selected files there in
  one batch, immediately. The source pane never changes — you stay exactly
  where you were, selection cleared, ready for the next batch.
- **Split screen**: left pane is the source folder you're browsing; right
  pane is an expandable tree of every folder on the device, rooted at shared
  storage. The expand/collapse arrow is a separate control from the "move
  here" tap, so browsing the tree never accidentally triggers a move.
- **All Files Access permission flow** (Android 11+), with a fallback to the
  classic runtime permission on Android 10 and below.
- A lightweight Snackbar confirms how many files moved after each operation
  (non-blocking — doesn't interrupt the flow).

## Not yet implemented

- **Drag-and-drop** (feature 3 from the original spec). Tap-to-select +
  tap-to-move already satisfies "no dialogs, immediate move" end to end, but
  drag gesture handling needs real device testing to get right (coordinate
  math, conflicts with the existing tap/long-press handling, drag-shadow
  behavior differ across devices) — worth doing as a focused second pass
  once the tap flow is verified on a device.
- Rename/delete/copy, thumbnails, search, and sorting options beyond
  name/folders-first.
- Background threading for folder listing — `listDir()` runs on the main
  thread. Fine for typical folders; for folders with tens of thousands of
  files, move it to `Dispatchers.IO` and show a loading state.
- Conflict handling on move currently just skips a file if a same-named file
  already exists at the destination (reported in the Snackbar) rather than
  offering rename/overwrite/skip choices.

## Getting an installable .apk (without installing Android Studio)

This project is source code, not a compiled app — an `.apk` needs the Android
SDK/build tools to compile, which aren't available in the environment that
generated this project. Two ways to get an actual `.apk`:

**Option A — GitHub Actions (no software to install):**
1. Create a free GitHub account if you don't have one, and create a new repo.
2. Push this project's contents to that repo (or use GitHub's "upload files"
   button in the browser if you don't want to use git).
3. A workflow file at `.github/workflows/build-apk.yml` is already included —
   it builds a debug APK automatically on every push.
4. Go to the repo's **Actions** tab, wait for the run to finish (a few
   minutes), open it, and download the `app-debug-apk` artifact — that's your
   `.apk`.
5. Transfer it to your phone and install it (you'll need to allow
   "install unknown apps" for whichever app you use to open it).

This is a debug build, fine for installing on your own phone, but not signed
for the Play Store. I wrote this workflow but couldn't run it myself here, so
if the first run fails on a dependency version, that's the most likely spot —
check the Actions log, it'll say exactly what's missing.

**Option B — Android Studio:** open the project as described below, then
**Build → Build Bundle(s)/APK(s) → Build APK(s)**. Slower to set up the first
time, but gives you a proper dev environment if you plan to keep changing
the app.

## Opening the project

1. Open this folder in Android Studio (Koala/2024.1 or newer recommended).
2. Let Android Studio generate `local.properties` and sync Gradle
   automatically (it will prompt you the first time).
3. Run on a device or emulator running Android 8.0 (API 26) or newer.
4. On first launch you'll be sent to the "All files access" system settings
   screen — toggle it on for this app, then return; the app picks this up
   automatically via `onResume`.

## Project structure

```
app/src/main/java/com/fileorganizer/app/
  MainActivity.kt          — permission flow + Compose host
  model/FileItem.kt         — wraps java.io.File for the UI
  model/TargetRow.kt        — one row in the flattened target tree
  ui/OrganizerViewModel.kt  — all state: source dir, selection, target tree, move logic
  ui/OrganizerScreen.kt     — split-screen layout + selection bar + Snackbar
  ui/SourcePane.kt          — left pane (browse + select)
  ui/TargetPane.kt          — right pane (expandable tree + move-here tap)
  ui/PermissionScreen.kt    — shown until storage access is granted
  ui/theme/                 — Material3 color scheme
```
