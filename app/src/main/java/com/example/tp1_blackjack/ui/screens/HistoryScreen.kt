package com.example.tp1_blackjack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.ui.components.RecordsStatus
import com.example.tp1_blackjack.viewmodel.RecordsUiState
import java.text.DateFormat
import java.util.Date
import kotlin.math.abs

@Composable
fun HistoryScreen(uiState: RecordsUiState) {
    if (uiState.isLoading || uiState.hasError || uiState.history.isEmpty()) {
        RecordsStatus(uiState, R.string.history_placeholder_title, R.string.history_placeholder_body)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("history-list"),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                pluralStringResource(
                    R.plurals.saved_games_count,
                    uiState.history.size,
                    uiState.history.size,
                ),
                modifier = Modifier.testTag("saved-games-count"),
            )
        }
        items(uiState.history, key = { it.game.id }) { entry ->
            val date = remember(entry.game.completedAt, entry.game.abandonedAt) {
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(checkNotNull(entry.game.completedAt ?: entry.game.abandonedAt)))
            }
            Card(modifier = Modifier.fillMaxWidth().testTag("history-item-${entry.game.id}")) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(date, style = MaterialTheme.typography.titleMedium)
                    val totalBet = if (entry.game.abandonedAt != null) {
                        entry.game.initialBet * entry.game.committedBetCount
                    } else {
                        entry.hands.sumOf { it.bet }
                    }
                    Text(pluralStringResource(R.plurals.history_total_bet, totalBet, totalBet))
                    if (entry.game.abandonedAt != null) {
                        Text(stringResource(R.string.history_abandoned))
                        return@Column
                    }
                    entry.hands.forEach { hand ->
                        Text(
                            pluralStringResource(
                                R.plurals.history_hand,
                                abs(hand.netChange),
                                hand.handIndex + 1,
                                hand.bet,
                                stringResource(hand.outcome.stringResourceId()),
                                hand.netChange,
                            ),
                        )
                    }
                    val delta = checkNotNull(entry.game.netChange)
                    Text(
                        pluralStringResource(R.plurals.history_delta, abs(delta), delta),
                        fontWeight = FontWeight.Bold,
                    )
                    val balance = checkNotNull(entry.game.balanceAfter)
                    Text(pluralStringResource(R.plurals.history_balance, balance, balance))
                }
            }
        }
    }
}
