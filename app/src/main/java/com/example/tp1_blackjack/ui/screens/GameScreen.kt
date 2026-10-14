package com.example.tp1_blackjack.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.model.GameAction
import com.example.tp1_blackjack.model.HandOutcome
import com.example.tp1_blackjack.model.PlayerHandStatus
import com.example.tp1_blackjack.ui.components.CardView
import com.example.tp1_blackjack.ui.components.TableSurface
import com.example.tp1_blackjack.viewmodel.DisplayCardUiState
import com.example.tp1_blackjack.viewmodel.GameUiState
import com.example.tp1_blackjack.viewmodel.PlayerHandUiState

const val ROUND_CONTENT_TEST_TAG = "round-content"
const val STAND_ACTION_TEST_TAG = "stand-action"
const val SPLIT_ACTION_TEST_TAG = "split-action"
const val ACTIVE_HAND_TEST_TAG_PREFIX = "active-hand-"
const val REVIEW_TO_RESULT_TEST_TAG = "review-to-result"
const val REVIEW_NEW_ROUND_TEST_TAG = "review-new-round"

@Composable
fun GameScreen(
    uiState: GameUiState,
    onGameAction: (GameAction) -> Unit,
    onRetrySave: () -> Unit = {},
    isReviewMode: Boolean = false,
    onBackToResult: () -> Unit = {},
    onNewRound: () -> Unit = {},
) {
    TableSurface(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> StatusMessage(R.string.storage_loading)
            uiState.hasStorageError && uiState.playerHands.isEmpty() -> StatusMessage(R.string.storage_error)
            else -> RoundContent(
                uiState = uiState,
                onStand = { onGameAction(GameAction.STAND) },
                onSplit = { onGameAction(GameAction.SPLIT) },
                onRetrySave = onRetrySave,
                isReviewMode = isReviewMode,
                onBackToResult = onBackToResult,
                onNewRound = onNewRound,
            )
        }
    }
}

@Composable
private fun StatusMessage(@StringRes message: Int) {
    Text(stringResource(message), color = Color.White, modifier = Modifier.padding(24.dp))
}

@Composable
private fun RoundContent(
    uiState: GameUiState,
    onStand: () -> Unit,
    onSplit: () -> Unit,
    onRetrySave: () -> Unit,
    isReviewMode: Boolean,
    onBackToResult: () -> Unit,
    onNewRound: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp)
            .testTag(ROUND_CONTENT_TEST_TAG),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isReviewMode) {
            Text(
                text = stringResource(R.string.table_review_explanation),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBackToResult,
                    modifier = Modifier.testTag(REVIEW_TO_RESULT_TEST_TAG),
                ) { Text(stringResource(R.string.action_back_to_result)) }
                Button(
                    onClick = onNewRound,
                    modifier = Modifier.testTag(REVIEW_NEW_ROUND_TEST_TAG),
                ) { Text(stringResource(R.string.action_new_round)) }
            }
        }
        if (uiState.isSaving) Text(stringResource(R.string.round_saving), color = Color.White)
        if (uiState.canRetrySave) {
            Text(stringResource(R.string.round_save_error), color = Color(0xFFFFB4AB))
            Button(onClick = onRetrySave, modifier = Modifier.testTag("retry-save")) {
                Text(stringResource(R.string.retry_save))
            }
        }

        DealerPanel(uiState.dealerCards, uiState.dealerScore)
        uiState.playerHands.forEachIndexed { index, hand ->
            PlayerPanel(index + 1, uiState.playerHands.size > 1, hand)
        }

        if (!isReviewMode) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (GameAction.STAND in uiState.availableActions) {
                    Button(onClick = onStand, modifier = Modifier.testTag(STAND_ACTION_TEST_TAG)) {
                        Text(stringResource(R.string.action_stand))
                    }
                }
                if (GameAction.SPLIT in uiState.availableActions) {
                    OutlinedButton(onClick = onSplit, modifier = Modifier.testTag(SPLIT_ACTION_TEST_TAG)) {
                        Text(stringResource(R.string.action_split))
                    }
                }
            }
        }
    }
}

@Composable
private fun DealerPanel(cards: List<DisplayCardUiState>, score: Int?) {
    HandPanel(
        title = stringResource(
            R.string.dealer_hand_title,
            score?.toString() ?: stringResource(R.string.score_hidden),
        ),
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cards.forEach { CardView(it.card, it.isFaceDown) }
        }
    }
}

@Composable
private fun PlayerPanel(handNumber: Int, showHandNumber: Boolean, hand: PlayerHandUiState) {
    val title = if (showHandNumber) {
        stringResource(R.string.player_numbered_hand_title, handNumber, hand.score)
    } else {
        stringResource(R.string.player_hand_title, hand.score)
    }
    HandPanel(
        title = title,
        isActive = hand.isActive,
        modifier = if (hand.isActive) Modifier.testTag(ACTIVE_HAND_TEST_TAG_PREFIX + handNumber) else Modifier,
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            hand.cards.forEach { CardView(it, isFaceDown = false) }
        }
        if (hand.status == PlayerHandStatus.BUST) {
            Text(stringResource(R.string.hand_bust), color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold)
        }
        hand.outcome?.let {
            Text(
                stringResource(it.stringResourceId()),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (hand.isActive) {
            Text(
                stringResource(R.string.active_hand),
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HandPanel(
    title: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.58f),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@StringRes
internal fun HandOutcome.stringResourceId(): Int = when (this) {
    HandOutcome.PLAYER_WIN -> R.string.outcome_player_win
    HandOutcome.DEALER_WIN -> R.string.outcome_dealer_win
    HandOutcome.PUSH -> R.string.outcome_push
    HandOutcome.PLAYER_BLACKJACK -> R.string.outcome_player_blackjack
}
