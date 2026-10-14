package com.example.tp1_blackjack.model

@JvmInline
value class Bet private constructor(val amount: Int) {
    companion object {
        const val MIN_AMOUNT = 1
        const val MAX_AMOUNT = 100
        const val DEFAULT_AMOUNT = 50

        val Default = Bet(DEFAULT_AMOUNT)

        fun from(amount: Int): Bet? = amount
            .takeIf { it in MIN_AMOUNT..MAX_AMOUNT }
            ?.let(::Bet)
    }
}
