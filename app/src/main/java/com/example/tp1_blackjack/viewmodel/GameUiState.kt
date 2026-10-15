package com.example.tp1_blackjack.viewmodel

import com.example.tp1_blackjack.model.cards.Card
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.model.game.GamePhase
import com.example.tp1_blackjack.model.game.PlayerHandStatus
import com.example.tp1_blackjack.model.results.HandOutcome
import com.example.tp1_blackjack.model.results.CardOwner

data class DrawEventUiState(
    val drawOrder: Int,
    val owner: CardOwner,
    val handIndex: Int?,
)

data class DisplayCardUiState(
    val card: Card?,
    val isFaceDown: Boolean,
)

data class PlayerHandUiState(
    val cards: List<Card>,
    val score: Int,
    val runningScores: List<Int>,
    val status: PlayerHandStatus,
    val isActive: Boolean,
    val outcome: HandOutcome?,
    val bet: Int? = null,
    val netChange: Int? = null,
)

enum class BetInputError {
    REQUIRED,
    NOT_A_NUMBER,
    OUT_OF_RANGE,
    INSUFFICIENT_FUNDS,
}

data class GameUiState(
    val isSaving: Boolean = false,
    val canRetrySave: Boolean = false,
    val isLoading: Boolean = false,
    val hasStorageError: Boolean = false,
    val phase: GamePhase = GamePhase.AWAITING_START,
    val dealerCards: List<DisplayCardUiState> = emptyList(),
    val dealerScore: Int? = null,
    val dealerRunningScores: List<Int> = emptyList(),
    val drawEvents: List<DrawEventUiState> = emptyList(),
    val playerHands: List<PlayerHandUiState> = emptyList(),
    val netChange: Int? = null,
    val betInput: String = "50",
    val selectedBet: Int = 50,
    val balance: Int = 1_000,
    val selectedDeckCount: Int = 1,
    val supportedDeckCounts: List<Int> = listOf(1, 2, 4, 6),
    val canSelectDeckCount: Boolean = true,
    val betInputError: BetInputError? = null,
    val availableActions: Set<GameAction> = setOf(GameAction.START_ROUND),
)
