import random
from abc import ABC, abstractmethod

PHRASES = [
    "the early bird catches the worm",
    "discipline beats motivation every single day",
    "get up and seize the day",
    "small steps every morning add up",
    "consistency is the real secret",
    "wake up and make it count",
]


class Challenge(ABC):
    kind = "base"

    @abstractmethod
    def prompt(self) -> str: ...

    @abstractmethod
    def check(self, answer: str) -> bool: ...


class MathChallenge(Challenge):
    kind = "math"

    def __init__(self, difficulty: int = 1):
        span = 20 * max(1, difficulty)
        self.a = random.randint(1, span)
        self.b = random.randint(1, span)
        self.op = random.choice(["+", "-", "*"])
        if self.op == "-" and self.b > self.a:
            self.a, self.b = self.b, self.a
        if self.op == "+":
            self.answer = self.a + self.b
        elif self.op == "-":
            self.answer = self.a - self.b
        else:
            self.answer = self.a * self.b

    def prompt(self) -> str:
        return f"What is {self.a} {self.op} {self.b} ?"

    def check(self, answer: str) -> bool:
        try:
            return int(answer.strip()) == self.answer
        except ValueError:
            return False


class TypingChallenge(Challenge):
    kind = "typing"

    def __init__(self, difficulty: int = 1):
        self.phrase = random.choice(PHRASES)

    def prompt(self) -> str:
        return f'Type this exactly:\n"{self.phrase}"'

    def check(self, answer: str) -> bool:
        return answer.strip().lower() == self.phrase.lower()


CHALLENGE_TYPES = {
    "math": MathChallenge,
    "typing": TypingChallenge,
}


def create_challenge(challenge_type: str = "random", difficulty: int = 1) -> Challenge:
    if challenge_type not in CHALLENGE_TYPES:
        challenge_type = random.choice(list(CHALLENGE_TYPES.keys()))
    return CHALLENGE_TYPES[challenge_type](difficulty=difficulty)
