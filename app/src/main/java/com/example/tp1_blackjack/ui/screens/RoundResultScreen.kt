package com.example.tp1_blackjack.ui.screens

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.model.HandOutcome
import com.example.tp1_blackjack.model.PlayerHandStatus
import com.example.tp1_blackjack.ui.components.TableSurface
import com.example.tp1_blackjack.viewmodel.GameUiState
import com.example.tp1_blackjack.viewmodel.PlayerHandUiState

const val ROUND_RESULT_SCREEN_TEST_TAG = "round-result-screen"
const val RESULT_NEW_ROUND_TEST_TAG = "result-new-round"
const val RESULT_REVIEW_TABLE_TEST_TAG = "result-review-table"

@Composable
fun RoundResultScreen(
    uiState: GameUiState,
    onNewRound: () -> Unit,
    onReviewTable: () -> Unit,
) {
    val presentation = resultPresentation(uiState)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    TableSurface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .testTag(ROUND_RESULT_SCREEN_TEST_TAG),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.72f),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ResultSummary(uiState, presentation, Modifier.weight(1f))
                        ResultHandsAndActions(
                            uiState,
                            onNewRound,
                            onReviewTable,
                            Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultSummary(uiState, presentation)
                        ResultHandsAndActions(uiState, onNewRound, onReviewTable)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSummary(
    uiState: GameUiState,
    presentation: ResultPresentation,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(presentation.title),
            color = presentation.color,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(stringResource(presentation.explanation))
        uiState.dealerScore?.let {
            Text(stringResource(R.string.result_dealer_score, it))
        } ?: Text(stringResource(R.string.result_dealer_did_not_play))
        uiState.netChange?.let {
            Text(
                pluralStringResource(R.plurals.result_net_change, kotlin.math.abs(it), it),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(pluralStringResource(R.plurals.history_balance, uiState.balance, uiState.balance))
    }
}

@Composable
private fun ResultHandsAndActions(
    uiState: GameUiState,
    onNewRound: () -> Unit,
    onReviewTable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        uiState.playerHands.forEachIndexed { index, hand ->
            HandResult(index + 1, uiState.playerHands.size > 1, hand)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            OutlinedButton(
                onClick = onReviewTable,
                modifier = Modifier.testTag(RESULT_REVIEW_TABLE_TEST_TAG),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                ),

                ) { Text(stringResource(R.string.action_review_table)) }
            Button(
                onClick = onNewRound,
                modifier = Modifier.testTag(RESULT_NEW_ROUND_TEST_TAG),
            ) { Text(stringResource(R.string.action_new_round)) }
        }
    }
}

@Composable
private fun HandResult(number: Int, numbered: Boolean, hand: PlayerHandUiState) {
    val outcome = hand.outcome ?: return
    val delta = hand.netChange ?: 0
    val outcomeText = stringResource(outcome.stringResourceId())
    Text(
        if (numbered) {
            stringResource(R.string.result_numbered_hand, number, hand.score, outcomeText, delta)
        } else {
            stringResource(R.string.result_player_score, hand.score, outcomeText)
        },
    )
}

private data class ResultPresentation(
    @param:StringRes val title: Int,
    @param:StringRes val explanation: Int,
    val color: Color,
)

private fun resultPresentation(uiState: GameUiState): ResultPresentation {
    val outcomes = uiState.playerHands.mapNotNull { it.outcome }
    val hasWin = outcomes.any { it == HandOutcome.PLAYER_WIN || it == HandOutcome.PLAYER_BLACKJACK }
    val hasLoss = outcomes.any { it == HandOutcome.DEALER_WIN }
    val hasPush = outcomes.any { it == HandOutcome.PUSH }
    val isMixed = listOf(hasWin, hasLoss, hasPush).count { it } > 1
    val singleHand = uiState.playerHands.singleOrNull()
    val title = when {
        isMixed -> R.string.result_mixed
        hasWin -> R.string.result_win
        hasLoss -> R.string.result_loss
        else -> R.string.result_push
    }
    val explanation = when {
        isMixed -> R.string.result_explanation_mixed
        singleHand?.status == PlayerHandStatus.BUST -> R.string.result_explanation_player_bust
        singleHand?.outcome == HandOutcome.PLAYER_BLACKJACK -> R.string.result_explanation_blackjack
        uiState.dealerScore?.let { it > 21 } == true -> R.string.result_explanation_dealer_bust
        hasWin -> R.string.result_explanation_higher_player
        hasLoss -> R.string.result_explanation_higher_dealer
        else -> R.string.result_explanation_equal
    }
    val color = when {
        isMixed || hasPush -> Color(0xFFFFD166)
        hasWin -> Color(0xFF7DDE92)
        else -> Color(0xFFFF8A80)
    }
    return ResultPresentation(title, explanation, color)
}
