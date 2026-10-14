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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.ui.components.RecordsStatus
import com.example.tp1_blackjack.viewmodel.RecordsUiState

@Composable
fun StatisticsScreen(uiState: RecordsUiState) {
    if (uiState.isLoading || uiState.hasError || uiState.observedCardCount == 0) {
        RecordsStatus(uiState, R.string.statistics_placeholder_title, R.string.statistics_placeholder_body)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("statistics-list"),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                pluralStringResource(
                    R.plurals.observed_cards_count,
                    uiState.observedCardCount,
                    uiState.observedCardCount,
                ),
                modifier = Modifier.testTag("observed-cards-count"),
            )
            Text(
                stringResource(R.string.statistics_explanation),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        items((1..10).toList(), key = { it }) { value ->
            val stats = uiState.cardStatistics.firstOrNull { it.value == value }
            val label = when (value) {
                1 -> stringResource(R.string.rank_ace)
                10 -> stringResource(R.string.statistics_ten)
                else -> value.toString()
            }
            Card(modifier = Modifier.fillMaxWidth().testTag("stats-value-$value")) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                    val count = stats?.count ?: 0
                    Text(
                        pluralStringResource(
                            R.plurals.statistics_value_count,
                            count,
                            count,
                            stats?.percentage ?: 0.0,
                        ),
                    )
                }
            }
        }
    }
}
