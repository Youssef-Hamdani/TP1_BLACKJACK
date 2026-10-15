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

class BlackjackEngineTest {
    @Test
    fun startRound_dealsPlayerHolePlayerVisible() {
        val playerFirst = card(Rank.TEN, Suit.HEARTS)
        val dealerHole = card(Rank.FIVE, Suit.CLUBS)
        val playerSecond = card(Rank.SEVEN, Suit.SPADES)
        val dealerVisible = card(Rank.KING, Suit.DIAMONDS)
        val source = FixedCardSource(playerFirst, dealerHole, playerSecond, dealerVisible)
        val engine = BlackjackEngine(source)

        val state = engine.startRound()

        assertEquals(listOf(playerFirst, playerSecond), state.playerHands.activeHand.hand.cards)
        assertEquals(listOf(dealerHole, dealerVisible), state.dealerHand.cards)
        assertFalse(state.isDealerHoleCardRevealed)
        assertEquals(GamePhase.PLAYER_TURN, state.phase)
    }

    @Test
    fun hit_addsCardToActiveHand() {
        val hitCard = card(Rank.THREE)
        val engine = engineWithInitialCards(
            playerFirst = Rank.TEN,
            dealerHole = Rank.SEVEN,
            playerSecond = Rank.FIVE,
            dealerVisible = Rank.KING,
            extraCards = arrayOf(hitCard),
        )
        engine.startRound()

        val state = engine.hit()

        assertEquals(18, state.playerHands.activeHand.hand.score)
        assertEquals(hitCard, state.playerHands.activeHand.hand.cards.last())
        assertEquals(GamePhase.PLAYER_TURN, state.phase)
    }

    @Test
    fun bust_completesWithoutRevealingOrPlayingDealer() {
        val source = FixedCardSource(
            card(Rank.KING),
            card(Rank.FIVE),
            card(Rank.QUEEN),
            card(Rank.SIX),
            card(Rank.TWO),
            card(Rank.KING),
        )
        val engine = BlackjackEngine(source)
        engine.startRound()

        val state = engine.hit()

        assertEquals(PlayerHandStatus.BUST, state.playerHands.activeHand.status)
        assertEquals(GamePhase.COMPLETE, state.phase)
        assertFalse(state.isDealerHoleCardRevealed)
        assertEquals(2, state.dealerHand.cards.size)
        assertEquals(5, source.drawCount)
        assertEquals(listOf(HandOutcome.DEALER_WIN), state.outcomes)
    }

    @Test
    fun stand_revealsDealerAndDrawsUntilSeventeen() {
        val source = FixedCardSource(
            card(Rank.TEN),
            card(Rank.FIVE),
            card(Rank.SEVEN),
            card(Rank.SIX),
            card(Rank.SIX),
            card(Rank.KING),
        )
        val engine = BlackjackEngine(source)
        engine.startRound()

        val state = engine.stand()

        assertTrue(state.isDealerHoleCardRevealed)
        assertEquals(17, state.dealerHand.score)
        assertEquals(3, state.dealerHand.cards.size)
        assertEquals(GamePhase.COMPLETE, state.phase)
        assertEquals(5, source.drawCount)
    }

    @Test
    fun dealer_standsOnSoftSeventeen() {
        val source = FixedCardSource(
            card(Rank.TEN),
            card(Rank.ACE),
            card(Rank.SEVEN),
            card(Rank.SIX),
            card(Rank.KING),
        )
        val engine = BlackjackEngine(source)
        engine.startRound()

        val state = engine.stand()

        assertEquals(17, state.dealerHand.score)
        assertEquals(2, state.dealerHand.cards.size)
        assertEquals(4, source.drawCount)
    }

    @Test
    fun hitOutsidePlayerTurn_isRejected() {
        val engine = engineWithInitialCards(
            playerFirst = Rank.TEN,
            dealerHole = Rank.FIVE,
            playerSecond = Rank.SEVEN,
            dealerVisible = Rank.SIX,
        )

        assertThrows(IllegalStateException::class.java) { engine.hit() }
    }

    @Test
    fun restoreWallet_appliesSavedBalanceAndBetBeforeFirstRound() {
        val engine = BlackjackEngine()
        engine.restoreWallet(Wallet(balance = 725, defaultBet = 75))

        assertEquals(725, engine.state.balance)
        assertEquals(75, engine.state.selectedBet.amount)
        assertEquals(GamePhase.AWAITING_START, engine.state.phase)
    }

    @Test
    fun restoreWallet_rejectsInvalidDataAndCannotReplaceAnActiveRound() {
        val engine = engineWithInitialCards(Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX)
        assertThrows(IllegalArgumentException::class.java) {
            engine.restoreWallet(Wallet(balance = -1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            engine.restoreWallet(Wallet(defaultBet = 0))
        }
        engine.startRound()
        val activeState = engine.state
        assertThrows(IllegalStateException::class.java) {
            engine.restoreWallet(Wallet(balance = 2_000))
        }
        assertEquals(activeState, engine.state)
    }

    private fun engineWithInitialCards(
        playerFirst: Rank,
        dealerHole: Rank,
        playerSecond: Rank,
        dealerVisible: Rank,
        vararg extraCards: Card,
    ): BlackjackEngine = BlackjackEngine(
        FixedCardSource(
            card(playerFirst),
            card(dealerHole),
            card(playerSecond),
            card(dealerVisible),
            *extraCards,
        ),
    )

    private fun card(rank: Rank, suit: Suit = Suit.CLUBS): Card = Card(rank, suit)

    private class FixedCardSource(vararg cards: Card) : CardSource {
        private val cards = ArrayDeque(cards.toList())

        var drawCount: Int = 0
            private set

        override val remainingCount: Int
            get() = cards.size

        override fun draw(): Card {
            drawCount++
            return cards.removeFirst()
        }

        override fun prepareForNewRound(): Boolean = false
    }
}
