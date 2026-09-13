# Stake Alarm

An alarm clock that puts money on the line. Deposit into a virtual balance,
set an alarm with a stake, and when it rings you have to solve a challenge
(math or typing) to dismiss it. Give up, ignore it, or run out of time and
the stake is deducted automatically.

Everything runs locally — the "balance" is a virtual number tracked in
`data/balance.json`, not real money. `balance.py` defines a `PaymentProvider`
interface so a real payment backend (Stripe, etc.) could be swapped in later
without touching the rest of the app.

## Requirements

- Python 3.10+ on Windows (uses `tkinter` and `winsound`, both included with
  the standard Windows Python installer — no `pip install` needed).

## Run it

```bash
python main.py
```

The app window must stay open (it can be minimized) for alarms to fire —
there's no background service.

## Notes

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
