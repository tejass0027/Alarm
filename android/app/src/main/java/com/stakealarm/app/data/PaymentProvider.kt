package com.stakealarm.app.data

interface PaymentProvider {
    fun getBalance(): Double
    fun deposit(amount: Double): Double
    fun deduct(amount: Double): Double
}

/** Tracks a virtual balance in a local JSON file. No real money moves. */
class LocalVirtualProvider(private val repository: Repository) : PaymentProvider {
    override fun getBalance(): Double = repository.getBalance()

    override fun deposit(amount: Double): Double {
        require(amount > 0) { "Deposit amount must be positive" }
        val balance = repository.getBalance() + amount
        repository.saveBalance(balance)
        return balance
    }

    override fun deduct(amount: Double): Double {
        val balance = (repository.getBalance() - amount).coerceAtLeast(0.0)
        repository.saveBalance(balance)
        return balance
    }
}

/**
 * Placeholder for a future real-money backend (e.g. Stripe). Wiring this up
 * needs a payment processor account only you can set up, so it's left
 * unimplemented on purpose — swap LocalVirtualProvider for a real
 * implementation of PaymentProvider once that's ready.
 */
class RealPaymentProvider : PaymentProvider {
    override fun getBalance(): Double = throw NotImplementedError()
    override fun deposit(amount: Double): Double = throw NotImplementedError()
    override fun deduct(amount: Double): Double = throw NotImplementedError()
}
