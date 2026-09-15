# Stake Alarm

An alarm clock that puts money on the line. Deposit into a virtual balance,
set an alarm with a stake, and when it rings you have to solve a challenge
(math or typing) to dismiss it. Give up, ignore it, or run out of time and
the stake is deducted automatically.

Nothing here moves real money — every implementation tracks a virtual
balance locally and exposes a `PaymentProvider`-style interface so a real
payment backend (Stripe, etc.) could be swapped in later without touching
the rest of the app.

This repo has the same concept built three times, independently, for three
platforms:

| Platform | Where | Stack |
|---|---|---|
| Desktop | this folder ([main.py](main.py)) | Python, Tkinter |
| Android | [android/](android) | Kotlin, Jetpack Compose |
| Web | [web/](web) | React, Tailwind CSS (design concept, no backend) |

Each one schedules alarms, requires solving a math or typing challenge to
dismiss them, and deducts the stake on failure/give-up — but they don't
share code or data. Alarms set on one platform don't appear on another.

## Desktop app (Python)

### Requirements

- Python 3.10+ on Windows (uses `tkinter` and `winsound`, both included with
  the standard Windows Python installer — no `pip install` needed).

### Run it

```bash
python main.py
```

The app window must stay open (it can be minimized) for alarms to fire —
there's no background service.

### Notes

- One-time alarms (no repeat days checked) automatically disable themselves
  after firing once.
- Each alarm requires solving a configurable number of challenge "rounds" in
  a row, and auto-fails (deducting the stake) after a configurable timeout
  if you never respond.
- History of every alarm firing — solved, failed, or given up — plus the
  resulting balance is kept in `data/history.json` and viewable from the
  "History" button.
- To add a new challenge type, add a `Challenge` subclass in `challenges.py`
  and register it in `CHALLENGE_TYPES`.

## Android app

Native Kotlin + Jetpack Compose app using `AlarmManager`'s alarm-clock API,
a foreground service for ringing, and a full-screen lock-screen activity for
the challenge. Open [android/](android) in Android Studio and run it —
see [android/README.md](android/README.md) for permissions and known
environment gotchas (e.g. a space in your Windows username breaking
Gradle's daemon).

## Web UI concept

A single-file, buildless React + Tailwind mockup of the interface — open
[web/index.html](web/index.html) directly in a browser, no build step. It's
a design concept, not a wired-up app: no persistence, no real scheduling.
See [web/README.md](web/README.md) for the phase-by-phase build notes.
