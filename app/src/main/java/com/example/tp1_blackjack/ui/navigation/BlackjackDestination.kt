package com.example.tp1_blackjack.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tp1_blackjack.R

enum class BlackjackDestination(
    val route: String,
    @param:StringRes val titleResourceId: Int,
    val icon: ImageVector,
    val showInBottomNavigation: Boolean = false,
) {
    PLAYER_CREATION(
        route = "player-creation",
        titleResourceId = R.string.destination_player_creation,
        icon = Icons.Default.Person,
    ),
    PREPARATION(
        route = "preparation",
        titleResourceId = R.string.destination_game,
        icon = Icons.Default.Casino,
        showInBottomNavigation = true,
    ),
    GAME(
        route = "game",
        titleResourceId = R.string.destination_game,
        icon = Icons.Default.Casino,
    ),
    RESULT(
        route = "result",
        titleResourceId = R.string.destination_result,
        icon = Icons.Default.Casino,
    ),
    TABLE_REVIEW(
        route = "table-review",
        titleResourceId = R.string.destination_table_review,
        icon = Icons.Default.Casino,
    ),
    HISTORY(
        route = "history",
        titleResourceId = R.string.destination_history,
        icon = Icons.Default.History,
        showInBottomNavigation = true,
    ),
    STATISTICS(
        route = "statistics",
        titleResourceId = R.string.destination_statistics,
        icon = Icons.Default.BarChart,
        showInBottomNavigation = true,
    ),
    ;

    companion object {
        fun fromRoute(route: String?): BlackjackDestination =
            entries.firstOrNull { it.route == route } ?: PREPARATION
    }
}
