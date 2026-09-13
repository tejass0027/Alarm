from dataclasses import dataclass, field, asdict
from typing import List
import uuid


@dataclass
class Alarm:
    id: str
    hour: int
    minute: int
    stake: float
    repeat_days: List[int] = field(default_factory=list)  # 0=Mon .. 6=Sun; empty = one-time
    label: str = ""
    enabled: bool = True
    challenge_type: str = "random"  # "math", "typing", or "random"
    rounds: int = 2
    timeout_seconds: int = 120
    last_triggered_date: str = ""  # ISO date string, prevents re-firing within the same minute

    @staticmethod
    def new(hour, minute, stake, repeat_days=None, label="", challenge_type="random",
            rounds=2, timeout_seconds=120):
        return Alarm(
            id=str(uuid.uuid4()),
            hour=hour,
            minute=minute,
            stake=stake,
            repeat_days=repeat_days or [],
            label=label,
            enabled=True,
            challenge_type=challenge_type,
            rounds=rounds,
            timeout_seconds=timeout_seconds,
        )

    def to_dict(self):
        return asdict(self)

    @staticmethod
    def from_dict(d):
        return Alarm(**d)


@dataclass
class HistoryEntry:
    id: str
    alarm_id: str
    timestamp: str
    label: str
    stake: float
    result: str  # "solved" | "failed" | "gave_up"
    balance_after: float

    @staticmethod
    def new(alarm_id, timestamp, label, stake, result, balance_after):
        return HistoryEntry(
            id=str(uuid.uuid4()),
            alarm_id=alarm_id,
            timestamp=timestamp,
            label=label,
            stake=stake,
            result=result,
            balance_after=balance_after,
        )

    def to_dict(self):
        return asdict(self)

    @staticmethod
    def from_dict(d):
        return HistoryEntry(**d)
