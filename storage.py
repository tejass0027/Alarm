import json
import os
from pathlib import Path
from typing import List

from models import Alarm, HistoryEntry

DATA_DIR = Path(__file__).resolve().parent / "data"
ALARMS_FILE = DATA_DIR / "alarms.json"
BALANCE_FILE = DATA_DIR / "balance.json"
HISTORY_FILE = DATA_DIR / "history.json"


class Storage:
    def __init__(self):
        DATA_DIR.mkdir(exist_ok=True)

    def load_alarms(self) -> List[Alarm]:
        if not ALARMS_FILE.exists():
            return []
        with open(ALARMS_FILE, "r", encoding="utf-8") as f:
            raw = json.load(f)
        return [Alarm.from_dict(d) for d in raw]

    def save_alarms(self, alarms: List[Alarm]):
        with open(ALARMS_FILE, "w", encoding="utf-8") as f:
            json.dump([a.to_dict() for a in alarms], f, indent=2)

    def load_balance(self) -> float:
        if not BALANCE_FILE.exists():
            return 0.0
        with open(BALANCE_FILE, "r", encoding="utf-8") as f:
            return json.load(f).get("balance", 0.0)

    def save_balance(self, balance: float):
        with open(BALANCE_FILE, "w", encoding="utf-8") as f:
            json.dump({"balance": round(balance, 2)}, f, indent=2)

    def load_history(self) -> List[HistoryEntry]:
        if not HISTORY_FILE.exists():
            return []
        with open(HISTORY_FILE, "r", encoding="utf-8") as f:
            raw = json.load(f)
        return [HistoryEntry.from_dict(d) for d in raw]

    def save_history(self, history: List[HistoryEntry]):
        with open(HISTORY_FILE, "w", encoding="utf-8") as f:
            json.dump([h.to_dict() for h in history], f, indent=2)

    def append_history(self, entry: HistoryEntry):
        history = self.load_history()
        history.append(entry)
        self.save_history(history)
