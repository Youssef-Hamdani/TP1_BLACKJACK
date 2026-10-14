package com.example.tp1_blackjack.model

object PayoutCalculator {
    fun grossPayout(
        bet: Bet,
        outcome: HandOutcome,
    ): Int = when (outcome) {
        HandOutcome.DEALER_WIN -> 0
        HandOutcome.PUSH -> bet.amount
        HandOutcome.PLAYER_WIN -> bet.amount * 2
        HandOutcome.PLAYER_BLACKJACK -> bet.amount * 3
    }
}
