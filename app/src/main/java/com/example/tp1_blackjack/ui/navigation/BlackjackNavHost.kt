package com.example.tp1_blackjack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.ui.screens.GameScreen
import com.example.tp1_blackjack.ui.screens.HistoryScreen
import com.example.tp1_blackjack.ui.screens.PreparationScreen
import com.example.tp1_blackjack.ui.screens.PlayerCreationScreen
import com.example.tp1_blackjack.ui.screens.RoundResultScreen
import com.example.tp1_blackjack.ui.screens.StatisticsScreen
import com.example.tp1_blackjack.viewmodel.GameUiState
import com.example.tp1_blackjack.viewmodel.RecordsUiState

@Composable
fun BlackjackNavHost(
    navController: NavHostController,
    initialDestination: String,
    onCreatePlayer: (String) -> Boolean,
    gameUiState: GameUiState,
    recordsUiState: RecordsUiState,
    onGameAction: (GameAction) -> Unit,
    onBetInputChanged: (String) -> Unit,
    onRestartBankroll: () -> Unit,
    onDeckCountSelected: (Int) -> Unit,
    isPresentationBusy: Boolean,
    onPresentationIdle: () -> Unit,
    onResultReady: () -> Unit,
    onNewRoundRequested: () -> Unit,
    modifier: Modifier = Modifier,
    onRetrySave: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = initialDestination,
        route = BLACKJACK_GRAPH_ROUTE,
        modifier = modifier,
    ) {
        composable(BlackjackDestination.PLAYER_CREATION.route) {
            PlayerCreationScreen(onCreate = { name ->
                if (onCreatePlayer(name)) {
                    navController.navigate(BlackjackDestination.PREPARATION.route) {
                        popUpTo(BlackjackDestination.PLAYER_CREATION.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    true
                } else {
                    false
                }
            })
        }
        composable(BlackjackDestination.PREPARATION.route) {
            PreparationScreen(
                uiState = gameUiState,
                onBetInputChanged = onBetInputChanged,
                onRestartBankroll = onRestartBankroll,
                onDeckCountSelected = onDeckCountSelected,
                onStart = {
                    onGameAction(GameAction.START_ROUND)
                    navController.navigate(BlackjackDestination.GAME.route) {
                        popUpTo(BlackjackDestination.PREPARATION.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                isPresentationBusy = isPresentationBusy,
            )
        }
        composable(BlackjackDestination.GAME.route) {
            GameScreen(
                uiState = gameUiState,
                onGameAction = onGameAction,
                onRetrySave = onRetrySave,
                isPresentationBusy = isPresentationBusy,
                onPresentationIdle = onPresentationIdle,
                onRoundPresentationFinished = {
                    onResultReady()
                    navController.navigate(BlackjackDestination.RESULT.route) {
                        popUpTo(BlackjackDestination.GAME.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(BlackjackDestination.RESULT.route) {
            RoundResultScreen(
                uiState = gameUiState,
                onNewRound = {
                    onNewRoundRequested()
                    navController.navigate(BlackjackDestination.PREPARATION.route) {
                        popUpTo(BLACKJACK_GRAPH_ROUTE)
                        launchSingleTop = true
                    }
                },
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
                onNewRound = {
                    onNewRoundRequested()
                    navController.navigate(BlackjackDestination.PREPARATION.route) {
                        popUpTo(BLACKJACK_GRAPH_ROUTE)
                        launchSingleTop = true
                    }
                },
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
