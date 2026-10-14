package com.example.tp1_blackjack

import android.app.UiAutomation
import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tp1_blackjack.model.BlackjackEngine
import com.example.tp1_blackjack.model.Card
import com.example.tp1_blackjack.model.CardSource
import com.example.tp1_blackjack.model.Rank
import com.example.tp1_blackjack.model.Suit
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.screens.BET_INPUT_TEST_TAG
import com.example.tp1_blackjack.ui.screens.START_ROUND_TEST_TAG
import com.example.tp1_blackjack.ui.screens.DECK_COUNT_OPTION_TEST_TAG_PREFIX
import com.example.tp1_blackjack.ui.screens.DECK_SCROLL_INDICATOR_TEST_TAG
import com.example.tp1_blackjack.ui.screens.RESULT_NEW_ROUND_TEST_TAG
import com.example.tp1_blackjack.ui.screens.RESULT_REVIEW_TABLE_TEST_TAG
import com.example.tp1_blackjack.ui.screens.ROUND_RESULT_SCREEN_TEST_TAG
import com.example.tp1_blackjack.ui.screens.STAND_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import com.example.tp1_blackjack.viewmodel.GameViewModel
import org.junit.Rule
import org.junit.Test

class CompletedRoundLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun completedRound_keepsResultAndNextRoundControlsVisibleInBothOrientations() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val viewModel = GameViewModel(
            BlackjackEngine(
                FixedCardSource(
                    card(Rank.TEN), card(Rank.FIVE), card(Rank.SEVEN),
                    card(Rank.FIVE), card(Rank.SIX), card(Rank.KING),
                ),
            ),
        )
        automation.setRotation(UiAutomation.ROTATION_FREEZE_0)
        try {
            composeRule.waitUntil(5_000) {
                composeRule.activity.resources.configuration.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
            }
            composeRule.activity.setContent {
                TP1_BlackJackTheme { BlackjackApp(gameViewModel = viewModel) }
            }
            composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
            composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performClick()
            assertCompletedControlsAreVisible()

            automation.setRotation(UiAutomation.ROTATION_FREEZE_90)
            composeRule.waitUntil(5_000) {
                composeRule.activity.resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
            }
            // Rotation recreates MainActivity, so install the same completed state on the new instance.
            composeRule.activity.setContent {
                TP1_BlackJackTheme { BlackjackApp(gameViewModel = viewModel) }
            }
            composeRule.onNodeWithTag("destination-preparation").performClick()
            assertCompletedControlsAreVisible()
            composeRule.onNodeWithTag(RESULT_NEW_ROUND_TEST_TAG).performClick()
            composeRule.onNodeWithTag(BET_INPUT_TEST_TAG).assertIsDisplayed()
            composeRule.onNodeWithTag(START_ROUND_TEST_TAG).assertIsDisplayed()
            composeRule.onNodeWithTag(DECK_SCROLL_INDICATOR_TEST_TAG).assertIsDisplayed()
            composeRule.onNodeWithTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + 6)
                .performScrollTo()
                .assertIsDisplayed()
        } finally {
            automation.setRotation(UiAutomation.ROTATION_UNFREEZE)
        }
    }

    private fun assertCompletedControlsAreVisible() {
        composeRule.onNodeWithTag(ROUND_RESULT_SCREEN_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(RESULT_NEW_ROUND_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(RESULT_REVIEW_TABLE_TEST_TAG).assertIsDisplayed()
    }

    private fun card(rank: Rank) = Card(rank, Suit.CLUBS)

    private class FixedCardSource(vararg cards: Card) : CardSource {
        private val cards = ArrayDeque(cards.toList())
        override val remainingCount: Int get() = cards.size
        override fun draw(): Card = cards.removeFirst()
        override fun prepareForNewRound(): Boolean = false
    }
}
