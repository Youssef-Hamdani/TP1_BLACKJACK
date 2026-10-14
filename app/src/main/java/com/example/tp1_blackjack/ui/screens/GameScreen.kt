package com.example.tp1_blackjack.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.model.GameAction
import com.example.tp1_blackjack.model.HandOutcome
import com.example.tp1_blackjack.model.PlayerHandStatus
import com.example.tp1_blackjack.ui.components.CardView
import com.example.tp1_blackjack.ui.components.TableSurface
import com.example.tp1_blackjack.viewmodel.DisplayCardUiState
import com.example.tp1_blackjack.viewmodel.BetInputError
import com.example.tp1_blackjack.viewmodel.GameUiState
import com.example.tp1_blackjack.viewmodel.PlayerHandUiState

const val ROUND_CONTENT_TEST_TAG = "round-content"
const val STAND_ACTION_TEST_TAG = "stand-action"
const val BET_INPUT_TEST_TAG = "bet-input"
const val SPLIT_ACTION_TEST_TAG = "split-action"
const val ACTIVE_HAND_TEST_TAG_PREFIX = "active-hand-"
const val DECK_COUNT_OPTION_TEST_TAG_PREFIX = "deck-count-"

@Composable
fun GameScreen(
    uiState: GameUiState,
    onGameAction: (GameAction) -> Unit,
    onBetInputChanged: (String) -> Unit,
    onDeckCountSelected: (Int) -> Unit,
    onRetrySave: () -> Unit = {},
) {
    TableSurface(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Text(stringResource(R.string.storage_loading), color = Color.White, modifier = Modifier.padding(24.dp))
        } else if (uiState.hasStorageError && uiState.availableActions.isEmpty() && uiState.playerHands.isEmpty()) {
            Text(stringResource(R.string.storage_error), color = Color.White, modifier = Modifier.padding(24.dp))
        } else if (uiState.playerHands.isEmpty()) {
            BetSetupScreen(
                uiState = uiState,
                onBetInputChanged = onBetInputChanged,
                onDeckCountSelected = onDeckCountSelected,
            )
        } else {
            RoundContent(
                uiState = uiState,
                onStand = { onGameAction(GameAction.STAND) },
                onSplit = { onGameAction(GameAction.SPLIT) },
                onBetInputChanged = onBetInputChanged,
                onDeckCountSelected = onDeckCountSelected,
                onRetrySave = onRetrySave,
            )
        }
    }
}

@Composable
private fun RoundContent(
    uiState: GameUiState,
    onStand: () -> Unit,
    onSplit: () -> Unit,
    onBetInputChanged: (String) -> Unit,
    onDeckCountSelected: (Int) -> Unit,
    onRetrySave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp)
            .testTag(ROUND_CONTENT_TEST_TAG),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.isSaving) {
            Text(stringResource(R.string.round_saving), color = Color.White)
        }
        if (uiState.canRetrySave) {
            Text(stringResource(R.string.round_save_error), color = Color(0xFFFFB4AB))
            Button(onClick = onRetrySave, modifier = Modifier.testTag("retry-save")) {
                Text(stringResource(R.string.retry_save))
            }
        }
        DealerPanel(
            cards = uiState.dealerCards,
            score = uiState.dealerScore,
        )

        uiState.playerHands.forEachIndexed { index, hand ->
            PlayerPanel(
                handNumber = index + 1,
                showHandNumber = uiState.playerHands.size > 1,
                hand = hand,
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (GameAction.STAND in uiState.availableActions) {
                Button(
                    onClick = onStand,
                    modifier = Modifier.testTag(STAND_ACTION_TEST_TAG),
                ) {
                    Text(stringResource(R.string.action_stand))
                }
            }
            if (GameAction.SPLIT in uiState.availableActions) {
                OutlinedButton(
                    onClick = onSplit,
                    modifier = Modifier.testTag(SPLIT_ACTION_TEST_TAG),
                ) {
                    Text(stringResource(R.string.action_split))
                }
            }
        }

        if (GameAction.START_ROUND in uiState.availableActions || uiState.betInputError != null) {
            BetEditor(
                uiState = uiState,
                onBetInputChanged = onBetInputChanged,
                onDeckCountSelected = onDeckCountSelected,
            )
        }
    }
}

@Composable
private fun BetSetupScreen(
    uiState: GameUiState,
    onBetInputChanged: (String) -> Unit,
    onDeckCountSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.game_placeholder_title),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        BetEditor(
            uiState = uiState,
            onBetInputChanged = onBetInputChanged,
            onDeckCountSelected = onDeckCountSelected,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun BetEditor(
    uiState: GameUiState,
    onBetInputChanged: (String) -> Unit,
    onDeckCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.66f),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.bet_prompt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (uiState.hasStorageError) {
                Text(stringResource(R.string.storage_write_error), color = Color(0xFFFFB4AB))
            }
            OutlinedTextField(
                value = uiState.betInput,
                onValueChange = onBetInputChanged,
                label = { Text(stringResource(R.string.bet_label), color = Color.White) },
                suffix = { Text(stringResource(R.string.chips_suffix), color = Color.White) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                ),
                isError = uiState.betInputError != null,
                supportingText = uiState.betInputError?.let { error ->
                    { Text(stringResource(error.stringResourceId())) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !uiState.isSaving && !uiState.canRetrySave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(BET_INPUT_TEST_TAG),
            )
            Text(
                text = pluralStringResource(
                    R.plurals.bet_reused_hint,
                    uiState.selectedBet,
                    uiState.selectedBet,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            DeckCountSelector(
                selectedDeckCount = uiState.selectedDeckCount,
                supportedDeckCounts = uiState.supportedDeckCounts,
                enabled = uiState.canSelectDeckCount,
                onDeckCountSelected = onDeckCountSelected,
            )
        }
    }
}

@Composable
private fun DeckCountSelector(
    selectedDeckCount: Int,
    supportedDeckCounts: List<Int>,
    enabled: Boolean,
    onDeckCountSelected: (Int) -> Unit,
) {
    Text(
        text = stringResource(R.string.deck_count_prompt),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        supportedDeckCounts.chunked(2).forEach { rowDeckCounts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowDeckCounts.forEach { deckCount ->
                    val modifier = Modifier
                        .weight(1f)
                        .testTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + deckCount)
                    val content: @Composable () -> Unit = {
                        Text(
                            pluralStringResource(
                                R.plurals.deck_count_option,
                                deckCount,
                                deckCount,
                            ),
                        )
                    }
                    if (deckCount == selectedDeckCount) {
                        Button(
                            onClick = { onDeckCountSelected(deckCount) },
                            enabled = enabled,
                            modifier = modifier,
                            content = { content() },
                        )
                    } else {
                        OutlinedButton(
                            onClick = { onDeckCountSelected(deckCount) },
                            enabled = enabled,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = modifier,
                            content = { content() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DealerPanel(
    cards: List<DisplayCardUiState>,
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
            cards.forEach { displayCard ->
                CardView(
                    card = displayCard.card,
                    isFaceDown = displayCard.isFaceDown,
                )
            }
        }
    }
}

@Composable
private fun PlayerPanel(
    handNumber: Int,
    showHandNumber: Boolean,
    hand: PlayerHandUiState,
) {
    val title = if (showHandNumber) {
        stringResource(R.string.player_numbered_hand_title, handNumber, hand.score)
    } else {
        stringResource(R.string.player_hand_title, hand.score)
    }

    HandPanel(
        title = title,
        isActive = hand.isActive,
        modifier = if (hand.isActive) {
            Modifier.testTag(ACTIVE_HAND_TEST_TAG_PREFIX + handNumber)
        } else {
            Modifier
        },
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            hand.cards.forEach { card ->
                CardView(card = card, isFaceDown = false)
            }
        }

        if (hand.status == PlayerHandStatus.BUST) {
            Text(
                text = stringResource(R.string.hand_bust),
                color = Color(0xFFFFB4AB),
                fontWeight = FontWeight.Bold,
            )
        }

        hand.outcome?.let { outcome ->
            Text(
                text = stringResource(outcome.stringResourceId()),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (hand.isActive) {
            Text(
                text = stringResource(R.string.active_hand),
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
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

@StringRes
private fun BetInputError.stringResourceId(): Int = when (this) {
    BetInputError.REQUIRED -> R.string.bet_error_required
    BetInputError.NOT_A_NUMBER -> R.string.bet_error_number
    BetInputError.OUT_OF_RANGE -> R.string.bet_error_range
    BetInputError.INSUFFICIENT_FUNDS -> R.string.bet_error_insufficient_funds
}

