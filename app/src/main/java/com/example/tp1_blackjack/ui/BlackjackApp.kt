package com.example.tp1_blackjack.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.tp1_blackjack.model.GameAction
import com.example.tp1_blackjack.ui.navigation.BlackjackNavHost
import com.example.tp1_blackjack.ui.scaffold.BlackjackScaffold
import com.example.tp1_blackjack.viewmodel.GameViewModel
import com.example.tp1_blackjack.viewmodel.RecordsViewModel
import com.example.tp1_blackjack.viewmodel.BlackjackViewModelFactory

@Composable
fun BlackjackApp(
    gameViewModel: GameViewModel = viewModel(factory = BlackjackViewModelFactory),
    recordsViewModel: RecordsViewModel = viewModel(factory = BlackjackViewModelFactory),
) {
    val navController = rememberNavController()
    val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val recordsUiState by recordsViewModel.uiState.collectAsStateWithLifecycle()
    val primaryGameAction = when {
        GameAction.START_ROUND in gameUiState.availableActions -> GameAction.START_ROUND
        GameAction.HIT in gameUiState.availableActions -> GameAction.HIT
        else -> null
    }

    BlackjackScaffold(
        navController = navController,
        balance = gameUiState.balance,
        primaryGameAction = primaryGameAction,
        onGameAction = gameViewModel::onGameAction,
    ) { contentPadding ->
        BlackjackNavHost(
            navController = navController,
            gameUiState = gameUiState,
            recordsUiState = recordsUiState,
            onGameAction = gameViewModel::onGameAction,
            onBetInputChanged = gameViewModel::onBetInputChanged,
            onDeckCountSelected = gameViewModel::onDeckCountSelected,
            onRetrySave = gameViewModel::onRetrySave,
            modifier = Modifier.padding(contentPadding),
        )
    }
}
