package com.example.tp1_blackjack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp1_blackjack.model.results.CardValueStats
import com.example.tp1_blackjack.model.results.GameHistoryEntry
import com.example.tp1_blackjack.repository.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RecordsUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val history: List<GameHistoryEntry> = emptyList(),
    val cardStatistics: List<CardValueStats> = emptyList(),
) {
    val observedCardCount: Int get() = cardStatistics.sumOf { it.count }
}

class RecordsViewModel(repository: GameRepository) : ViewModel() {
    val uiState = combine(repository.history, repository.cardStatistics) { history, statistics ->
        RecordsUiState(isLoading = false, history = history, cardStatistics = statistics)
    }.catch { error ->
        if (error is CancellationException) throw error
        emit(RecordsUiState(isLoading = false, hasError = true))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordsUiState(),
    )
}
