package com.example.tp1_blackjack.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.model.game.GamePhase
import com.example.tp1_blackjack.ui.navigation.BlackjackNavHost
import com.example.tp1_blackjack.ui.navigation.BlackjackDestination
import com.example.tp1_blackjack.ui.scaffold.BlackjackScaffold
import com.example.tp1_blackjack.viewmodel.GameViewModel
import com.example.tp1_blackjack.viewmodel.RecordsViewModel
import com.example.tp1_blackjack.viewmodel.PlayerProfileViewModel
import com.example.tp1_blackjack.viewmodel.BlackjackViewModelFactory

@Composable
fun BlackjackApp(
    gameViewModel: GameViewModel = viewModel(factory = BlackjackViewModelFactory),
    recordsViewModel: RecordsViewModel = viewModel(factory = BlackjackViewModelFactory),
    playerProfileViewModel: PlayerProfileViewModel = viewModel(factory = BlackjackViewModelFactory),
) {
    val navController = rememberNavController()
    val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val recordsUiState by recordsViewModel.uiState.collectAsStateWithLifecycle()
    val playerName by playerProfileViewModel.playerName.collectAsStateWithLifecycle()
    // Freeze the start destination for this NavHost; creation explicitly navigates
    // to preparation after saving the name.
    val initialDestination = remember {
        if (playerName == null) BlackjackDestination.PLAYER_CREATION.route
        else BlackjackDestination.PREPARATION.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Android may restore the previous NavHost stack after killing the process,
    // but this ViewModel starts with a fresh game. Do not show an empty table.
    LaunchedEffect(gameUiState.phase, gameUiState.isLoading, backStackEntry?.destination?.route) {
        val staleGameRoute = backStackEntry?.destination?.route in setOf(
            BlackjackDestination.GAME.route,
            BlackjackDestination.RESULT.route,
            BlackjackDestination.TABLE_REVIEW.route,
        )
        if (!gameUiState.isLoading &&
            gameUiState.phase == GamePhase.AWAITING_START && staleGameRoute
        ) {
            navController.navigate(BlackjackDestination.PREPARATION.route) {
                popUpTo(BlackjackDestination.PREPARATION.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
    var isPresentationBusy by rememberSaveable { mutableStateOf(false) }
    var isResultReady by rememberSaveable { mutableStateOf(false) }
    // COMPLETE remains the engine phase while the next round is being configured.
    var isPreparingNextRound by rememberSaveable { mutableStateOf(false) }
    val gameTabRoute = when (gameUiState.phase) {
        GamePhase.AWAITING_START -> BlackjackDestination.PREPARATION.route
        GamePhase.COMPLETE -> if (isPreparingNextRound) {
            BlackjackDestination.PREPARATION.route
        } else if (isResultReady) {
            BlackjackDestination.RESULT.route
        } else {
            BlackjackDestination.GAME.route
        }
        else -> BlackjackDestination.GAME.route
    }
    val performGameAction: (GameAction) -> Unit = { action ->
        if (!isPresentationBusy) {
            isPresentationBusy = true
            if (action == GameAction.START_ROUND) {
                isResultReady = false
                isPreparingNextRound = false
            }
            gameViewModel.onGameAction(action)
        }
    }

    BlackjackScaffold(
        navController = navController,
        balance = gameUiState.balance,
        playerName = playerName,
        primaryGameAction = GameAction.HIT.takeIf {
            !isPresentationBusy && it in gameUiState.availableActions
        },
        gameTabRoute = gameTabRoute,
        onGameAction = performGameAction,
    ) { contentPadding ->
        BlackjackNavHost(
            navController = navController,
            initialDestination = initialDestination,
            onCreatePlayer = playerProfileViewModel::createPlayer,
            gameUiState = gameUiState,
            recordsUiState = recordsUiState,
            onGameAction = performGameAction,
            onBetInputChanged = gameViewModel::onBetInputChanged,
            onDeckCountSelected = gameViewModel::onDeckCountSelected,
            onRetrySave = {
                isPresentationBusy = true
                gameViewModel.onRetrySave()
            },
            isPresentationBusy = isPresentationBusy,
            onPresentationIdle = { isPresentationBusy = false },
            onResultReady = { isResultReady = true },
            onNewRoundRequested = { isPreparingNextRound = true },
            modifier = Modifier.padding(contentPadding),
        )
    }
}
