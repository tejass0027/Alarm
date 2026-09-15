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

A Tkinter app that runs as a normal alarm clock while open. When an alarm
fires, it opens a topmost window that plays a looping alarm sound and won't
let you close it — you have to solve a configurable number of challenge
rounds in a row to dismiss it. Giving up or letting it time out deducts the
stake automatically.

- One-time alarms disable themselves after firing once; repeating alarms
  reschedule for their next matching day.
- Every firing — solved, failed, or given up — is logged to a history view
  alongside the resulting balance.
- Challenges are pluggable: math and typing exist now, and adding a new
  `Challenge` subclass is enough to register a new type.

## Android app

A native alarm-clock app built on `AlarmManager`'s alarm-clock API, so
alarms fire reliably even in Doze mode and reschedule themselves after a
reboot. When one rings, a foreground service loops the alarm sound and a
full-screen activity takes over the lock screen with the same
challenge-to-dismiss mechanic as the desktop app — solve it or give up and
lose the stake.

## Web UI concept

A design concept for what a Stake Alarm web app could look like: a
dashboard with a balance card, alarm cards you can add/edit/delete/toggle,
and a full-screen ringing/challenge view (triggered from a "Preview" button
per card, since there's no real scheduling behind it). It's a visual and
interaction mockup, not a persisted, wired-up app.
