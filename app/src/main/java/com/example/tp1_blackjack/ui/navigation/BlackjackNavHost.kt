package com.example.tp1_blackjack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tp1_blackjack.model.GameAction
import com.example.tp1_blackjack.ui.screens.GameScreen
import com.example.tp1_blackjack.ui.screens.HistoryScreen
import com.example.tp1_blackjack.ui.screens.StatisticsScreen
import com.example.tp1_blackjack.viewmodel.GameUiState
import com.example.tp1_blackjack.viewmodel.RecordsUiState

@Composable
fun BlackjackNavHost(
    navController: NavHostController,
    gameUiState: GameUiState,
    recordsUiState: RecordsUiState,
    onGameAction: (GameAction) -> Unit,
    onBetInputChanged: (String) -> Unit,
    onDeckCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onRetrySave: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = BlackjackDestination.GAME.route,
        modifier = modifier,
    ) {
        composable(BlackjackDestination.GAME.route) {
            GameScreen(
                uiState = gameUiState,
                onGameAction = onGameAction,
                onBetInputChanged = onBetInputChanged,
                onDeckCountSelected = onDeckCountSelected,
                onRetrySave = onRetrySave,
            )
        }
        composable(BlackjackDestination.HISTORY.route) {
            HistoryScreen(recordsUiState)
        }
        composable(BlackjackDestination.STATISTICS.route) {
            StatisticsScreen(recordsUiState)
        }
    }
}
