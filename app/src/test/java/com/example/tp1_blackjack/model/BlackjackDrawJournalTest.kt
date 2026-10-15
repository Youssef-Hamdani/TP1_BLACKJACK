package com.example.tp1_blackjack.model

import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*

import org.junit.Assert.*
import org.junit.Test

class BlackjackDrawJournalTest {
    @Test
    fun distributionAndHit_keepDrawOrder_thenBustRevealsDealer() {
        val engine = engine(Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX, Rank.KING)
        engine.startRound()
        val initial = engine.state.drawnCards
        assertEquals(listOf(CardOwner.PLAYER, CardOwner.DEALER, CardOwner.PLAYER, CardOwner.DEALER), initial.map { it.owner })
        assertEquals(listOf(true, false, true, true), initial.map { it.isObserved })
        engine.hit()
        assertEquals((0..5).toList(), engine.state.drawnCards.map { it.drawOrder })
        assertEquals(Rank.KING, engine.state.drawnCards[4].card.rank)
        assertTrue(engine.state.drawnCards[1].isObserved)
        assertEquals(6, engine.state.drawnCards.count { it.isObserved })
        assertEquals(4, initial.size) // The earlier immutable snapshot stays unchanged.
        engine.startRound()
        assertEquals((0..3).toList(), engine.state.drawnCards.map { it.drawOrder })
    }

    @Test
    fun splitMovesExistingCard_andDealerDrawsFollowBothPlayerHands() {
        val engine = engine(Rank.EIGHT, Rank.FIVE, Rank.EIGHT, Rank.SIX,
            Rank.TEN, Rank.NINE, Rank.SIX)
        engine.startRound()
        engine.split()
        assertEquals(listOf(0, 1, 0, 1), engine.state.drawnCards.filter { it.owner == CardOwner.PLAYER }.map { it.handIndex })
        engine.stand()
        assertFalse(engine.state.drawnCards[1].isObserved)
        engine.stand()
        assertEquals((0..6).toList(), engine.state.drawnCards.map { it.drawOrder })
        assertEquals(CardOwner.DEALER, engine.state.drawnCards.last().owner)
        assertTrue(engine.state.drawnCards.all { it.isObserved })
    }

    @Test
    fun bothSplitHandsBust_revealHoleCardAndKeepBothLosses() {
        val engine = engine(Rank.EIGHT, Rank.FIVE, Rank.EIGHT, Rank.SIX,
            Rank.KING, Rank.KING, Rank.KING, Rank.KING)
        engine.startRound()
        engine.split()
        engine.hit()
        engine.hit()
        assertEquals(GamePhase.COMPLETE, engine.state.phase)
        assertEquals(9, engine.state.drawnCards.size)
        assertEquals(9, engine.state.drawnCards.count { it.isObserved })
        assertTrue(engine.state.drawnCards[1].isObserved)
        assertEquals(listOf(HandOutcome.DEALER_WIN, HandOutcome.DEALER_WIN), engine.state.outcomes)
    }

    @Test
    fun naturalRevealsInitialHoleCard_withoutExtraDraw() {
        val engine = engine(Rank.ACE, Rank.FIVE, Rank.KING, Rank.SIX)
        engine.startRound()
        assertEquals(GamePhase.COMPLETE, engine.state.phase)
        assertEquals(4, engine.state.drawnCards.size)
        assertTrue(engine.state.drawnCards.all { it.isObserved })
    }

    private fun engine(vararg ranks: Rank) = BlackjackEngine(object : CardSource {
        private val cards = ArrayDeque((ranks.toList() + listOf(
            Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX,
            Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX,
        ))
            .map { Card(it, Suit.CLUBS) })
        override val remainingCount get() = cards.size
        override fun prepareForNewRound() = false
        override fun draw() = cards.removeFirst()
    })
}
