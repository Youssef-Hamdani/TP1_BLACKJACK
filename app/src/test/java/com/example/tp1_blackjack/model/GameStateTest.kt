package com.example.tp1_blackjack.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {
    @Test
    fun awaitingStart_onlyAllowsStartingRound() {
        val state = GameState()

        assertEquals(setOf(GameAction.START_ROUND), state.availableActions)
        assertThrows(IllegalStateException::class.java) {
            state.requireAction(GameAction.HIT)
        }
    }

    @Test
    fun playerTurn_allowsHitAndStandForActiveHand() {
        val state = GameState(phase = GamePhase.PLAYER_TURN)

        assertEquals(setOf(GameAction.HIT, GameAction.STAND), state.availableActions)
        state.requireAction(GameAction.HIT)
        state.requireAction(GameAction.STAND)
    }

    @Test
    fun dealerTurn_refusesPlayerActions() {
        val state = GameState(phase = GamePhase.DEALER_TURN)

        assertTrue(state.availableActions.isEmpty())
        assertThrows(IllegalStateException::class.java) {
            state.requireAction(GameAction.STAND)
        }
    }

    @Test
    fun oneGameState_supportsTwoPlayerHandsAndOneDealer() {
        val firstHand = PlayerHandState(Hand(listOf(card(Rank.EIGHT))))
        val secondHand = PlayerHandState(
            hand = Hand(listOf(card(Rank.NINE))),
            isFromSplit = true,
        )
        val dealer = Hand(listOf(card(Rank.KING)))

        val state = GameState(
            phase = GamePhase.PLAYER_TURN,
            playerHands = PlayerHands(listOf(firstHand, secondHand)),
            dealerHand = dealer,
        )

        assertEquals(2, state.playerHands.hands.size)
        assertEquals(dealer, state.dealerHand)
    }

    private fun card(rank: Rank): Card = Card(rank, Suit.HEARTS)
}
