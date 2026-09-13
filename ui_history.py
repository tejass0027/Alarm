import tkinter as tk
from tkinter import ttk


class HistoryWindow(tk.Toplevel):
    def __init__(self, master, history):
        super().__init__(master)
        self.title("Alarm history")
        self.geometry("640x360")
        self.transient(master)

        columns = ("timestamp", "label", "stake", "result", "balance_after")
        tree = ttk.Treeview(self, columns=columns, show="headings")
        tree.heading("timestamp", text="When")
        tree.heading("label", text="Alarm")
        tree.heading("stake", text="Stake")
        tree.heading("result", text="Result")
        tree.heading("balance_after", text="Balance after")
        tree.column("timestamp", width=150)
        tree.column("label", width=140)
        tree.column("stake", width=80, anchor="e")
        tree.column("result", width=90)
        tree.column("balance_after", width=100, anchor="e")
        tree.pack(fill="both", expand=True, padx=10, pady=10)

        for entry in sorted(history, key=lambda h: h.timestamp, reverse=True):
            tree.insert("", "end", values=(
                entry.timestamp.replace("T", " ")[:19],
                entry.label or "(unlabeled)",
                f"${entry.stake:.2f}",
                entry.result,
                f"${entry.balance_after:.2f}",
            ))

        if not history:
            tk.Label(self, text="No alarms have fired yet.", fg="#666").pack(pady=20)
