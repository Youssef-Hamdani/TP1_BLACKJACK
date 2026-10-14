package com.example.tp1_blackjack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tp1_blackjack.model.GameAction
import com.example.tp1_blackjack.ui.screens.GameScreen
import com.example.tp1_blackjack.ui.screens.HistoryScreen
import com.example.tp1_blackjack.ui.screens.PreparationScreen
import com.example.tp1_blackjack.ui.screens.RoundResultScreen
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
        startDestination = BlackjackDestination.PREPARATION.route,
        modifier = modifier,
    ) {
        composable(BlackjackDestination.PREPARATION.route) {
            PreparationScreen(
                uiState = gameUiState,
                onBetInputChanged = onBetInputChanged,
                onDeckCountSelected = onDeckCountSelected,
                onStart = {
                    onGameAction(GameAction.START_ROUND)
                    navController.navigate(BlackjackDestination.GAME.route) { launchSingleTop = true }
                },
            )
        }
        composable(BlackjackDestination.GAME.route) {
            GameScreen(
                uiState = gameUiState,
                onGameAction = onGameAction,
                onRetrySave = onRetrySave,
            )
        }
        composable(BlackjackDestination.RESULT.route) {
            RoundResultScreen(
                uiState = gameUiState,
                onNewRound = { navController.popBackStack(BlackjackDestination.PREPARATION.route, false) },
                onReviewTable = {
                    navController.navigate(BlackjackDestination.TABLE_REVIEW.route) { launchSingleTop = true }
                },
            )
        }
        composable(BlackjackDestination.TABLE_REVIEW.route) {
            GameScreen(
                uiState = gameUiState,
                onGameAction = onGameAction,
                isReviewMode = true,
                onBackToResult = { navController.popBackStack() },
                onNewRound = { navController.popBackStack(BlackjackDestination.PREPARATION.route, false) },
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
