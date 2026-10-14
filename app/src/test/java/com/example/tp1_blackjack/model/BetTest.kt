package com.example.tp1_blackjack.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BetTest {
    @Test
    fun boundariesOneAndOneHundred_areValid() {
        assertEquals(1, Bet.from(1)?.amount)
        assertEquals(100, Bet.from(100)?.amount)
    }

    @Test
    fun valuesOutsideBoundaries_areRejected() {
        assertNull(Bet.from(0))
        assertNull(Bet.from(101))
    }
}
