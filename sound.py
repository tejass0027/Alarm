import threading
import time

try:
    import winsound
    HAS_WINSOUND = True
except ImportError:
    HAS_WINSOUND = False


class AlarmSound:
    def __init__(self):
        self._stop = threading.Event()
        self._thread = None

    def start(self):
        self._stop.clear()
        self._thread = threading.Thread(target=self._loop, daemon=True)
        self._thread.start()

    def _loop(self):
        while not self._stop.is_set():
            if HAS_WINSOUND:
                winsound.Beep(1200, 350)
            else:
                time.sleep(0.35)
            time.sleep(0.15)

    def stop(self):
        self._stop.set()
