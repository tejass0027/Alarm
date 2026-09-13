package com.stakealarm.app.challenges

import kotlin.random.Random

interface Challenge {
    fun prompt(): String
    fun check(answer: String): Boolean
}

private val PHRASES = listOf(
    "the early bird catches the worm",
    "discipline beats motivation every single day",
    "get up and seize the day",
    "small steps every morning add up",
    "consistency is the real secret",
    "wake up and make it count",
)

class MathChallenge(difficulty: Int = 1) : Challenge {
    private var a: Int
    private var b: Int
    private val op: String
    private val answer: Int

    init {
        val span = 20 * difficulty.coerceAtLeast(1)
        var x = Random.nextInt(1, span + 1)
        var y = Random.nextInt(1, span + 1)
        val chosenOp = listOf("+", "-", "*").random()
        if (chosenOp == "-" && y > x) {
            val t = x; x = y; y = t
        }
        a = x
        b = y
        op = chosenOp
        answer = when (op) {
            "+" -> a + b
            "-" -> a - b
            else -> a * b
        }
    }

    override fun prompt() = "What is $a $op $b ?"
    override fun check(answer: String) = answer.trim().toIntOrNull() == this.answer
}

class TypingChallenge(@Suppress("UNUSED_PARAMETER") difficulty: Int = 1) : Challenge {
    private val phrase = PHRASES.random()
    override fun prompt() = "Type this exactly:\n\"$phrase\""
    override fun check(answer: String) = answer.trim().equals(phrase, ignoreCase = true)
}

fun createChallenge(challengeType: String = "random", difficulty: Int = 1): Challenge {
    val type = if (challengeType == "random") listOf("math", "typing").random() else challengeType
    return when (type) {
        "typing" -> TypingChallenge(difficulty)
        else -> MathChallenge(difficulty)
    }
}
