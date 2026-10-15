package com.example.tp1_blackjack

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.tp1_blackjack.repository.PlayerProfileRepository
import org.junit.Before
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.tp1_blackjack.model.cards.Card
import com.example.tp1_blackjack.model.cards.CardSource
import com.example.tp1_blackjack.model.cards.Rank
import com.example.tp1_blackjack.model.cards.Suit
import com.example.tp1_blackjack.model.game.BlackjackEngine
import com.example.tp1_blackjack.model.game.GamePhase
import com.example.tp1_blackjack.model.game.PlayerHandStatus
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.components.FACE_DOWN_CARD_TEST_TAG
import com.example.tp1_blackjack.ui.components.FACE_UP_CARD_TEST_TAG
import com.example.tp1_blackjack.ui.scaffold.GAME_FAB_TEST_TAG
import com.example.tp1_blackjack.ui.screens.STAND_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.screens.BET_INPUT_TEST_TAG
import com.example.tp1_blackjack.ui.screens.ACTIVE_HAND_TEST_TAG_PREFIX
import com.example.tp1_blackjack.ui.screens.DECK_COUNT_OPTION_TEST_TAG_PREFIX
import com.example.tp1_blackjack.ui.screens.SPLIT_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.screens.RESULT_NEW_ROUND_TEST_TAG
import com.example.tp1_blackjack.ui.screens.RESULT_REVIEW_TABLE_TEST_TAG
import com.example.tp1_blackjack.ui.screens.ROUND_RESULT_SCREEN_TEST_TAG
import com.example.tp1_blackjack.ui.screens.ROUND_CONTENT_TEST_TAG
import com.example.tp1_blackjack.ui.screens.REVIEW_TO_RESULT_TEST_TAG
import com.example.tp1_blackjack.ui.screens.START_ROUND_TEST_TAG
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import com.example.tp1_blackjack.viewmodel.GameViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class GameFlowTest {
    @Before fun registerTestPlayer() {
        PlayerProfileRepository(ApplicationProvider.getApplicationContext<Context>())
            .saveName("Testeur")
    }

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
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
        composeRule.onNodeWithTag(FACE_DOWN_CARD_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Croupier · carte cachée").assertIsDisplayed()
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).assertIsDisplayed().performClick()

        composeRule.onNodeWithTag(FACE_DOWN_CARD_TEST_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithTag(STAND_ACTION_TEST_TAG).assertCountEquals(0)
        waitForTag(ROUND_RESULT_SCREEN_TEST_TAG)
        composeRule.onAllNodesWithTag(FACE_DOWN_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithTag(ROUND_RESULT_SCREEN_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Victoire !").assertIsDisplayed()
        composeRule.onNodeWithText("1050 jetons").assertIsDisplayed()
        composeRule.onAllNodesWithTag(GAME_FAB_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithTag(RESULT_REVIEW_TABLE_TEST_TAG).performClick()
        composeRule.onNodeWithTag(ROUND_CONTENT_TEST_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithTag(STAND_ACTION_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithTag(REVIEW_TO_RESULT_TEST_TAG).performClick()
        composeRule.onNodeWithTag(ROUND_RESULT_SCREEN_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(RESULT_NEW_ROUND_TEST_TAG).performClick()
        composeRule.onNodeWithText("Prêt à commencer un nouveau match ?").assertIsDisplayed()
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(BET_INPUT_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + 1).assertIsDisplayed()

        // The game tab should return to setup, not the previous completed result.
        composeRule.onNodeWithTag("destination-history").performClick()
        composeRule.onNodeWithTag("destination-preparation").performClick()
        composeRule.onNodeWithTag(BET_INPUT_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun invalidBet_disablesStartUntilValueIsValid() {
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
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).assertIsDisplayed().assertIsNotEnabled()

        composeRule.onNodeWithTag(BET_INPUT_TEST_TAG).performTextReplacement("75")
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).assertIsDisplayed().assertIsEnabled()
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
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
        composeRule.onNodeWithTag(SPLIT_ACTION_TEST_TAG).assertIsDisplayed().performClick()

        composeRule.onAllNodesWithTag(SPLIT_ACTION_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 1).assertIsDisplayed()
        composeRule.onAllNodesWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 2).assertCountEquals(0)
        waitForEnabledTag(STAND_ACTION_TEST_TAG)
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performClick()
        composeRule.onAllNodesWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 1).assertCountEquals(0)
        composeRule.onNodeWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 2).assertIsDisplayed()
        waitForEnabledTag(STAND_ACTION_TEST_TAG)
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performClick()
        waitForTag(ROUND_RESULT_SCREEN_TEST_TAG)
        composeRule.onNodeWithText("Résultats partagés").assertIsDisplayed()
        composeRule.onNodeWithText("Main 1 · Score 18 · Vous gagnez · +50 jetons").assertIsDisplayed()
        composeRule.onNodeWithText("Main 2 · Score 17 · Égalité · +0 jetons").assertIsDisplayed()
    }

    @Test
    fun splitFirstHandBust_keepsSecondHandActiveUntilItIsPlayed() {
        val viewModel = GameViewModel(
            BlackjackEngine(
                FixedCardSource(
                    card(Rank.FIVE), card(Rank.TEN), card(Rank.FIVE), card(Rank.SEVEN),
                    card(Rank.FOUR), card(Rank.SIX), card(Rank.FIVE), card(Rank.FIVE),
                    card(Rank.TEN), card(Rank.KING),
                ),
            ),
        )
        composeRule.setContent {
            TP1_BlackJackTheme { BlackjackApp(gameViewModel = viewModel) }
        }

        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
        waitForEnabledTag(SPLIT_ACTION_TEST_TAG)
        composeRule.onNodeWithTag(SPLIT_ACTION_TEST_TAG).performClick()
        repeat(3) {
            waitForEnabledTag(GAME_FAB_TEST_TAG)
            composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        }
        waitForEnabledTag(GAME_FAB_TEST_TAG)

        val afterFirstHandBust = viewModel.uiState.value
        assertEquals(GamePhase.PLAYER_TURN, afterFirstHandBust.phase)
        assertEquals(PlayerHandStatus.BUST, afterFirstHandBust.playerHands[0].status)
        assertEquals(29, afterFirstHandBust.playerHands[0].score)
        assertEquals(PlayerHandStatus.ACTIVE, afterFirstHandBust.playerHands[1].status)
        assertEquals(11, afterFirstHandBust.playerHands[1].score)
        composeRule.onAllNodesWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 1).assertCountEquals(0)
        composeRule.onNodeWithTag(ACTIVE_HAND_TEST_TAG_PREFIX + 2).assertIsDisplayed()
        composeRule.onAllNodesWithTag(ROUND_RESULT_SCREEN_TEST_TAG).assertCountEquals(0)

        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        waitForEnabledTag(STAND_ACTION_TEST_TAG)
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performClick()
        waitForTag(ROUND_RESULT_SCREEN_TEST_TAG)

        assertEquals(GamePhase.COMPLETE, viewModel.uiState.value.phase)
        assertEquals(21, viewModel.uiState.value.playerHands[1].score)
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

        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
        composeRule.onAllNodesWithTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + 1).assertCountEquals(0)
        composeRule.onAllNodesWithTag(DECK_COUNT_OPTION_TEST_TAG_PREFIX + 4).assertCountEquals(0)
    }

    @Test
    fun hit_revealsNewCardBeforeAllowingAnotherAction() {
        val viewModel = GameViewModel(
            BlackjackEngine(
                FixedCardSource(
                    card(Rank.FIVE), card(Rank.TEN), card(Rank.SIX), card(Rank.SEVEN),
                    card(Rank.FOUR),
                ),
            ),
        )
        composeRule.setContent {
            TP1_BlackJackTheme { BlackjackApp(gameViewModel = viewModel) }
        }

        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
        waitForTag(GAME_FAB_TEST_TAG)
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        composeRule.onAllNodesWithTag(GAME_FAB_TEST_TAG).assertCountEquals(0)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(FACE_UP_CARD_TEST_TAG).fetchSemanticsNodes().size == 4
        }
        waitForTag(GAME_FAB_TEST_TAG)
        composeRule.onNodeWithText("Joueur · 15").assertIsDisplayed()
    }

    @Test
    fun naturalBlackjack_showsTableTransitionBeforeResult() {
        val viewModel = GameViewModel(
            BlackjackEngine(
                FixedCardSource(
                    card(Rank.ACE), card(Rank.FIVE), card(Rank.KING), card(Rank.SIX),
                ),
            ),
        )
        composeRule.setContent {
            TP1_BlackJackTheme { BlackjackApp(gameViewModel = viewModel) }
        }

        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
        composeRule.onAllNodesWithTag(ROUND_RESULT_SCREEN_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithTag(ROUND_CONTENT_TEST_TAG).assertIsDisplayed()
        waitForTag(ROUND_RESULT_SCREEN_TEST_TAG)
        composeRule.onNodeWithText("Victoire !").assertIsDisplayed()
        composeRule.onNodeWithText("Vous avez obtenu un Blackjack naturel.").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(8_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForEnabledTag(tag: String) {
        composeRule.waitUntil(5_000) {
            runCatching { composeRule.onNodeWithTag(tag).assertIsEnabled() }.isSuccess
        }
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
