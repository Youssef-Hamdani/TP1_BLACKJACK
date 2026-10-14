package com.example.tp1_blackjack.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tp1_blackjack.R

enum class BlackjackDestination(
    val route: String,
    @param:StringRes val titleResourceId: Int,
    val icon: ImageVector,
) {
    GAME(
        route = "game",
        titleResourceId = R.string.destination_game,
        icon = Icons.Default.Casino,
    ),
    HISTORY(
        route = "history",
        titleResourceId = R.string.destination_history,
        icon = Icons.Default.History,
    ),
    STATISTICS(
        route = "statistics",
        titleResourceId = R.string.destination_statistics,
        icon = Icons.Default.BarChart,
    ),
    ;

    companion object {
        fun fromRoute(route: String?): BlackjackDestination =
            entries.firstOrNull { it.route == route } ?: GAME
    }
}
