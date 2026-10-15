package com.example.tp1_blackjack.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.model.game.GamePhase
import com.example.tp1_blackjack.model.game.PlayerHandStatus
import com.example.tp1_blackjack.model.results.HandOutcome
import com.example.tp1_blackjack.ui.components.CardView
import com.example.tp1_blackjack.ui.components.TableSurface
import com.example.tp1_blackjack.viewmodel.DisplayCardUiState
import com.example.tp1_blackjack.viewmodel.GameUiState
import com.example.tp1_blackjack.viewmodel.PlayerHandUiState
import kotlinx.coroutines.delay

const val ROUND_CONTENT_TEST_TAG = "round-content"
const val STAND_ACTION_TEST_TAG = "stand-action"
const val SPLIT_ACTION_TEST_TAG = "split-action"
const val ACTIVE_HAND_TEST_TAG_PREFIX = "active-hand-"
const val REVIEW_TO_RESULT_TEST_TAG = "review-to-result"
const val REVIEW_NEW_ROUND_TEST_TAG = "review-new-round"

private data class TablePresentation(
    val playerCardCounts: List<Int> = emptyList(),
    val playerScores: List<Int?> = emptyList(),
    val dealerCardCount: Int = 0,
    val revealDealerHoleCard: Boolean = false,
    val dealerScore: Int? = null,
)

@Composable
private fun rememberTablePresentation(
    uiState: GameUiState,
    animateChanges: Boolean,
    onPresentationIdle: () -> Unit,
    onRoundPresentationFinished: () -> Unit,
): TablePresentation {
    fun finalPresentation() = TablePresentation(
        playerCardCounts = uiState.playerHands.map { it.cards.size },
        playerScores = uiState.playerHands.map { it.score },
        dealerCardCount = uiState.dealerCards.size,
        revealDealerHoleCard = uiState.dealerCards.firstOrNull()?.isFaceDown == false,
        dealerScore = uiState.dealerScore,
    )

    var presentation by remember {
        mutableStateOf(if (animateChanges) TablePresentation() else finalPresentation())
    }
    var completionAnimationFinished by remember { mutableStateOf(false) }
    var resultNavigationRequested by remember { mutableStateOf(false) }

    LaunchedEffect(
        uiState.phase,
        uiState.playerHands.map { it.cards },
        uiState.playerHands.map { it.status to it.isActive },
        uiState.dealerCards,
    ) {
        if (!animateChanges) {
            presentation = finalPresentation()
            completionAnimationFinished = uiState.phase == GamePhase.COMPLETE
            return@LaunchedEffect
        }

        val targetHands = uiState.playerHands
        if (presentation.playerCardCounts.size != targetHands.size) {
            val splitOccurred = presentation.playerCardCounts.isNotEmpty() && targetHands.size > 1
            presentation = presentation.copy(
                playerCardCounts = targetHands.map { if (splitOccurred) minOf(1, it.cards.size) else 0 },
                playerScores = List(targetHands.size) { null },
            )
        }
        targetHands.forEachIndexed { handIndex, hand ->
            while (presentation.playerCardCounts.getOrElse(handIndex) { 0 } < hand.cards.size) {
                delay(180)
                val counts = presentation.playerCardCounts.toMutableList()
                counts[handIndex] = counts[handIndex] + 1
                presentation = presentation.copy(playerCardCounts = counts)
            }
            delay(120)
            val scores = presentation.playerScores.toMutableList()
            scores[handIndex] = hand.score
            presentation = presentation.copy(playerScores = scores)
        }

        val dealerTargetCount = uiState.dealerCards.size
        while (presentation.dealerCardCount < minOf(2, dealerTargetCount)) {
            delay(180)
            presentation = presentation.copy(dealerCardCount = presentation.dealerCardCount + 1)
        }
        if (uiState.dealerCards.firstOrNull()?.isFaceDown == false &&
            !presentation.revealDealerHoleCard
        ) {
            delay(220)
            presentation = presentation.copy(revealDealerHoleCard = true)
        }
        while (presentation.dealerCardCount < dealerTargetCount) {
            delay(300)
            presentation = presentation.copy(dealerCardCount = presentation.dealerCardCount + 1)
        }
        if (uiState.dealerScore != null) {
            delay(120)
            presentation = presentation.copy(dealerScore = uiState.dealerScore)
        }

        if (uiState.phase == GamePhase.COMPLETE) {
            delay(1_250)
            completionAnimationFinished = true
        } else {
            completionAnimationFinished = false
            resultNavigationRequested = false
        }
        onPresentationIdle()
    }

    LaunchedEffect(
        completionAnimationFinished,
        uiState.isSaving,
        uiState.canRetrySave,
        uiState.hasStorageError,
    ) {
        val savedSuccessfully = !uiState.isSaving && !uiState.canRetrySave && !uiState.hasStorageError
        if (uiState.phase == GamePhase.COMPLETE && completionAnimationFinished &&
            savedSuccessfully && !resultNavigationRequested
        ) {
            resultNavigationRequested = true
            onRoundPresentationFinished()
        }
    }
    return presentation
}

@Composable
fun GameScreen(
    uiState: GameUiState,
    onGameAction: (GameAction) -> Unit,
    onRetrySave: () -> Unit = {},
    isPresentationBusy: Boolean = false,
    onPresentationIdle: () -> Unit = {},
    onRoundPresentationFinished: () -> Unit = {},
    isReviewMode: Boolean = false,
    onBackToResult: () -> Unit = {},
    onNewRound: () -> Unit = {},
) {
    val presentation = rememberTablePresentation(
        uiState = uiState,
        animateChanges = isPresentationBusy && !isReviewMode,
        onPresentationIdle = onPresentationIdle,
        onRoundPresentationFinished = onRoundPresentationFinished,
    )
    TableSurface(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> StatusMessage(R.string.storage_loading)
            uiState.hasStorageError && uiState.playerHands.isEmpty() -> StatusMessage(R.string.storage_error)
            else -> RoundContent(
                uiState = uiState,
                presentation = presentation,
                onStand = { onGameAction(GameAction.STAND) },
                onSplit = { onGameAction(GameAction.SPLIT) },
                onRetrySave = onRetrySave,
                isReviewMode = isReviewMode,
                onBackToResult = onBackToResult,
                onNewRound = onNewRound,
                actionsEnabled = !isPresentationBusy,
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
    presentation: TablePresentation,
    onStand: () -> Unit,
    onSplit: () -> Unit,
    onRetrySave: () -> Unit,
    isReviewMode: Boolean,
    onBackToResult: () -> Unit,
    onNewRound: () -> Unit,
    actionsEnabled: Boolean,
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF1E1E1E),
                        contentColor = Color.White
                    ),
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

        DealerPanel(
            cards = uiState.dealerCards,
            visibleCardCount = presentation.dealerCardCount,
            revealHoleCard = presentation.revealDealerHoleCard,
            score = presentation.dealerScore,
        )
        uiState.playerHands.forEachIndexed { index, hand ->
            PlayerPanel(
                handNumber = index + 1,
                showHandNumber = uiState.playerHands.size > 1,
                hand = hand,
                visibleCardCount = presentation.playerCardCounts.getOrElse(index) { 0 },
                displayedScore = presentation.playerScores.getOrNull(index),
                showOutcome = isReviewMode,
            )
        }

        if (!isReviewMode) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (GameAction.STAND in uiState.availableActions) {
                    Button(
                        onClick = onStand,
                        enabled = actionsEnabled,
                        modifier = Modifier.testTag(STAND_ACTION_TEST_TAG),
                    ) {
                        Text(stringResource(R.string.action_stand))
                    }
                }
                if (GameAction.SPLIT in uiState.availableActions) {
                    OutlinedButton(
                        onClick = onSplit,
                        enabled = actionsEnabled,
                        modifier = Modifier.testTag(SPLIT_ACTION_TEST_TAG),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF1E1E1E),
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.action_split))
                    }
                }
            }
        }
    }
}

@Composable
private fun DealerPanel(
    cards: List<DisplayCardUiState>,
    visibleCardCount: Int,
    revealHoleCard: Boolean,
    score: Int?,
) {
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
            cards.forEachIndexed { index, displayCard ->
                AnimatedVisibility(
                    visible = index < visibleCardCount,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                ) {
                    val keepHoleCardHidden = index == 0 && !revealHoleCard
                    CardView(
                        card = displayCard.card.takeUnless { keepHoleCardHidden },
                        isFaceDown = displayCard.isFaceDown || keepHoleCardHidden,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerPanel(
    handNumber: Int,
    showHandNumber: Boolean,
    hand: PlayerHandUiState,
    visibleCardCount: Int,
    displayedScore: Int?,
    showOutcome: Boolean,
) {
    val title = if (displayedScore == null) {
        if (showHandNumber) {
            stringResource(R.string.player_numbered_hand_label, handNumber)
        } else {
            stringResource(R.string.player_hand_label)
        }
    } else if (showHandNumber) {
        stringResource(R.string.player_numbered_hand_title, handNumber, displayedScore)
    } else {
        stringResource(R.string.player_hand_title, displayedScore)
    }
    HandPanel(
        title = title,
        titleColor = if (hand.status == PlayerHandStatus.BUST && displayedScore == hand.score) {
            Color(0xFFFF6B6B)
        } else {
            Color.White
        },
        isActive = hand.isActive,
        modifier = if (hand.isActive) Modifier.testTag(ACTIVE_HAND_TEST_TAG_PREFIX + handNumber) else Modifier,
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            hand.cards.forEachIndexed { index, card ->
                AnimatedVisibility(
                    visible = index < visibleCardCount,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                ) {
                    CardView(card, isFaceDown = false)
                }
            }
        }
        if (hand.status == PlayerHandStatus.BUST && displayedScore == hand.score) {
            Text(stringResource(R.string.hand_bust), color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold)
        }
        hand.outcome?.takeIf { showOutcome }?.let {
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
    titleColor: Color = Color.White,
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
            Text(
                title,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
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
