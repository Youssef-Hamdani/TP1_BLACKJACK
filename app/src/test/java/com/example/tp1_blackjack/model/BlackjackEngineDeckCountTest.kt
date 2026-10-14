package com.example.tp1_blackjack.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class BlackjackEngineDeckCountTest {
    @Test
    fun initialShoeDeckCount_isProjectedIntoState() {
        val engine = BlackjackEngine(cardSource = Shoe(deckCount = 4))

        assertEquals(4, engine.state.deckCount)
    }

    @Test
    fun selectingSupportedCountBetweenRounds_replacesShoeAndUsesItNextRound() {
        val replacement = FixedCardSource(Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX)
        var requestedDeckCount: Int? = null
        val engine = BlackjackEngine(
            cardSource = FixedCardSource(Rank.TWO),
            cardSourceFactory = { deckCount ->
                requestedDeckCount = deckCount
                replacement
            },
        )

        val selected = engine.selectDeckCount(4)

        assertEquals(4, requestedDeckCount)
        assertEquals(4, selected.deckCount)
        val round = engine.startRound()
        assertEquals(listOf(Rank.TEN, Rank.SEVEN), round.playerHands.activeHand.hand.cards.map { it.rank })
        assertEquals(4, round.deckCount)
    }

    @Test
    fun selectingCurrentCount_keepsExistingShoe() {
        val original = FixedCardSource(Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX)
        var factoryCalls = 0
        val engine = BlackjackEngine(
            cardSource = original,
            cardSourceFactory = {
                factoryCalls++
                FixedCardSource(Rank.TWO)
            },
        )

        val unchanged = engine.selectDeckCount(1)

        assertSame(engine.state, unchanged)
        assertEquals(0, factoryCalls)
        engine.startRound()
        assertEquals(4, original.drawCount)
    }

    @Test
    fun changingCountDuringRound_isRejectedWithoutReplacingShoe() {
        val original = FixedCardSource(Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX)
        var factoryCalls = 0
        val engine = BlackjackEngine(
            cardSource = original,
            cardSourceFactory = {
                factoryCalls++
                FixedCardSource(Rank.TWO)
            },
        )
        engine.startRound()
        val before = engine.state

        assertThrows(IllegalStateException::class.java) { engine.selectDeckCount(2) }

        assertSame(before, engine.state)
        assertEquals(0, factoryCalls)
    }

    @Test
    fun unsupportedCount_isRejectedBetweenRounds() {
        val engine = BlackjackEngine(cardSource = FixedCardSource(Rank.TWO))

        assertThrows(IllegalArgumentException::class.java) { engine.selectDeckCount(3) }
        assertEquals(1, engine.state.deckCount)
    }

    private class FixedCardSource(vararg ranks: Rank) : CardSource {
        private val cards = ArrayDeque(ranks.map { rank -> Card(rank, Suit.CLUBS) })
        var drawCount = 0
            private set

        override val remainingCount: Int get() = cards.size
        override fun prepareForNewRound(): Boolean = false
        override fun draw(): Card {
            drawCount++
            return cards.removeFirst()
        }
    }
}
