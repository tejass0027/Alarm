import threading
import time
from datetime import date, datetime
from typing import Callable, List

from models import Alarm


class Scheduler:
    """Polls alarms in a background thread and hands matches back to the
    Tkinter main thread via `schedule_on_main` (typically `root.after`)."""

    def __init__(self, get_alarms: Callable[[], List[Alarm]], on_trigger: Callable[[Alarm], None],
                 schedule_on_main: Callable[[Callable], None], poll_seconds: float = 2.0):
        self._get_alarms = get_alarms
        self._on_trigger = on_trigger
        self._schedule_on_main = schedule_on_main
        self._poll_seconds = poll_seconds
        self._stop = threading.Event()
        self._thread = None

    def start(self):
        self._thread = threading.Thread(target=self._run, daemon=True)
        self._thread.start()

    def stop(self):
        self._stop.set()

    def _run(self):
        while not self._stop.is_set():
            now = datetime.now()
            today = date.today().isoformat()
            for alarm in list(self._get_alarms()):
                if self._matches(alarm, now, today):
                    self._schedule_on_main(lambda a=alarm: self._on_trigger(a))
            time.sleep(self._poll_seconds)

    @staticmethod
    def _matches(alarm: Alarm, now: datetime, today: str) -> bool:
        if not alarm.enabled:
            return False
        if alarm.hour != now.hour or alarm.minute != now.minute:
            return False
        if alarm.last_triggered_date == today:
            return False
        if alarm.repeat_days and now.weekday() not in alarm.repeat_days:
            return False
        return True
