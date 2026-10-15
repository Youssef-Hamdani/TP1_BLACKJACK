package com.example.tp1_blackjack

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.tp1_blackjack.database.AppDatabase
import com.example.tp1_blackjack.model.results.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.GameAction
import com.example.tp1_blackjack.repository.GameRepository
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import com.example.tp1_blackjack.viewmodel.GameViewModel
import com.example.tp1_blackjack.viewmodel.RecordsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RecordsFlowTest {
    @get:Rule val composeRule = createComposeRule()
    private lateinit var database: AppDatabase
    private lateinit var repository: GameRepository
    private val store = ViewModelStore()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = GameRepository(database)
    }

    @After
    fun close() {
        composeRule.runOnIdle { store.clear() }
        database.close()
    }

    @Test
    fun roomInsertAndRevealUpdateVisibleCountersWithoutRefresh() {
        val records = RecordsViewModel(repository)
        val game = GameViewModel()
        store.put("records", records)
        store.put("game", game)
        composeRule.setContent {
            TP1_BlackJackTheme {
                BlackjackApp(gameViewModel = game, recordsViewModel = records)
            }
        }
        composeRule.onNodeWithTag("destination-statistics").performClick()
        waitForText("Aucune statistique disponible")
        runBlocking {
            repository.createGame(GameRecord("flow", 10, 1, 50))
            repository.recordDraws(listOf(
                CardDrawRecord("flow", 0, Card(Rank.TEN, Suit.CLUBS), CardOwner.PLAYER, 0, true),
                CardDrawRecord("flow", 1, Card(Rank.SEVEN, Suit.HEARTS), CardOwner.DEALER, null, false),
            ))
        }
        waitForText("1 carte observée")
        runBlocking { repository.revealCard("flow", 1) }
        waitForText("2 cartes observées")
        composeRule.onNodeWithTag("destination-history").performClick()
        waitForText("Aucune partie enregistrée")
        runBlocking {
            repository.settleGame("flow", 20, listOf(HandRecord("flow", 0, 50, HandOutcome.PUSH, 0)), 1_000)
        }
        waitForText("1 partie enregistrée")
        composeRule.onNodeWithText("1 partie enregistrée").assertIsDisplayed()
    }

    @Test
    fun gameViewModelLoadsSettingsAndSavesBetInItsScope() {
        runBlocking { repository.setDefaultBet(65) }
        lateinit var game: GameViewModel
        composeRule.runOnIdle {
            game = GameViewModel(repository = repository)
            store.put("game", game)
            assertTrue(game.uiState.value.isLoading)
            assertTrue(game.uiState.value.availableActions.isEmpty())
        }
        composeRule.waitUntil(5_000) { !game.uiState.value.isLoading }
        assertEquals(65, game.uiState.value.selectedBet)
        composeRule.runOnIdle { game.onBetInputChanged("75") }
        runBlocking {
            withTimeout(5_000) { repository.savedWallet.first { it.defaultBet == 75 } }
        }
        lateinit var restored: GameViewModel
        composeRule.runOnIdle {
            restored = GameViewModel(repository = repository)
            store.put("restored", restored)
        }
        composeRule.waitUntil(5_000) { !restored.uiState.value.isLoading }
        assertEquals("75", restored.uiState.value.betInput)
        assertTrue(GameAction.START_ROUND in restored.uiState.value.availableActions)
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
