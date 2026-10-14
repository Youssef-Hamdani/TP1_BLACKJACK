package com.example.tp1_blackjack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp1_blackjack.repository.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.tp1_blackjack.model.BlackjackEngine
import com.example.tp1_blackjack.model.Bet
import com.example.tp1_blackjack.model.GameAction
import com.example.tp1_blackjack.model.GamePhase
import com.example.tp1_blackjack.model.GameState
import com.example.tp1_blackjack.model.GameRecord
import com.example.tp1_blackjack.model.PayoutCalculator
import java.util.UUID
import com.example.tp1_blackjack.model.Shoe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val engine: BlackjackEngine = BlackjackEngine(),
    private val repository: GameRepository? = null,
) : ViewModel() {
    private var isStorageReady = repository == null
    private var hasStorageError = false
    private val preferenceWrites = Mutex()
    private var isSaving = false
    private var activeGame: GameRecord? = null
    private var pendingSave: RoundSave? = null

    private data class RoundSave(val game: GameRecord, val state: GameState, val completedAt: Long?)
    private val _uiState = MutableStateFlow(engine.state.toUiState().copy(
        isLoading = !isStorageReady,
        availableActions = if (isStorageReady) engine.state.availableActions else emptySet(),
        canSelectDeckCount = isStorageReady,
    ))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        if (repository != null) {
            viewModelScope.launch {
                try {
                    engine.restoreWallet(repository.loadWallet())
                    isStorageReady = true
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    hasStorageError = true
                }
                publish(engine.state.selectedBet.amount.toString())
            }
        }
    }

    private fun publish(betInput: String) {
        val state = engine.state.toUiState(betInput)
        val canAct = isStorageReady && !isSaving && pendingSave == null
        _uiState.value = state.copy(
            isLoading = !isStorageReady && !hasStorageError,
            hasStorageError = hasStorageError,
            isSaving = isSaving,
            canRetrySave = pendingSave != null && !isSaving,
            availableActions = if (canAct) state.availableActions else emptySet(),
            canSelectDeckCount = canAct && state.canSelectDeckCount,
        )
    }

    fun onGameAction(action: GameAction) {
        // A second tap before recomposition must not replay an action still being saved.
        if (repository != null && (!isStorageReady || isSaving || pendingSave != null)) return
        check(action in _uiState.value.availableActions) {
            "$action is not available in the current UI state"
        }
        val currentBetInput = _uiState.value.betInput
        if (action == GameAction.START_ROUND && repository != null) {
            activeGame = GameRecord(UUID.randomUUID().toString(), System.currentTimeMillis(),
                engine.state.deckCount, engine.state.selectedBet.amount)
        }
        when (action) {
            GameAction.START_ROUND -> engine.startRound()
            GameAction.HIT -> engine.hit()
            GameAction.STAND -> engine.stand()
            GameAction.SPLIT -> engine.split()
        }
        if (repository != null) {
            val game = checkNotNull(activeGame)
            pendingSave = RoundSave(game, engine.state,
                if (engine.state.phase == GamePhase.COMPLETE) maxOf(game.startedAt, System.currentTimeMillis()) else null)
            savePendingRound()
        }
        publish(currentBetInput)
    }

    fun onRetrySave() {
        if (pendingSave != null && !isSaving) savePendingRound()
    }

    private fun savePendingRound() {
        val snapshot = checkNotNull(pendingSave)
        val storage = checkNotNull(repository)
        isSaving = true
        hasStorageError = false
        publish(_uiState.value.betInput)
        viewModelScope.launch {
            preferenceWrites.withLock {
                try {
                    storage.saveRound(snapshot.game, snapshot.state, snapshot.completedAt)
                    pendingSave = null
                    hasStorageError = false
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    hasStorageError = true
                } finally {
                    isSaving = false
                    publish(_uiState.value.betInput)
                }
            }
        }
    }

    fun onBetInputChanged(input: String) {
        if (!isStorageReady || isSaving || pendingSave != null) return
        val parsedBet = input.toIntOrNull()?.let(Bet::from)
        val canEditBet = engine.state.phase == GamePhase.AWAITING_START ||
            engine.state.phase == GamePhase.COMPLETE
        if (parsedBet != null && canEditBet) {
            engine.selectBet(parsedBet.amount)
            if (repository != null) viewModelScope.launch {
                preferenceWrites.withLock {
                    try {
                        repository.setDefaultBet(parsedBet.amount)
                        hasStorageError = false
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        hasStorageError = true
                    }
                    publish(_uiState.value.betInput)
                }
            }
        }
        publish(input)
    }

    fun onDeckCountSelected(deckCount: Int) {
        check(_uiState.value.canSelectDeckCount) { "Deck selection is currently unavailable" }
        engine.selectDeckCount(deckCount)
        publish(_uiState.value.betInput)
    }
}

private fun GameState.toUiState(
    betInput: String = selectedBet.amount.toString(),
): GameUiState {
    val isBetweenRounds = phase == GamePhase.AWAITING_START || phase == GamePhase.COMPLETE
    val betInputError = when {
        betInput.isBlank() -> BetInputError.REQUIRED
        betInput.toIntOrNull() == null -> BetInputError.NOT_A_NUMBER
        Bet.from(betInput.toInt()) == null -> BetInputError.OUT_OF_RANGE
        isBetweenRounds && betInput.toInt() > balance -> BetInputError.INSUFFICIENT_FUNDS
        else -> null
    }

    return GameUiState(
        phase = phase,
        dealerCards = dealerHand.cards.mapIndexed { index, card ->
            val isHoleCardHidden = index == 0 && !isDealerHoleCardRevealed
            DisplayCardUiState(
                card = card.takeUnless { isHoleCardHidden },
                isFaceDown = isHoleCardHidden,
            )
        },
        dealerScore = dealerHand.score.takeIf { isDealerHoleCardRevealed },
        playerHands = playerHands.hands.mapIndexedNotNull { index, playerHand ->
            if (playerHand.hand.cards.isEmpty()) return@mapIndexedNotNull null
            PlayerHandUiState(
                cards = playerHand.hand.cards,
                score = playerHand.hand.score,
                status = playerHand.status,
                isActive = phase == GamePhase.PLAYER_TURN && index == playerHands.activeHandIndex,
                outcome = outcomes.getOrNull(index),
                bet = committedBets.getOrNull(index)?.amount,
                netChange = outcomes.getOrNull(index)?.let { outcome ->
                    committedBets.getOrNull(index)?.let { bet ->
                        PayoutCalculator.grossPayout(bet, outcome) - bet.amount
                    }
                },
            )
        },
        netChange = if (phase == GamePhase.COMPLETE && outcomes.size == committedBets.size) {
            outcomes.indices.sumOf { index ->
                PayoutCalculator.grossPayout(committedBets[index], outcomes[index]) -
                    committedBets[index].amount
            }
        } else {
            null
        },
        betInput = betInput,
        selectedBet = selectedBet.amount,
        balance = balance,
        selectedDeckCount = deckCount,
        supportedDeckCounts = Shoe.SUPPORTED_DECK_COUNTS,
        canSelectDeckCount = canSelectDeckCount,
        betInputError = betInputError,
        availableActions = if (betInputError == null) {
            availableActions
        } else {
            availableActions - GameAction.START_ROUND
        },
    )
}
