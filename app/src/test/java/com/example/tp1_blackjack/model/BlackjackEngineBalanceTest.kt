package com.example.tp1_blackjack.model

import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackEngineBalanceTest {
    @Test
    fun normalWin_hasNetGainEqualToBet() {
        val engine = engine(
            Rank.TEN,
            Rank.TEN,
            Rank.NINE,
            Rank.SIX,
            Rank.SIX,
        )
        engine.selectBet(100)

        val duringRound = engine.startRound()
        val completed = engine.stand()

        assertEquals(900, duringRound.balance)
        assertEquals(1_100, completed.balance)
    }

    @Test
    fun loss_hasNetLossEqualToBet() {
        val engine = engine(Rank.TEN, Rank.TEN, Rank.SEVEN, Rank.NINE)
        engine.selectBet(100)
        engine.startRound()

        val completed = engine.stand()

        assertEquals(900, completed.balance)
    }

    @Test
    fun push_returnsCommittedBet() {
        val engine = engine(Rank.TEN, Rank.TEN, Rank.SEVEN, Rank.SEVEN)
        engine.selectBet(100)
        engine.startRound()

        val completed = engine.stand()

        assertEquals(1_000, completed.balance)
    }

    @Test
    fun naturalBlackjack_hasNetGainOfTwiceTheBet() {
        val engine = engine(Rank.ACE, Rank.TEN, Rank.KING, Rank.NINE)
        engine.selectBet(100)

        val completed = engine.startRound()

        assertEquals(GamePhase.COMPLETE, completed.phase)
        assertEquals(1_200, completed.balance)
    }

    @Test
    fun insufficientFunds_blockRoundUntilAffordableBetSelected() {
        val engine = BlackjackEngine(
            cardSource = FixedCardSource(
                card(Rank.TEN),
                card(Rank.FIVE),
                card(Rank.SEVEN),
                card(Rank.SIX),
            ),
            initialBalance = 40,
            initialBet = 50,
        )

        assertTrue(GameAction.START_ROUND !in engine.state.availableActions)
        assertThrows(IllegalStateException::class.java) { engine.startRound() }

        engine.selectBet(40)
        assertTrue(GameAction.START_ROUND in engine.state.availableActions)
        assertEquals(0, engine.startRound().balance)
    }

    @Test
    fun completedRound_cannotBeSettledTwice() {
        val engine = engine(Rank.TEN, Rank.TEN, Rank.SEVEN, Rank.NINE)
        engine.selectBet(100)
        engine.startRound()
        val completed = engine.stand()

        assertThrows(IllegalStateException::class.java) { engine.stand() }
        assertEquals(completed.balance, engine.state.balance)
    }

    private fun engine(vararg ranks: Rank): BlackjackEngine = BlackjackEngine(
        cardSource = FixedCardSource(*ranks.map(::card).toTypedArray()),
    )

    private fun card(rank: Rank): Card = Card(rank, Suit.CLUBS)

    private class FixedCardSource(vararg cards: Card) : CardSource {
        private val cards = ArrayDeque(cards.toList())

        override val remainingCount: Int
            get() = cards.size

        override fun draw(): Card = cards.removeFirst()

        override fun prepareForNewRound(): Boolean = false
    }
}
