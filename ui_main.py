import tkinter as tk
from datetime import datetime
from tkinter import messagebox, simpledialog, ttk

from models import HistoryEntry
from scheduler import Scheduler
from ui_alarm_dialog import AlarmDialog
from ui_history import HistoryWindow
from ui_ringing import RingingWindow

DAY_LABELS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]


class MainWindow(tk.Tk):
    def __init__(self, storage, provider):
        super().__init__()
        self.storage = storage
        self.provider = provider
        self.alarms = storage.load_alarms()
        self._active_alarm_ids = set()

        self.title("Stake Alarm")
        self.geometry("560x420")

        self._build_ui()
        self._refresh_alarm_list()
        self._refresh_balance()

        self.scheduler = Scheduler(
            get_alarms=lambda: self.alarms,
            on_trigger=self._trigger_alarm,
            schedule_on_main=lambda fn: self.after(0, fn),
        )
        self.scheduler.start()
        self.protocol("WM_DELETE_WINDOW", self._on_close)

    def _build_ui(self):
        top = tk.Frame(self)
        top.pack(fill="x", padx=10, pady=10)

        self.balance_label = tk.Label(top, text="Balance: $0.00", font=("Segoe UI", 16, "bold"))
        self.balance_label.pack(side="left")

        tk.Button(top, text="Deposit", command=self._deposit).pack(side="right")

        columns = ("time", "label", "stake", "repeat", "status")
        self.tree = ttk.Treeview(self, columns=columns, show="headings", height=10)
        self.tree.heading("time", text="Time")
        self.tree.heading("label", text="Label")
        self.tree.heading("stake", text="Stake")
        self.tree.heading("repeat", text="Repeat")
        self.tree.heading("status", text="Status")
        self.tree.column("time", width=70, anchor="center")
        self.tree.column("label", width=140)
        self.tree.column("stake", width=70, anchor="e")
        self.tree.column("repeat", width=120)
        self.tree.column("status", width=80, anchor="center")
        self.tree.pack(fill="both", expand=True, padx=10, pady=(0, 10))

        btns = tk.Frame(self)
        btns.pack(fill="x", padx=10, pady=(0, 10))
        tk.Button(btns, text="Add alarm", command=self._add_alarm).pack(side="left")
        tk.Button(btns, text="Edit", command=self._edit_alarm).pack(side="left", padx=5)
        tk.Button(btns, text="Enable/Disable", command=self._toggle_alarm).pack(side="left", padx=5)
        tk.Button(btns, text="Delete", command=self._delete_alarm).pack(side="left", padx=5)
        tk.Button(btns, text="History", command=self._show_history).pack(side="right")

    def _refresh_balance(self):
        self.balance_label.config(text=f"Balance: ${self.provider.get_balance():.2f}")

    def _refresh_alarm_list(self):
        self.tree.delete(*self.tree.get_children())
        for alarm in self.alarms:
            repeat = ", ".join(DAY_LABELS[d] for d in sorted(alarm.repeat_days)) if alarm.repeat_days else "Once"
            status = "On" if alarm.enabled else "Off"
            self.tree.insert("", "end", iid=alarm.id, values=(
                f"{alarm.hour:02d}:{alarm.minute:02d}", alarm.label or "(unlabeled)",
                f"${alarm.stake:.2f}", repeat, status,
            ))

    def _selected_alarm(self):
        sel = self.tree.selection()
        if not sel:
            return None
        alarm_id = sel[0]
        return next((a for a in self.alarms if a.id == alarm_id), None)

    def _deposit(self):
        amount = simpledialog.askfloat("Deposit", "Amount to add to your virtual balance ($):", minvalue=0.01, parent=self)
        if amount:
            self.provider.deposit(amount)
            self._refresh_balance()

    def _add_alarm(self):
        AlarmDialog(self, on_save=self._save_new_alarm)

    def _save_new_alarm(self, alarm):
        self.alarms.append(alarm)
        self.storage.save_alarms(self.alarms)
        self._refresh_alarm_list()

    def _edit_alarm(self):
        alarm = self._selected_alarm()
        if not alarm:
            messagebox.showinfo("Edit alarm", "Select an alarm first.", parent=self)
            return
        AlarmDialog(self, on_save=lambda a: self._save_edit(), existing=alarm)

    def _save_edit(self):
        self.storage.save_alarms(self.alarms)
        self._refresh_alarm_list()

    def _toggle_alarm(self):
        alarm = self._selected_alarm()
        if not alarm:
            return
        alarm.enabled = not alarm.enabled
        self.storage.save_alarms(self.alarms)
        self._refresh_alarm_list()

    def _delete_alarm(self):
        alarm = self._selected_alarm()
        if not alarm:
            return
        if messagebox.askyesno("Delete alarm", "Delete this alarm?", parent=self):
            self.alarms = [a for a in self.alarms if a.id != alarm.id]
            self.storage.save_alarms(self.alarms)
            self._refresh_alarm_list()

    def _show_history(self):
        HistoryWindow(self, self.storage.load_history())

    def _trigger_alarm(self, alarm):
        if alarm.id in self._active_alarm_ids:
            return
        self._active_alarm_ids.add(alarm.id)

        alarm.last_triggered_date = datetime.now().date().isoformat()
        if not alarm.repeat_days:
            alarm.enabled = False
        self.storage.save_alarms(self.alarms)
        self._refresh_alarm_list()

        RingingWindow(self, alarm, on_resolved=self._on_alarm_resolved)

    def _on_alarm_resolved(self, alarm, result):
        self._active_alarm_ids.discard(alarm.id)

        if result == "solved":
            new_balance = self.provider.get_balance()
        else:
            new_balance = self.provider.deduct(alarm.stake)

        entry = HistoryEntry.new(
            alarm_id=alarm.id,
            timestamp=datetime.now().isoformat(timespec="seconds"),
            label=alarm.label,
            stake=alarm.stake,
            result=result,
            balance_after=new_balance,
        )
        self.storage.append_history(entry)
        self._refresh_balance()

        if result == "solved":
            messagebox.showinfo("Nice work", "Alarm solved — your stake is safe.", parent=self)
        else:
            messagebox.showwarning(
                "Stake lost",
                f"${alarm.stake:.2f} was deducted from your balance.",
                parent=self,
            )

    def _on_close(self):
        self.scheduler.stop()
        self.destroy()
