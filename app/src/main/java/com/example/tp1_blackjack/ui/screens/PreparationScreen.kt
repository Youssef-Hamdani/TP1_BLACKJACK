package com.example.tp1_blackjack.ui.screens

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.ui.components.TableSurface
import com.example.tp1_blackjack.viewmodel.BetInputError
import com.example.tp1_blackjack.viewmodel.GameUiState

const val BET_INPUT_TEST_TAG = "bet-input"
const val DECK_COUNT_OPTION_TEST_TAG_PREFIX = "deck-count-"
const val DECK_SCROLL_INDICATOR_TEST_TAG = "deck-scroll-indicator"
const val START_ROUND_TEST_TAG = "start-round"

@Composable
fun PreparationScreen(
    uiState: GameUiState,
    onBetInputChanged: (String) -> Unit,
    onDeckCountSelected: (Int) -> Unit,
    onStart: () -> Unit,
    isPresentationBusy: Boolean,
) {
    TableSurface(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Text(
                stringResource(R.string.storage_loading),
                color = Color.White,
                modifier = Modifier.padding(24.dp),
            )
            uiState.hasStorageError && uiState.availableActions.isEmpty() -> Text(
                stringResource(R.string.storage_error),
                color = Color.White,
                modifier = Modifier.padding(24.dp),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.game_placeholder_title),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                BetEditor(
                    uiState = uiState,
                    onBetInputChanged = onBetInputChanged,
                    onDeckCountSelected = onDeckCountSelected,
                    onStart = onStart,
                isPresentationBusy = isPresentationBusy,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun BetEditor(
    uiState: GameUiState,
    onBetInputChanged: (String) -> Unit,
    onDeckCountSelected: (Int) -> Unit,
    onStart: () -> Unit,
    isPresentationBusy: Boolean,
    modifier: Modifier = Modifier,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Surface(
        color = Color.Black.copy(alpha = 0.66f),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BetInput(uiState, onBetInputChanged, Modifier.weight(1f))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DeckCountSelector(
                        uiState.selectedDeckCount,
                        uiState.supportedDeckCounts,
                        uiState.canSelectDeckCount,
                        onDeckCountSelected,
                    )
                    StartButton(uiState, isPresentationBusy, onStart)
                }
            }
        } else {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BetInput(uiState, onBetInputChanged)
                DeckCountSelector(
                    uiState.selectedDeckCount,
                    uiState.supportedDeckCounts,
                    uiState.canSelectDeckCount,
                    onDeckCountSelected,
                )
                StartButton(uiState, isPresentationBusy, onStart)
            }
        }
    }
}

@Composable
private fun BetInput(
    uiState: GameUiState,
    onBetInputChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.bet_prompt),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (uiState.hasStorageError) {
            Text(stringResource(R.string.storage_write_error), color = Color(0xFFFFB4AB))
        }
        OutlinedTextField(
            value = uiState.selectedBet.toString(),
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
            modifier = Modifier.fillMaxWidth().testTag(BET_INPUT_TEST_TAG),
        )
        Text(
            pluralStringResource(R.plurals.bet_reused_hint, uiState.selectedBet, uiState.selectedBet),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun StartButton(
    uiState: GameUiState,
    isPresentationBusy: Boolean,
    onStart: () -> Unit,
) {
    Button(
        onClick = onStart,
        enabled = !isPresentationBusy && GameAction.START_ROUND in uiState.availableActions,
        modifier = Modifier.testTag(START_ROUND_TEST_TAG),
    ) {
        Text(stringResource(R.string.action_start_game))
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
        stringResource(R.string.deck_count_prompt),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        supportedDeckCounts.forEach { deckCount ->
            val buttonModifier = Modifier.testTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + deckCount)
            val label: @Composable () -> Unit = {
                Text(pluralStringResource(R.plurals.deck_count_option, deckCount, deckCount))
            }
            if (deckCount == selectedDeckCount) {
                Button(
                    onClick = { onDeckCountSelected(deckCount) },
                    enabled = enabled,
                    modifier = buttonModifier,
                    content = { label() },
                )
            } else {
                OutlinedButton(
                    onClick = { onDeckCountSelected(deckCount) },
                    enabled = enabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF1E1E1E),
                        contentColor = Color.White
                    ),
                    modifier = buttonModifier,
                    content = { label() },
                )
            }
        }
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .testTag(DECK_SCROLL_INDICATOR_TEST_TAG),
    ) {
        val trackHeight = 3.dp.toPx()
        val trackTop = (size.height - trackHeight) / 2f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.28f),
            topLeft = Offset(0f, trackTop),
            size = Size(size.width, trackHeight),
        )
        val maxScroll = scrollState.maxValue.toFloat()
        val contentWidth = size.width + maxScroll
        val thumbWidth = if (contentWidth == 0f) size.width else {
            (size.width * size.width / contentWidth).coerceIn(24.dp.toPx(), size.width)
        }
        val progress = if (maxScroll == 0f) 0f else scrollState.value / maxScroll
        drawRoundRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset((size.width - thumbWidth) * progress, trackTop),
            size = Size(thumbWidth, trackHeight),
        )
    }
}

@StringRes
private fun BetInputError.stringResourceId(): Int = when (this) {
    BetInputError.REQUIRED -> R.string.bet_error_required
    BetInputError.NOT_A_NUMBER -> R.string.bet_error_number
    BetInputError.OUT_OF_RANGE -> R.string.bet_error_range
    BetInputError.INSUFFICIENT_FUNDS -> R.string.bet_error_insufficient_funds
}
