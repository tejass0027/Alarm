from abc import ABC, abstractmethod

from storage import Storage


class PaymentProvider(ABC):
    """Interface for wherever the staked money actually lives."""

    @abstractmethod
    def get_balance(self) -> float: ...

    @abstractmethod
    def deposit(self, amount: float) -> float: ...

    @abstractmethod
    def deduct(self, amount: float) -> float: ...


class LocalVirtualProvider(PaymentProvider):
    """Tracks a virtual balance in a local JSON file. No real money moves."""

    def __init__(self, storage: Storage):
        self._storage = storage

    def get_balance(self) -> float:
        return self._storage.load_balance()

    def deposit(self, amount: float) -> float:
        if amount <= 0:
            raise ValueError("Deposit amount must be positive")
        balance = self._storage.load_balance() + amount
        self._storage.save_balance(balance)
        return balance

    def deduct(self, amount: float) -> float:
        balance = max(0.0, self._storage.load_balance() - amount)
        self._storage.save_balance(balance)
        return balance


class RealPaymentProvider(PaymentProvider):
    """
    Placeholder for a future real-money backend (e.g. Stripe/a bank API).
    Wiring this up needs a payment processor account and API keys that only
    you can set up, so it's left unimplemented here on purpose.
    """

    def __init__(self, *args, **kwargs):
        raise NotImplementedError(
            "Real payment integration isn't implemented. Swap LocalVirtualProvider "
            "for a real implementation of PaymentProvider once you've set up a "
            "payment processor."
        )

    def get_balance(self) -> float:
        raise NotImplementedError

    def deposit(self, amount: float) -> float:
        raise NotImplementedError

    def deduct(self, amount: float) -> float:
        raise NotImplementedError
