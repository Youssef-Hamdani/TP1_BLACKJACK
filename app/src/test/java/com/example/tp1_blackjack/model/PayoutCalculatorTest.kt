package com.example.tp1_blackjack.model

import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*

import org.junit.Assert.assertEquals
import org.junit.Test

class PayoutCalculatorTest {
    private val bet = requireNotNull(Bet.from(100))

    @Test
    fun grossPayouts_includeReturnedStake() {
        assertEquals(0, PayoutCalculator.grossPayout(bet, HandOutcome.DEALER_WIN))
        assertEquals(100, PayoutCalculator.grossPayout(bet, HandOutcome.PUSH))
        assertEquals(200, PayoutCalculator.grossPayout(bet, HandOutcome.PLAYER_WIN))
        assertEquals(300, PayoutCalculator.grossPayout(bet, HandOutcome.PLAYER_BLACKJACK))
    }
}
