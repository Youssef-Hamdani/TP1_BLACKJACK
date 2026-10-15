package com.example.tp1_blackjack.viewmodel

import com.example.tp1_blackjack.model.cards.Card
import com.example.tp1_blackjack.model.cards.CardSource
import com.example.tp1_blackjack.model.cards.Rank
import com.example.tp1_blackjack.model.cards.Suit
import com.example.tp1_blackjack.model.game.BlackjackEngine
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.model.game.GamePhase
import com.example.tp1_blackjack.model.results.HandOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {
    @Test
    fun startRound_projectsHiddenDealerCardAndAvailableActions() {
        val viewModel = viewModelWithCards(
            Rank.TEN,
            Rank.FIVE,
            Rank.SEVEN,
            Rank.SIX,
        )

        viewModel.onGameAction(GameAction.START_ROUND)

        val state = viewModel.uiState.value
        assertEquals(GamePhase.PLAYER_TURN, state.phase)
        assertEquals(17, state.playerHands.single().score)
        assertTrue(state.dealerCards.first().isFaceDown)
        assertNull(state.dealerCards.first().card)
        assertNull(state.dealerScore)
        assertEquals(setOf(GameAction.HIT, GameAction.STAND), state.availableActions)
    }

    @Test
    fun stand_completesRoundAndPublishesOutcome() {
        val viewModel = viewModelWithCards(
            Rank.TEN,
            Rank.FIVE,
            Rank.SEVEN,
            Rank.FIVE,
            Rank.SIX,
            Rank.KING,
        )
        viewModel.onGameAction(GameAction.START_ROUND)

        viewModel.onGameAction(GameAction.STAND)

        val state = viewModel.uiState.value
        assertEquals(GamePhase.COMPLETE, state.phase)
        assertEquals(4, state.dealerCards.size)
        assertTrue(state.dealerCards.none { it.isFaceDown })
        assertEquals(setOf(GameAction.START_ROUND), state.availableActions)
        assertEquals(HandOutcome.PLAYER_WIN, state.playerHands.single().outcome)
        assertEquals(50, state.playerHands.single().netChange)
        assertEquals(50, state.netChange)
    }

    @Test
    fun betInput_rejectsInvalidValueAndKeepsValidSelectionAcrossRound() {
        val viewModel = viewModelWithCards(
            Rank.TEN,
            Rank.FIVE,
            Rank.SEVEN,
            Rank.SIX,
            Rank.SIX,
        )

        viewModel.onBetInputChanged("0")
        assertEquals(BetInputError.OUT_OF_RANGE, viewModel.uiState.value.betInputError)
        assertTrue(GameAction.START_ROUND !in viewModel.uiState.value.availableActions)

        viewModel.onBetInputChanged("75")
        assertEquals(75, viewModel.uiState.value.selectedBet)
        assertNull(viewModel.uiState.value.betInputError)

        viewModel.onGameAction(GameAction.START_ROUND)
        viewModel.onGameAction(GameAction.STAND)

        assertEquals("75", viewModel.uiState.value.betInput)
        assertEquals(75, viewModel.uiState.value.selectedBet)
        assertTrue(GameAction.START_ROUND in viewModel.uiState.value.availableActions)
    }

    @Test
    fun betAboveBalance_isRejectedUntilAffordableValueSelected() {
        val source = FixedCardSource(
            card(Rank.TEN),
            card(Rank.FIVE),
            card(Rank.SEVEN),
            card(Rank.SIX),
        )
        val viewModel = GameViewModel(
            BlackjackEngine(
                cardSource = source,
                initialBalance = 40,
                initialBet = 50,
            ),
        )

        assertEquals(BetInputError.INSUFFICIENT_FUNDS, viewModel.uiState.value.betInputError)
        assertTrue(GameAction.START_ROUND !in viewModel.uiState.value.availableActions)

        viewModel.onBetInputChanged("40")

        assertNull(viewModel.uiState.value.betInputError)
        assertTrue(GameAction.START_ROUND in viewModel.uiState.value.availableActions)

        viewModel.onGameAction(GameAction.START_ROUND)

        assertEquals(0, viewModel.uiState.value.balance)
        assertNull(viewModel.uiState.value.betInputError)
        assertTrue(GameAction.HIT in viewModel.uiState.value.availableActions)
    }

    @Test
    fun split_publishesTwoHandsBalanceAndActiveHandUntilSettlement() {
        val viewModel = viewModelWithCards(
            Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN, Rank.TEN, Rank.NINE,
        )
        viewModel.onGameAction(GameAction.START_ROUND)
        assertTrue(GameAction.SPLIT in viewModel.uiState.value.availableActions)

        viewModel.onGameAction(GameAction.SPLIT)

        val splitState = viewModel.uiState.value
        assertEquals(900, splitState.balance)
        assertEquals(listOf(18, 17), splitState.playerHands.map { it.score })
        assertEquals(listOf(true, false), splitState.playerHands.map { it.isActive })
        assertNull(splitState.dealerCards.first().card)
        assertTrue(GameAction.SPLIT !in splitState.availableActions)
        assertThrows(IllegalStateException::class.java) {
            viewModel.onGameAction(GameAction.SPLIT)
        }
        assertEquals(splitState, viewModel.uiState.value)

        viewModel.onGameAction(GameAction.STAND)
        assertEquals(listOf(false, true), viewModel.uiState.value.playerHands.map { it.isActive })
        assertNull(viewModel.uiState.value.dealerScore)
        viewModel.onGameAction(GameAction.STAND)

        val completed = viewModel.uiState.value
        assertEquals(GamePhase.COMPLETE, completed.phase)
        assertEquals(listOf(HandOutcome.PLAYER_WIN, HandOutcome.PUSH), completed.playerHands.map { it.outcome })
        assertEquals(listOf(50, 0), completed.playerHands.map { it.netChange })
        assertEquals(50, completed.netChange)
        assertEquals(1_050, completed.balance)
        assertEquals(17, completed.dealerScore)
        assertTrue(completed.playerHands.none { it.isActive })
    }

    @Test
    fun deckCountSelection_isPublishedAndBlockedDuringRound() {
        val viewModel = GameViewModel(
            BlackjackEngine(
                cardSource = FixedCardSource(card(Rank.TWO)),
                cardSourceFactory = {
                    FixedCardSource(
                        card(Rank.TEN), card(Rank.FIVE), card(Rank.SEVEN), card(Rank.SIX),
                    )
                },
            ),
        )

        viewModel.onDeckCountSelected(4)

        assertEquals(4, viewModel.uiState.value.selectedDeckCount)
        assertTrue(viewModel.uiState.value.canSelectDeckCount)
        assertEquals(listOf(1, 2, 4, 6), viewModel.uiState.value.supportedDeckCounts)
        viewModel.onGameAction(GameAction.START_ROUND)
        assertTrue(!viewModel.uiState.value.canSelectDeckCount)
        assertThrows(IllegalStateException::class.java) {
            viewModel.onDeckCountSelected(2)
        }
        assertEquals(4, viewModel.uiState.value.selectedDeckCount)
    }

    private fun viewModelWithCards(vararg ranks: Rank): GameViewModel = GameViewModel(
        BlackjackEngine(FixedCardSource(*ranks.map(::card).toTypedArray())),
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
