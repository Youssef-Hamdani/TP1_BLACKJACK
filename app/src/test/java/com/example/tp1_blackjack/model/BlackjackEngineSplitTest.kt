package com.example.tp1_blackjack.model

import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackEngineSplitTest {
    @Test
    fun split_separatesOriginalCardsDealsInOrderAndCommitsEqualBet() {
        val source = FixedCardSource(Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN, Rank.TWO, Rank.THREE)
        val engine = BlackjackEngine(source, initialBet = 75)
        val original = engine.startRound()
        assertTrue(GameAction.SPLIT in original.availableActions)

        val state = engine.split()

        assertEquals(listOf(Rank.EIGHT, Rank.TWO), state.playerHands.hands[0].hand.cards.map { it.rank })
        assertEquals(listOf(Rank.EIGHT, Rank.THREE), state.playerHands.hands[1].hand.cards.map { it.rank })
        assertEquals(original.playerHands.activeHand.hand.cards[0], state.playerHands.hands[0].hand.cards[0])
        assertEquals(original.playerHands.activeHand.hand.cards[1], state.playerHands.hands[1].hand.cards[0])
        assertTrue(state.playerHands.hands.all { it.isFromSplit && it.status == PlayerHandStatus.ACTIVE })
        assertEquals(0, state.playerHands.activeHandIndex)
        assertEquals(listOf(75, 75), state.committedBets.map { it.amount })
        assertEquals(850, state.balance)
        assertEquals(original.dealerHand, state.dealerHand)
        assertFalse(state.isDealerHoleCardRevealed)
        assertFalse(state.isRoundSettled)
        assertEquals(6, source.drawCount)
        // The old snapshot stays intact; only the engine's current state changes.
        assertEquals(1, original.playerHands.hands.size)
        assertEquals(925, original.balance)
    }

    @Test
    fun sameBlackjackValueButDifferentRanks_cannotSplit() {
        val source = FixedCardSource(Rank.TEN, Rank.TEN, Rank.KING, Rank.SEVEN)
        val engine = BlackjackEngine(source)
        engine.startRound()

        assertSplitRejectedWithoutChanges(engine, source)
    }

    @Test
    fun insufficientRemainingBalance_cannotSplit() {
        val source = FixedCardSource(Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN)
        val engine = BlackjackEngine(source, initialBalance = 99)
        engine.startRound()

        assertSplitRejectedWithoutChanges(engine, source)
    }

    @Test
    fun exactlyEnoughBalance_canSplitAndReachZero() {
        val source = FixedCardSource(Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN, Rank.TWO, Rank.THREE)
        val engine = BlackjackEngine(source, initialBalance = 100)
        engine.startRound()

        val state = engine.split()

        assertEquals(0, state.balance)
        assertEquals(setOf(GameAction.HIT, GameAction.STAND), state.availableActions)
    }

    @Test
    fun afterHit_cannotSplitOriginalPair() {
        val source = FixedCardSource(Rank.TWO, Rank.TEN, Rank.TWO, Rank.SEVEN, Rank.TWO)
        val engine = BlackjackEngine(source)
        engine.startRound()
        engine.hit()

        assertSplitRejectedWithoutChanges(engine, source)
    }

    @Test
    fun splitPair_cannotBeSplitAgainOnEitherHand() {
        val source = FixedCardSource(Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN, Rank.EIGHT, Rank.EIGHT)
        val engine = BlackjackEngine(source)
        engine.startRound()
        engine.split()

        assertSplitRejectedWithoutChanges(engine, source)
        engine.stand()
        assertEquals(1, engine.state.playerHands.activeHandIndex)
        assertSplitRejectedWithoutChanges(engine, source)
    }

    @Test
    fun handsPlaySuccessively_thenOneDealerTurnSettlesMixedResultsOnce() {
        // First hand: 8+2+8=18. Second: 8+3+6=17. Dealer: 10+6+2=18.
        val source = FixedCardSource(
            Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SIX,
            Rank.TWO, Rank.THREE, Rank.EIGHT, Rank.SIX, Rank.TWO,
        )
        val engine = BlackjackEngine(source)
        engine.startRound()
        engine.split()
        val untouchedSecond = engine.state.playerHands.hands[1]
        engine.hit()
        assertEquals(untouchedSecond, engine.state.playerHands.hands[1])

        val betweenHands = engine.stand()

        assertEquals(GamePhase.PLAYER_TURN, betweenHands.phase)
        assertEquals(1, betweenHands.playerHands.activeHandIndex)
        assertEquals(PlayerHandStatus.STOOD, betweenHands.playerHands.hands[0].status)
        assertEquals(2, betweenHands.dealerHand.cards.size)
        assertFalse(betweenHands.isDealerHoleCardRevealed)
        assertTrue(betweenHands.outcomes.isEmpty())
        assertEquals(900, betweenHands.balance)
        val finishedFirst = betweenHands.playerHands.hands[0]

        engine.hit()
        assertEquals(finishedFirst, engine.state.playerHands.hands[0])
        val completed = engine.stand()

        assertEquals(GamePhase.COMPLETE, completed.phase)
        assertEquals(listOf(HandOutcome.PUSH, HandOutcome.DEALER_WIN), completed.outcomes)
        assertEquals(18, completed.dealerHand.score)
        assertEquals(3, completed.dealerHand.cards.size)
        assertTrue(completed.isDealerHoleCardRevealed)
        assertTrue(completed.isRoundSettled)
        assertEquals(950, completed.balance)
        assertEquals(9, source.drawCount)
        assertThrows(IllegalStateException::class.java) { engine.stand() }
        assertThrows(IllegalStateException::class.java) { engine.hit() }
        assertSplitRejectedWithoutChanges(engine, source)
        assertEquals(completed, engine.state)
    }

    @Test
    fun firstHandBust_advancesToSecondWithoutPlayingDealer() {
        val source = FixedCardSource(
            Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN,
            Rank.TEN, Rank.TEN, Rank.FIVE,
        )
        val engine = BlackjackEngine(source)
        engine.startRound()
        engine.split()

        val afterBust = engine.hit()

        assertEquals(1, afterBust.playerHands.activeHandIndex)
        assertEquals(PlayerHandStatus.BUST, afterBust.playerHands.hands[0].status)
        assertEquals(GamePhase.PLAYER_TURN, afterBust.phase)
        assertFalse(afterBust.isDealerHoleCardRevealed)
        assertEquals(900, afterBust.balance)

        val completed = engine.stand()
        assertEquals(listOf(HandOutcome.DEALER_WIN, HandOutcome.PLAYER_WIN), completed.outcomes)
        assertTrue(completed.isDealerHoleCardRevealed)
        assertEquals(1_000, completed.balance)
    }

    @Test
    fun repeatedHitsBustFirstFiveHand_secondHandRemainsPlayableAndSettlesIndependently() {
        // Split hands: 5+4 and 5+6. Main 1 then receives 5, 5, 10 and busts at 29.
        val source = FixedCardSource(
            Rank.FIVE, Rank.TEN, Rank.FIVE, Rank.SEVEN,
            Rank.FOUR, Rank.SIX, Rank.FIVE, Rank.FIVE, Rank.TEN, Rank.KING,
        )
        val engine = BlackjackEngine(source)
        engine.startRound()
        engine.split()

        engine.hit()
        engine.hit()
        val afterFirstHandBust = engine.hit()

        assertEquals(GamePhase.PLAYER_TURN, afterFirstHandBust.phase)
        assertEquals(1, afterFirstHandBust.playerHands.activeHandIndex)
        assertEquals(29, afterFirstHandBust.playerHands.hands[0].hand.score)
        assertEquals(PlayerHandStatus.BUST, afterFirstHandBust.playerHands.hands[0].status)
        assertEquals(11, afterFirstHandBust.playerHands.hands[1].hand.score)
        assertEquals(PlayerHandStatus.ACTIVE, afterFirstHandBust.playerHands.hands[1].status)
        assertEquals(setOf(GameAction.HIT, GameAction.STAND), afterFirstHandBust.availableActions)
        assertTrue(afterFirstHandBust.outcomes.isEmpty())
        assertFalse(afterFirstHandBust.isDealerHoleCardRevealed)
        assertEquals(2, afterFirstHandBust.dealerHand.cards.size)

        val secondHandAtTwentyOne = engine.hit()
        assertEquals(21, secondHandAtTwentyOne.playerHands.activeHand.hand.score)
        assertEquals(GamePhase.PLAYER_TURN, secondHandAtTwentyOne.phase)

        val completed = engine.stand()
        assertEquals(GamePhase.COMPLETE, completed.phase)
        assertEquals(listOf(HandOutcome.DEALER_WIN, HandOutcome.PLAYER_WIN), completed.outcomes)
        assertEquals(1_000, completed.balance)
    }

    @Test
    fun bothHandsBust_dealerRevealsAndDrawsAsRequiredByAssignment() {
        val source = FixedCardSource(
            Rank.EIGHT, Rank.FIVE, Rank.EIGHT, Rank.SIX,
            Rank.TEN, Rank.TEN, Rank.FIVE, Rank.FIVE, Rank.TEN,
        )
        val engine = BlackjackEngine(source)
        engine.startRound()
        engine.split()
        engine.hit()

        val completed = engine.hit()

        assertEquals(GamePhase.COMPLETE, completed.phase)
        assertTrue(completed.playerHands.hands.all { it.status == PlayerHandStatus.BUST })
        assertEquals(listOf(HandOutcome.DEALER_WIN, HandOutcome.DEALER_WIN), completed.outcomes)
        assertEquals(900, completed.balance)
        assertTrue(completed.isDealerHoleCardRevealed)
        assertEquals(3, completed.dealerHand.cards.size)
        assertEquals(9, source.drawCount)
    }

    @Test
    fun splitAcesMakingTwentyOne_receiveOrdinaryWinPayouts() {
        val source = FixedCardSource(Rank.ACE, Rank.TEN, Rank.ACE, Rank.SEVEN, Rank.KING, Rank.QUEEN)
        val engine = BlackjackEngine(source)
        engine.startRound()
        engine.split()
        engine.stand()

        val completed = engine.stand()

        assertEquals(listOf(21, 21), completed.playerHands.hands.map { it.hand.score })
        assertEquals(listOf(HandOutcome.PLAYER_WIN, HandOutcome.PLAYER_WIN), completed.outcomes)
        assertEquals(1_100, completed.balance)
    }

    @Test
    fun newRound_resetsSplitHandsAndBetsButKeepsBalanceAndSelectedBet() {
        val source = FixedCardSource(
            Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN, Rank.TEN, Rank.TEN,
            Rank.SIX, Rank.TEN, Rank.SIX, Rank.SEVEN,
        )
        val engine = BlackjackEngine(source, initialBet = 75)
        engine.startRound()
        engine.split()
        engine.stand()
        engine.stand()
        assertEquals(1_150, engine.state.balance)

        val state = engine.startRound()

        assertEquals(1_075, state.balance)
        assertEquals(75, state.selectedBet.amount)
        assertEquals(listOf(75), state.committedBets.map { it.amount })
        assertEquals(1, state.playerHands.hands.size)
        assertFalse(state.playerHands.activeHand.isFromSplit)
        assertEquals(0, state.playerHands.activeHandIndex)
        assertFalse(state.isRoundSettled)
        assertTrue(state.outcomes.isEmpty())
        assertTrue(GameAction.SPLIT in state.availableActions)
    }

    @Test
    fun beforeRoundAndAfterDealerNatural_splitIsRejected() {
        val source = FixedCardSource(Rank.EIGHT, Rank.ACE, Rank.EIGHT, Rank.KING)
        val engine = BlackjackEngine(source)
        assertSplitRejectedWithoutChanges(engine, source)

        engine.startRound()
        assertEquals(GamePhase.COMPLETE, engine.state.phase)
        assertSplitRejectedWithoutChanges(engine, source)
    }

    private fun assertSplitRejectedWithoutChanges(engine: BlackjackEngine, source: FixedCardSource) {
        val before = engine.state
        val drawsBefore = source.drawCount
        assertFalse(GameAction.SPLIT in before.availableActions)
        assertThrows(IllegalStateException::class.java) { engine.split() }
        assertEquals(before, engine.state)
        assertEquals(drawsBefore, source.drawCount)
    }

    private class FixedCardSource(vararg ranks: Rank) : CardSource {
        private val cards = ArrayDeque(ranks.mapIndexed { index, rank ->
            Card(rank, Suit.entries[index % Suit.entries.size])
        })
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
