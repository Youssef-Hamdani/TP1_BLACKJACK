package com.example.tp1_blackjack.model

import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoeTest {
    @Test
    fun oneDeck_containsFiftyTwoUniqueCards() {
        val shoe = Shoe(deckCount = 1, random = Random(1))

        val cards = List(52) { shoe.draw() }

        assertEquals(52, cards.toSet().size)
        assertEquals(0, shoe.remainingCount)
    }

    @Test
    fun twoDecks_distinguishPhysicalCopies() {
        val shoe = Shoe(deckCount = 2, random = Random(2))

        val cards = List(104) { shoe.draw() }

        assertEquals(104, cards.toSet().size)
    }

    @Test
    fun everySupportedDeckCount_containsExactlyFiftyTwoCardsPerDeck() {
        Shoe.SUPPORTED_DECK_COUNTS.forEach { deckCount ->
            val shoe = Shoe(deckCount = deckCount, random = Random(deckCount))
            val cards = List(52 * deckCount) { shoe.draw() }

            assertEquals(52 * deckCount, cards.size)
            assertEquals(52 * deckCount, cards.toSet().size)
            assertEquals(0, shoe.remainingCount)
        }
    }

    @Test
    fun drawnCard_cannotReappearBeforeRenewal() {
        val shoe = Shoe(random = Random(3))
        val drawnCards = mutableSetOf<Card>()

        repeat(52) {
            assertTrue(drawnCards.add(shoe.draw()))
        }

        assertThrows(IllegalStateException::class.java) { shoe.draw() }
    }

    @Test
    fun exactlyTwentyCards_doesNotRenewShoe() {
        val shoe = Shoe(random = Random(4))
        repeat(32) { shoe.draw() }

        assertFalse(shoe.prepareForNewRound())
        assertEquals(20, shoe.remainingCount)
    }

    @Test
    fun fewerThanTwentyCards_renewsBeforeRound() {
        val shoe = Shoe(random = Random(5))
        repeat(33) { shoe.draw() }

        assertTrue(shoe.prepareForNewRound())
        assertEquals(52, shoe.remainingCount)
    }

    @Test
    fun unsupportedDeckCount_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Shoe(deckCount = 3)
        }
    }
}
