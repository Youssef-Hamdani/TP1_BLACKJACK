package com.example.tp1_blackjack.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tp1_blackjack.model.GameAction
import com.example.tp1_blackjack.model.GamePhase
import com.example.tp1_blackjack.ui.navigation.BlackjackNavHost
import com.example.tp1_blackjack.ui.navigation.BlackjackDestination
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val gameTabRoute = when (gameUiState.phase) {
        GamePhase.AWAITING_START -> BlackjackDestination.PREPARATION.route
        GamePhase.COMPLETE -> BlackjackDestination.RESULT.route
        else -> BlackjackDestination.GAME.route
    }

    LaunchedEffect(gameUiState.phase, gameUiState.isSaving, gameUiState.canRetrySave, currentRoute) {
        val completedAndSaved = gameUiState.phase == GamePhase.COMPLETE &&
            !gameUiState.isSaving && !gameUiState.canRetrySave && !gameUiState.hasStorageError
        if (completedAndSaved && currentRoute == BlackjackDestination.GAME.route) {
            navController.navigate(BlackjackDestination.RESULT.route) { launchSingleTop = true }
        }
    }

    BlackjackScaffold(
        navController = navController,
        balance = gameUiState.balance,
        primaryGameAction = GameAction.HIT.takeIf { it in gameUiState.availableActions },
        gameTabRoute = gameTabRoute,
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
