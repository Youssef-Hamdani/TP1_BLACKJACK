package com.example.tp1_blackjack.ui.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.ui.navigation.BlackjackDestination

const val GAME_FAB_TEST_TAG = "game-primary-action"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlackjackScaffold(
    navController: NavHostController,
    balance: Int,
    playerName: String?,
    primaryGameAction: GameAction?,
    gameTabRoute: String,
    onGameAction: (GameAction) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = BlackjackDestination.fromRoute(backStackEntry?.destination?.route)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(
                            R.string.screen_title,
                            stringResource(currentDestination.titleResourceId),
                        ))
                        playerName?.let {
                            Text(
                                stringResource(R.string.current_player_name, it),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
                actions = {
                    if (currentDestination != BlackjackDestination.PLAYER_CREATION) Text(
                        text = pluralStringResource(
                            R.plurals.balance_chips,
                            balance,
                            balance,
                        ),
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
        },
        bottomBar = {
            if (currentDestination != BlackjackDestination.PLAYER_CREATION) NavigationBar {
                BlackjackDestination.entries.filter { it.showInBottomNavigation }.forEach { destination ->
                    val isGameTab = destination == BlackjackDestination.PREPARATION
                    val selected = if (isGameTab) {
                        currentDestination in setOf(
                            BlackjackDestination.PREPARATION,
                            BlackjackDestination.GAME,
                            BlackjackDestination.RESULT,
                            BlackjackDestination.TABLE_REVIEW,
                        )
                    } else {
                        currentDestination == destination
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            val targetRoute = if (isGameTab) gameTabRoute else destination.route
                            navController.navigate(targetRoute) {
                                if (isGameTab) {
                                    popUpTo(BlackjackDestination.PREPARATION.route)
                                }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.titleResourceId)) },
                        modifier = Modifier.testTag("destination-${destination.route}"),
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentDestination == BlackjackDestination.GAME && primaryGameAction == GameAction.HIT) {
                ExtendedFloatingActionButton(
                    onClick = { onGameAction(primaryGameAction) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(
                            stringResource(R.string.action_hit),
                        )
                    },
                    modifier = Modifier.testTag(GAME_FAB_TEST_TAG),
                )
            }
        },
        content = content,
    )
}
