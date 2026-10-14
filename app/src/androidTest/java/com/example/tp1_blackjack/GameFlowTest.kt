package com.example.tp1_blackjack

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.tp1_blackjack.model.BlackjackEngine
import com.example.tp1_blackjack.model.Card
import com.example.tp1_blackjack.model.CardSource
import com.example.tp1_blackjack.model.Rank
import com.example.tp1_blackjack.model.Suit
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.components.FACE_DOWN_CARD_TEST_TAG
import com.example.tp1_blackjack.ui.scaffold.GAME_FAB_TEST_TAG
import com.example.tp1_blackjack.ui.screens.STAND_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.screens.BET_INPUT_TEST_TAG
import com.example.tp1_blackjack.ui.screens.ACTIVE_HAND_TEST_TAG_PREFIX
import com.example.tp1_blackjack.ui.screens.DECK_COUNT_OPTION_TEST_TAG_PREFIX
import com.example.tp1_blackjack.ui.screens.SPLIT_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import com.example.tp1_blackjack.viewmodel.GameViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class GameFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun startThenStand_revealsDealerAndShowsWinner() {
        val cardSource = FixedCardSource(
            card(Rank.TEN),
            card(Rank.FIVE),
            card(Rank.SEVEN),
            card(Rank.FIVE),
            card(Rank.SIX),
            card(Rank.KING),
        )
        val viewModel = GameViewModel(BlackjackEngine(cardSource))

        composeRule.setContent {
            TP1_BlackJackTheme {
                BlackjackApp(gameViewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("1000 jetons").assertIsDisplayed()
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        composeRule.onNodeWithTag(FACE_DOWN_CARD_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).assertIsDisplayed().performClick()

        composeRule.onAllNodesWithTag(FACE_DOWN_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText("Vous gagnez").assertIsDisplayed()
        composeRule.onNodeWithText("1050 jetons").assertIsDisplayed()
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun invalidBet_hidesStartUntilValueIsValid() {
        val viewModel = GameViewModel(
            BlackjackEngine(
                cardSource = FixedCardSource(card(Rank.TWO)),
                cardSourceFactory = {
                    FixedCardSource(
                    card(Rank.TEN),
                    card(Rank.FIVE),
                    card(Rank.SEVEN),
                    card(Rank.SIX),
                    )
                },
            ),
        )
        composeRule.setContent {
            TP1_BlackJackTheme {
                BlackjackApp(gameViewModel = viewModel)
            }
        }

        composeRule.onNodeWithTag(BET_INPUT_TEST_TAG).performTextReplacement("0")
        composeRule.onNodeWithText("La mise doit être entre 1 et 100 jetons.").assertIsDisplayed()
        composeRule.onAllNodesWithTag(GAME_FAB_TEST_TAG).assertCountEquals(0)

        composeRule.onNodeWithTag(BET_INPUT_TEST_TAG).performTextReplacement("75")
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun splitAction_appearsOnlyForPairAndMovesActiveHand() {
        val viewModel = GameViewModel(
            BlackjackEngine(
                FixedCardSource(
                    card(Rank.EIGHT),
                    card(Rank.TEN),
                    card(Rank.EIGHT),
                    card(Rank.SEVEN),
                    card(Rank.TEN),
                    card(Rank.NINE),
                ),
            ),
        )
        composeRule.setContent {
            TP1_BlackJackTheme {
                BlackjackApp(gameViewModel = viewModel)
            }
        }

        composeRule.onAllNodesWithTag(SPLIT_ACTION_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        composeRule.onNodeWithTag(SPLIT_ACTION_TEST_TAG).assertIsDisplayed().performClick()

        composeRule.onAllNodesWithTag(SPLIT_ACTION_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 1).assertIsDisplayed()
        composeRule.onAllNodesWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 2).assertCountEquals(0)
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performClick()
        composeRule.onAllNodesWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 1).assertCountEquals(0)
        composeRule.onNodeWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 2).assertIsDisplayed()
    }

    @Test
    fun deckCountSelector_updatesViewModelAndIsHiddenDuringRound() {
        val viewModel = GameViewModel(
            BlackjackEngine(
                cardSource = FixedCardSource(card(Rank.TWO)),
                cardSourceFactory = {
                    FixedCardSource(
                        card(Rank.TEN),
                        card(Rank.FIVE),
                        card(Rank.SEVEN),
                        card(Rank.SIX),
                    )
                },
            ),
        )
        composeRule.setContent {
            TP1_BlackJackTheme {
                BlackjackApp(gameViewModel = viewModel)
            }
        }

        composeRule.onNodeWithTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + 4)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()
        assertEquals(4, viewModel.uiState.value.selectedDeckCount)

        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        composeRule.onAllNodesWithTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + 1).assertCountEquals(0)
        composeRule.onAllNodesWithTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + 4).assertCountEquals(0)
    }

    private fun card(rank: Rank): Card = Card(rank, Suit.CLUBS)

    private class FixedCardSource(vararg cards: Card) : CardSource {
        private val cards = ArrayDeque(cards.toList())

        override val remainingCount: Int
            get() = cards.size

        override fun draw(): Card = cards.removeFirst()

        override fun prepareForNewRound(): Boolean = false
    }
}
