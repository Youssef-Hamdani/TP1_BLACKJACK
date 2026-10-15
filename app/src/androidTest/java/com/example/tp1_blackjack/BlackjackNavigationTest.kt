package com.example.tp1_blackjack

import androidx.activity.compose.setContent
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import androidx.lifecycle.ViewModelStore
import com.example.tp1_blackjack.database.AppDatabase
import com.example.tp1_blackjack.repository.GameRepository
import com.example.tp1_blackjack.repository.PlayerProfileRepository
import com.example.tp1_blackjack.viewmodel.PlayerProfileViewModel
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.viewmodel.GameViewModel
import com.example.tp1_blackjack.viewmodel.RecordsViewModel
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import org.junit.After
import org.junit.Before
import com.example.tp1_blackjack.ui.screens.START_ROUND_TEST_TAG
import com.example.tp1_blackjack.ui.screens.ROUND_CONTENT_TEST_TAG
import com.example.tp1_blackjack.ui.screens.SPLIT_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.screens.STAND_ACTION_TEST_TAG
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlackjackNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()
    private lateinit var database: AppDatabase
    private lateinit var game: GameViewModel
    private val store = ViewModelStore()

    @Before fun setup() {
        PlayerProfileRepository(composeRule.activity).saveName("Testeur")
        database = Room.inMemoryDatabaseBuilder(composeRule.activity, AppDatabase::class.java).build()
        val repository = GameRepository(database)
        val source = object : CardSource {
            val cards = ArrayDeque(listOf(Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX).map { Card(it, Suit.CLUBS) })
            override val remainingCount get() = cards.size
            override fun prepareForNewRound() = false
            override fun draw() = cards.removeFirst()
        }
        lateinit var records: RecordsViewModel
        composeRule.runOnIdle {
            game = GameViewModel(BlackjackEngine(source), repository)
            records = RecordsViewModel(repository)
            store.put("game", game)
            store.put("records", records)
        }
        val profile = PlayerProfileViewModel(PlayerProfileRepository(composeRule.activity))
        composeRule.activity.setContent {
            TP1_BlackJackTheme { BlackjackApp(game, records, profile) }
        }
        composeRule.waitUntil(5_000) { !game.uiState.value.isLoading }
    }

    @After fun close() {
        composeRule.runOnIdle { store.clear() }
        database.close()
    }

    @Test
    fun bottomNavigation_reachesEveryDestination_withoutDuplicatingHistory() {
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).performClick()
        composeRule.waitUntil(5_000) { !game.uiState.value.isSaving }
        composeRule.onNodeWithTag(ROUND_CONTENT_TEST_TAG).assertIsDisplayed()
        val cardsBeforeNavigation = game.uiState.value.playerHands.single().cards
        assertEquals(GamePhase.PLAYER_TURN, game.uiState.value.phase)
        assertEquals(950, game.uiState.value.balance)
        composeRule.onAllNodesWithTag(SPLIT_ACTION_TEST_TAG).assertCountEquals(0)

        composeRule.onNodeWithTag("destination-history").performClick()
        composeRule.onNodeWithText("Aucune partie enregistrée").assertIsDisplayed()

        composeRule.onNodeWithTag("destination-history").performClick()
        pressBack()
        composeRule.onNodeWithTag(ROUND_CONTENT_TEST_TAG).assertIsDisplayed()
        assertEquals(cardsBeforeNavigation, game.uiState.value.playerHands.single().cards)
        assertEquals(950, game.uiState.value.balance)
        composeRule.waitUntil(6_000) {
            runCatching {
                composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).assertIsEnabled()
            }.isSuccess
        }
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithTag(SPLIT_ACTION_TEST_TAG).assertCountEquals(0)

        composeRule.onNodeWithTag("destination-statistics").performClick()
        composeRule.waitUntil(5_000) { !composeRule.onAllNodesWithText("3 cartes observées").fetchSemanticsNodes().isEmpty() }
        composeRule.onNodeWithText("3 cartes observées").assertIsDisplayed()
        pressBack()
        composeRule.onNodeWithTag(ROUND_CONTENT_TEST_TAG).assertIsDisplayed()
        assertEquals(cardsBeforeNavigation, game.uiState.value.playerHands.single().cards)
    }
}
