import tkinter as tk
from tkinter import messagebox

from challenges import create_challenge
from sound import AlarmSound


class RingingWindow(tk.Toplevel):
    def __init__(self, master, alarm, on_resolved):
        """on_resolved(alarm, result) where result is 'solved' | 'failed' | 'gave_up'."""
        super().__init__(master)
        self.alarm = alarm
        self.on_resolved = on_resolved
        self._resolved = False
        self._round = 0
        self._challenge = None

        self.title("Wake up!")
        self.attributes("-topmost", True)
        self.attributes("-fullscreen", True)
        self.configure(bg="#1a1a2e")
        self.protocol("WM_DELETE_WINDOW", self._give_up)
        self.bind("<Escape>", lambda e: None)

        self._build_ui()

        self.sound = AlarmSound()
        self.sound.start()

        self._timeout_ms = max(10, alarm.timeout_seconds) * 1000
        self._timeout_id = self.after(self._timeout_ms, self._on_timeout)

        self._next_round()
        self.focus_force()

    def _build_ui(self):
        self.header = tk.Label(
            self, text=f"ALARM: {self.alarm.label or 'Wake up'}",
            font=("Segoe UI", 28, "bold"), fg="#f1f1f1", bg="#1a1a2e",
        )
        self.header.pack(pady=(60, 10))

        self.stake_label = tk.Label(
            self, text=f"${self.alarm.stake:.2f} is staked on this alarm",
            font=("Segoe UI", 14), fg="#ff6b6b", bg="#1a1a2e",
        )
        self.stake_label.pack(pady=(0, 30))

        self.round_label = tk.Label(self, text="", font=("Segoe UI", 12), fg="#aaaaaa", bg="#1a1a2e")
        self.round_label.pack()

        self.prompt_label = tk.Label(
            self, text="", font=("Segoe UI", 20), fg="#f1f1f1", bg="#1a1a2e", wraplength=800, justify="center",
        )
        self.prompt_label.pack(pady=30)

        self.answer_var = tk.StringVar()
        self.entry = tk.Entry(self, textvariable=self.answer_var, font=("Segoe UI", 16), width=40, justify="center")
        self.entry.pack(pady=10)
        self.entry.bind("<Return>", lambda e: self._submit())

        self.feedback_label = tk.Label(self, text="", font=("Segoe UI", 12, "bold"), fg="#ffd93d", bg="#1a1a2e")
        self.feedback_label.pack(pady=10)

        submit_btn = tk.Button(self, text="Submit", font=("Segoe UI", 12), command=self._submit, width=12)
        submit_btn.pack(pady=10)

        give_up_btn = tk.Button(
            self, text="Give up (lose stake)", font=("Segoe UI", 10), fg="#ff6b6b", command=self._give_up,
        )
        give_up_btn.pack(pady=(30, 10))

    def _next_round(self):
        self._round += 1
        self._challenge = create_challenge(self.alarm.challenge_type)
        self.round_label.config(text=f"Round {self._round} of {self.alarm.rounds}")
        self.prompt_label.config(text=self._challenge.prompt())
        self.answer_var.set("")
        self.feedback_label.config(text="")
        self.entry.focus_set()

    def _submit(self):
        if self._resolved:
            return
        if self._challenge.check(self.answer_var.get()):
            if self._round >= self.alarm.rounds:
                self._resolve("solved")
            else:
                self.feedback_label.config(text="Correct! Next one...", fg="#6bff8f")
                self.after(500, self._next_round)
        else:
            self.feedback_label.config(text="Not quite — try again.", fg="#ffd93d")
            self.answer_var.set("")

    def _give_up(self):
        if self._resolved:
            return
        if messagebox.askyesno(
            "Give up?",
            f"Give up and lose ${self.alarm.stake:.2f}?",
            parent=self,
        ):
            self._resolve("gave_up")

    def _on_timeout(self):
        if self._resolved:
            return
        self._resolve("failed")

    def _resolve(self, result):
        self._resolved = True
        self.sound.stop()
        try:
            self.after_cancel(self._timeout_id)
        except Exception:
            pass
        self.destroy()
        self.on_resolved(self.alarm, result)
