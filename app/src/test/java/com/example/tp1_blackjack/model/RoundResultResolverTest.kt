package com.example.tp1_blackjack.model

import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundResultResolverTest {
    @Test
    fun twoNaturals_push() {
        val player = playerHand(Rank.ACE, Rank.KING)
        val dealer = hand(Rank.ACE, Rank.QUEEN)

        assertEquals(HandOutcome.PUSH, resolve(player, dealer))
    }

    @Test
    fun playerNatural_beatsDealerTwentyOneWithThreeCards() {
        val player = playerHand(Rank.ACE, Rank.TEN)
        val dealer = hand(Rank.SEVEN, Rank.SEVEN, Rank.SEVEN)

        assertEquals(HandOutcome.PLAYER_BLACKJACK, resolve(player, dealer))
    }

    @Test
    fun splitTwentyOne_isRegularWinNotNatural() {
        val player = PlayerHandState(
            hand = hand(Rank.ACE, Rank.KING),
            isFromSplit = true,
        )
        val dealer = hand(Rank.TEN, Rank.NINE)

        assertEquals(HandOutcome.PLAYER_WIN, resolve(player, dealer))
    }

    @Test
    fun dealerNatural_beatsNonNaturalPlayer() {
        val player = playerHand(Rank.TEN, Rank.NINE)
        val dealer = hand(Rank.ACE, Rank.JACK)

        assertEquals(HandOutcome.DEALER_WIN, resolve(player, dealer))
    }

    @Test
    fun dealerBust_playerWins() {
        val player = playerHand(Rank.TEN, Rank.EIGHT)
        val dealer = hand(Rank.KING, Rank.QUEEN, Rank.TWO)

        assertEquals(HandOutcome.PLAYER_WIN, resolve(player, dealer))
    }

    @Test
    fun equalScores_push() {
        val player = playerHand(Rank.TEN, Rank.EIGHT)
        val dealer = hand(Rank.KING, Rank.EIGHT)

        assertEquals(HandOutcome.PUSH, resolve(player, dealer))
    }

    @Test
    fun higherScoreWins() {
        val player = playerHand(Rank.TEN, Rank.NINE)
        val dealer = hand(Rank.TEN, Rank.EIGHT)

        assertEquals(HandOutcome.PLAYER_WIN, resolve(player, dealer))
    }

    @Test
    fun initialNatural_completesAndRevealsDealerCard() {
        val source = FixedCardSource(
            card(Rank.ACE),
            card(Rank.TEN),
            card(Rank.KING),
            card(Rank.NINE),
        )
        val engine = BlackjackEngine(source)

        val state = engine.startRound()

        assertEquals(GamePhase.COMPLETE, state.phase)
        assertTrue(state.isDealerHoleCardRevealed)
        assertEquals(listOf(HandOutcome.PLAYER_BLACKJACK), state.outcomes)
    }

    @Test
    fun twoInitialNaturals_completeImmediatelyAsPush() {
        val source = FixedCardSource(
            card(Rank.ACE),
            card(Rank.ACE),
            card(Rank.KING),
            card(Rank.QUEEN),
        )
        val engine = BlackjackEngine(source)

        val state = engine.startRound()

        assertEquals(GamePhase.COMPLETE, state.phase)
        assertTrue(state.isDealerHoleCardRevealed)
        assertEquals(listOf(HandOutcome.PUSH), state.outcomes)
        assertEquals(setOf(GameAction.START_ROUND), state.availableActions)
    }

    private fun resolve(player: PlayerHandState, dealer: Hand): HandOutcome =
        RoundResultResolver.resolve(PlayerHands(listOf(player)), dealer).single()

    private fun playerHand(vararg ranks: Rank): PlayerHandState =
        PlayerHandState(hand(*ranks))

    private fun hand(vararg ranks: Rank): Hand = Hand(ranks.map(::card))

    private fun card(rank: Rank): Card = Card(rank, Suit.CLUBS)

    private class FixedCardSource(vararg cards: Card) : CardSource {
        private val cards = ArrayDeque(cards.toList())

        override val remainingCount: Int
            get() = cards.size

        override fun draw(): Card = cards.removeFirst()

        override fun prepareForNewRound(): Boolean = false
    }
}
