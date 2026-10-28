# GitGraph

GitGraph is an Android home screen widget that shows your GitHub contribution graph, with a companion app to set it up.  
This fork rounds the widget's cells like GitHub's own graph, stops the bottom row from being clipped on short widgets, and adds a Material You theme that follows your wallpaper.

<p align="center">
  <img src="screenshots/widget_on_home_screen.jpeg" alt="Widget on the home screen" width="320">
</p>

## Features

**Widget**

- Your real contribution data, fetched from GitHub's GraphQL API
- Rounded cells, proportioned like the ones on your GitHub profile
- Resizable: a wider widget shows more weeks, a taller one gets bigger cells
- Hourly updates, and a tap opens the app

**App**

- Current and longest streak
- Tap any cell for its date and contribution count
- Year-over-year comparison and a breakdown of your contribution activity
- Daily reminders, with the evening one skipped once you have contributed
- Pull to refresh, with cached data shown while offline
- Four color themes, applied to both the app and the widget

> [!NOTE]
> Your token is stored only on your device, and the app talks to GitHub directly.
> Nothing is sent anywhere else.

---

## Color themes

| Theme | Graph | Current streak | Longest streak |
|---|---|---|---|
| Green | GitHub's default greens | orange | gold |
| Blue | slate blue | dark blue | light blue |
| Yellow | amber | dark amber | bright yellow |
| Material You | your wallpaper's accent colors | medium accent | bright accent |

Material You needs Android 12 or newer, and only shows up in the theme picker on
devices with dynamic colors. On the widget it also tints the background and the
empty cells, and it follows wallpaper changes by itself.

> [!TIP]
> The widget picks up a new theme on its next update. Tap **Update Widget Now** in
> the app to apply it straight away.

---

## Changes from upstream

This is a fork of [aequerion/git-graph](https://github.com/aequerion/git-graph). Each
change lives on its own branch, stacked in this order:

| Branch | Change |
|---|---|
| `fix/duplicate-today-contribution-helpers` | removes two methods a merge left duplicated, which stopped the app from compiling |
| `fix/widget-rounded-cells` | rounded widget cells, which were drawn as plain squares |
| `fix/widget-row-clipping` | the bottom row is no longer cut off on short widgets, plus a compact cell size for them |
| `feature/material-you-theme` | the Material You theme, in the widget and the app |

---

## Requirements

- Android 7.0 or newer, Android 12 or newer for Material You
- A GitHub account
- A personal access token (classic) with the `read:user` scope

---

## Installation

There are no prebuilt releases of this fork, so [build it](#building) first, then
install the APK over USB:

```bash
adb install build/app/outputs/flutter-apk/app-release.apk
```

Or copy the APK to your phone and open it from a file manager, allowing installs
from that app when asked.

> [!WARNING]
> If the upstream GitGraph is installed, uninstall it first. The two are signed with
> different keys, so Android refuses to install one over the other. You will have
> to enter your username and token again.

---

## Building

| Dependency | Purpose |
|---|---|
| Flutter (stable) | builds the app |
| Android SDK | platform, build tools and `adb` |
| JDK 17 | runs Gradle |

### Arch Linux

```bash
sudo pacman -S --needed jdk17-openjdk android-tools
yay -S flutter-bin
```

Download the [Android command line tools](https://developer.android.com/studio#command-line-tools-only),
unpack them to `~/Android/Sdk/cmdline-tools/latest`, then install the SDK packages
and point Flutter at them:

```bash
~/Android/Sdk/cmdline-tools/latest/bin/android --sdk="$HOME/Android/Sdk" sdk install platform-tools platforms/android-36 build-tools/36.1.0
flutter config --android-sdk ~/Android/Sdk --jdk-dir /usr/lib/jvm/java-17-openjdk
flutter doctor
```

`flutter doctor` lists anything still missing.

### Build

```bash
git clone https://github.com/orrnithogalum/git-graph.git
cd git-graph

flutter pub get
flutter build apk --release --android-skip-build-dependency-validation
```

> [!NOTE]
> The project still pins Gradle 8.7, Android Gradle Plugin 8.2.1 and Kotlin 1.9.0.
> Recent Flutter releases reject those versions by default;
> `--android-skip-build-dependency-validation` skips that check, and the build
> itself works.

---

## Setup

1. Create a [personal access token (classic)](https://github.com/settings/tokens/new) with the `read:user` scope
2. Open GitGraph, enter your username and paste the token
3. Tap **Save & Fetch Contributions**
4. Long-press your home screen, open **Widgets**, find **GitGraph** and drag it out
5. Resize it until it looks right

---

## Troubleshooting

<details>
<summary><b>The widget shows an old theme or old data</b></summary>

Widgets update hourly. Tap **Update Widget Now** in the app to refresh it
immediately.
</details>

<details>
<summary><b>"App not installed" when installing the APK</b></summary>

A GitGraph signed with a different key is already installed, usually the upstream
release. Uninstall it and install again.
</details>

<details>
<summary><b>There is no Material You circle in the theme picker</b></summary>

The device has no dynamic colors, which need Android 12 or newer. The other three
themes still work.
</details>

<details>
<summary><b>The widget looks wrong at some size</b></summary>

The widget logs the size the launcher reports and the cell size it picked:

```bash
adb logcat -s GitHubWidget
```
</details>

---

## Credits

GitGraph was created by Jerome Wilson as
[aequerion/git-graph](https://github.com/aequerion/git-graph), and this fork builds
on that work under the MIT License, see [LICENSE](LICENSE).

GitGraph is not affiliated with GitHub. GitHub is a trademark of GitHub, Inc.
