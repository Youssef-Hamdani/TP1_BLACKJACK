package com.example.tp1_blackjack.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.viewmodel.RecordsUiState

@Composable
internal fun RecordsStatus(
    uiState: RecordsUiState,
    @StringRes emptyTitle: Int,
    @StringRes emptyBody: Int,
) {
    val message = when {
        uiState.isLoading -> R.string.storage_loading
        uiState.hasError -> R.string.storage_error
        else -> emptyTitle
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(message), style = MaterialTheme.typography.titleMedium)
        if (!uiState.isLoading && !uiState.hasError) {
            Text(stringResource(emptyBody), modifier = Modifier.padding(top = 8.dp))
        }
    }
}
