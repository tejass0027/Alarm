# Stake Alarm — Android

Native Android implementation (Kotlin + Jetpack Compose) of the Stake Alarm concept:
deposit into a virtual balance, set an alarm with a stake, and solve a math or typing
challenge to dismiss it when it rings. Giving up or letting it time out deducts the
stake.

## Requirements

- Android Studio with an Android SDK (compileSdk 34, minSdk 26)
- A device or emulator running Android 8.0 (API 26) or newer

## Run it

Open this `android/` folder as the project root in Android Studio and run the `app`
configuration on a device or emulator. No manual Gradle setup should be needed — the
Gradle wrapper is committed.

## Permissions

On first run, Android will prompt for (or the app will show a banner requesting):

- **Notifications** (Android 13+) — needed to show the ringing alarm.
- **Exact alarms** (`SCHEDULE_EXACT_ALARM`) — needed so alarms aren't delayed by Doze.
  If this isn't granted, the dashboard shows a "Grant permission" banner.

## Architecture

- `data/` — `Alarm`/`HistoryEntry` models with JSON persistence in app-internal
  storage, plus a `PaymentProvider` interface (`LocalVirtualProvider` for the virtual
  balance now; `RealPaymentProvider` stubbed for a future real payment backend).
- `challenges/` — math/typing challenge generators, extensible via the `Challenge`
  interface.
- `alarm/` — `AlarmScheduler` (uses `AlarmManager.setAlarmClock`, the API intended for
  real alarm-clock apps), `AlarmReceiver`, `BootReceiver` (reschedules after reboot),
  `AlarmRingService` (foreground service that loops the alarm sound and posts a
  full-screen notification), and `RingingActivity` (shows over the lock screen and
  runs the challenge).
- `ui/` — Jetpack Compose screens: the alarm list, the add/edit dialog, and history.

## Known gotcha: Windows usernames with a space

If you build from the command line (`./gradlew assembleDebug`) on a Windows machine
where the user profile path contains a space (e.g. `C:\Users\Jane Doe\`), Gradle's
daemon can fail with:

```
java.io.IOException: Unable to establish loopback connection
```

This is a JVM/Windows issue with the daemon's local socket, not a Gradle or project
config problem — it happens because `java.io.tmpdir` resolves into the spaced path.
Workaround: point `TMP`/`TEMP` at a short, space-free path before building, e.g.:

```bash
export TMP="C:\\gt"
export TEMP="C:\\gt"
./gradlew assembleDebug
```

Building from Android Studio's own Gradle integration is usually unaffected, but if
you hit the same error there, set a short-path Gradle user home under
**Settings → Build Tools → Gradle → Gradle user home**.
