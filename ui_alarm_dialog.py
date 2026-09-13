import tkinter as tk
from tkinter import messagebox

from models import Alarm

DAY_LABELS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]


class AlarmDialog(tk.Toplevel):
    def __init__(self, master, on_save, existing: Alarm = None):
        super().__init__(master)
        self.on_save = on_save
        self.existing = existing
        self.title("Edit alarm" if existing else "New alarm")
        self.resizable(False, False)
        self.transient(master)
        self.grab_set()

        self._build_ui()
        if existing:
            self._load(existing)

    def _build_ui(self):
        pad = {"padx": 10, "pady": 6}

        tk.Label(self, text="Time (HH:MM, 24h)").grid(row=0, column=0, sticky="w", **pad)
        time_frame = tk.Frame(self)
        time_frame.grid(row=0, column=1, sticky="w", **pad)
        self.hour_var = tk.StringVar(value="07")
        self.minute_var = tk.StringVar(value="00")
        tk.Spinbox(time_frame, from_=0, to=23, width=3, format="%02.0f", textvariable=self.hour_var).pack(side="left")
        tk.Label(time_frame, text=":").pack(side="left")
        tk.Spinbox(time_frame, from_=0, to=59, width=3, format="%02.0f", textvariable=self.minute_var).pack(side="left")

        tk.Label(self, text="Label").grid(row=1, column=0, sticky="w", **pad)
        self.label_var = tk.StringVar()
        tk.Entry(self, textvariable=self.label_var, width=28).grid(row=1, column=1, sticky="w", **pad)

        tk.Label(self, text="Stake ($)").grid(row=2, column=0, sticky="w", **pad)
        self.stake_var = tk.StringVar(value="5.00")
        tk.Entry(self, textvariable=self.stake_var, width=10).grid(row=2, column=1, sticky="w", **pad)

        tk.Label(self, text="Repeat on").grid(row=3, column=0, sticky="w", **pad)
        days_frame = tk.Frame(self)
        days_frame.grid(row=3, column=1, sticky="w", **pad)
        self.day_vars = []
        for i, name in enumerate(DAY_LABELS):
            v = tk.BooleanVar(value=False)
            tk.Checkbutton(days_frame, text=name, variable=v).grid(row=0, column=i)
            self.day_vars.append(v)
        tk.Label(self, text="(leave all unchecked for a one-time alarm)", font=("Segoe UI", 8), fg="#666") \
            .grid(row=4, column=1, sticky="w", padx=10)

        tk.Label(self, text="Challenge").grid(row=5, column=0, sticky="w", **pad)
        self.challenge_var = tk.StringVar(value="random")
        challenge_frame = tk.Frame(self)
        challenge_frame.grid(row=5, column=1, sticky="w", **pad)
        for val, text in [("random", "Random"), ("math", "Math"), ("typing", "Typing")]:
            tk.Radiobutton(challenge_frame, text=text, value=val, variable=self.challenge_var).pack(side="left")

        tk.Label(self, text="Rounds to solve").grid(row=6, column=0, sticky="w", **pad)
        self.rounds_var = tk.StringVar(value="2")
        tk.Spinbox(self, from_=1, to=5, width=5, textvariable=self.rounds_var).grid(row=6, column=1, sticky="w", **pad)

        tk.Label(self, text="Timeout before auto-fail (sec)").grid(row=7, column=0, sticky="w", **pad)
        self.timeout_var = tk.StringVar(value="120")
        tk.Entry(self, textvariable=self.timeout_var, width=10).grid(row=7, column=1, sticky="w", **pad)

        btn_frame = tk.Frame(self)
        btn_frame.grid(row=8, column=0, columnspan=2, pady=15)
        tk.Button(btn_frame, text="Save", width=10, command=self._save).pack(side="left", padx=5)
        tk.Button(btn_frame, text="Cancel", width=10, command=self.destroy).pack(side="left", padx=5)

    def _load(self, alarm: Alarm):
        self.hour_var.set(f"{alarm.hour:02d}")
        self.minute_var.set(f"{alarm.minute:02d}")
        self.label_var.set(alarm.label)
        self.stake_var.set(f"{alarm.stake:.2f}")
        for i, v in enumerate(self.day_vars):
            v.set(i in alarm.repeat_days)
        self.challenge_var.set(alarm.challenge_type)
        self.rounds_var.set(str(alarm.rounds))
        self.timeout_var.set(str(alarm.timeout_seconds))

    def _save(self):
        try:
            hour = int(self.hour_var.get())
            minute = int(self.minute_var.get())
            stake = float(self.stake_var.get())
            rounds = int(self.rounds_var.get())
            timeout_seconds = int(self.timeout_var.get())
        except ValueError:
            messagebox.showerror("Invalid input", "Please enter valid numbers.", parent=self)
            return

        if stake <= 0:
            messagebox.showerror("Invalid input", "Stake must be greater than 0.", parent=self)
            return

        repeat_days = [i for i, v in enumerate(self.day_vars) if v.get()]

        if self.existing:
            alarm = self.existing
            alarm.hour, alarm.minute = hour, minute
            alarm.label = self.label_var.get().strip()
            alarm.stake = stake
            alarm.repeat_days = repeat_days
            alarm.challenge_type = self.challenge_var.get()
            alarm.rounds = rounds
            alarm.timeout_seconds = timeout_seconds
            alarm.enabled = True
        else:
            alarm = Alarm.new(
                hour=hour, minute=minute, stake=stake, repeat_days=repeat_days,
                label=self.label_var.get().strip(), challenge_type=self.challenge_var.get(),
                rounds=rounds, timeout_seconds=timeout_seconds,
            )

        self.on_save(alarm)
        self.destroy()
