package com.example.tp1_blackjack

import android.content.Context
import android.app.UiAutomation
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tp1_blackjack.database.AppDatabase
import com.example.tp1_blackjack.model.*
import com.example.tp1_blackjack.repository.GameRepository
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.scaffold.GAME_FAB_TEST_TAG
import com.example.tp1_blackjack.ui.screens.STAND_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.screens.SPLIT_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import com.example.tp1_blackjack.viewmodel.GameViewModel
import com.example.tp1_blackjack.viewmodel.RecordsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class GamePersistenceFlowTest {
    @get:Rule val composeRule = createComposeRule()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var database: AppDatabase
    private lateinit var repository: GameRepository
    private val store = ViewModelStore()

    @Before fun setup() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = GameRepository(database)
    }

    @After fun close() {
        composeRule.runOnIdle { store.clear() }
        database.close()
    }

    @Test fun hitStand_updatesSqlAndHistoryThroughActualUiEvents() {
        val game = showGame(Rank.TEN, Rank.FIVE, Rank.TWO, Rank.SIX, Rank.FIVE, Rank.SIX)
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        awaitSaved(game)
        assertEquals(950, runBlocking { repository.loadWallet() }.balance)
        composeRule.onNodeWithTag("destination-statistics").performClick()
        waitForText("3 cartes observées")
        composeRule.onNodeWithText(context.getString(R.string.rank_ace)).assertIsDisplayed()
        composeRule.onNodeWithTag("destination-game").performClick()
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        awaitSaved(game)
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performClick()
        awaitSaved(game)
        assertEquals(GamePhase.COMPLETE, game.uiState.value.phase)
        val entry = runBlocking { repository.history.first().single() }
        val draws = runBlocking { repository.readDraws(entry.game.id) }
        assertEquals((0..5).toList(), draws.map { it.drawOrder })
        assertTrue(draws.all { it.isObserved })
        assertEquals(HandOutcome.PUSH, entry.hands.single().outcome)
        assertEquals(0, entry.game.netChange)
        composeRule.onNodeWithTag("destination-history").performClick()
        waitForText("1 partie enregistrée")
        composeRule.onNodeWithText("Mise totale : 50 jetons").assertIsDisplayed()
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.history_delta, 0, 0)).assertIsDisplayed()
        composeRule.onNodeWithTag("destination-statistics").performClick()
        waitForText("6 cartes observées")
        val five = runBlocking { repository.cardStatistics.first().first { it.value == 5 } }
        assertEquals(2, five.count)
        composeRule.onNodeWithTag("statistics-list").performScrollToNode(hasTestTag("stats-value-5"))
        composeRule.onNode(hasText(context.resources.getQuantityString(R.plurals.statistics_value_count, five.count, five.count, five.percentage))
            and hasAnyAncestor(hasTestTag("stats-value-5")))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("statistics-list").performScrollToNode(hasTestTag("stats-value-10"))
        composeRule.onNodeWithText(context.getString(R.string.statistics_ten)).assertIsDisplayed()
    }

    @Test fun split_persistsBothOutcomesWithoutDuplicatingMovedCard() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.setRotation(UiAutomation.ROTATION_FREEZE_90)
        try {
            composeRule.waitUntil(5_000) {
                context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }
            verifySplitPersistence()
        } finally {
            automation.setRotation(UiAutomation.ROTATION_UNFREEZE)
        }
    }

    private fun verifySplitPersistence() {
        val game = showGame(Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN, Rank.TEN, Rank.NINE)
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        awaitSaved(game)
        composeRule.onNodeWithTag(SPLIT_ACTION_TEST_TAG).performScrollTo().performClick()
        awaitSaved(game)
        assertEquals(900, runBlocking { repository.loadWallet() }.balance)
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performScrollTo().performClick()
        awaitSaved(game)
        composeRule.onNodeWithTag(STAND_ACTION_TEST_TAG).performScrollTo().performClick()
        awaitSaved(game)
        val entry = runBlocking { repository.history.first().single() }
        val draws = runBlocking { repository.readDraws(entry.game.id) }
        assertEquals(6, draws.size)
        assertEquals(1, draws.first { it.drawOrder == 2 }.handIndex)
        assertEquals(listOf(HandOutcome.PLAYER_WIN, HandOutcome.PUSH), entry.hands.map { it.outcome })
        assertEquals(50, entry.game.netChange)
        assertEquals(1_050, runBlocking { repository.loadWallet() }.balance)
        composeRule.onNodeWithTag("destination-history").performClick()
        waitForText("1 partie enregistrée")
        composeRule.onNodeWithText("Mise totale : 100 jetons").assertIsDisplayed()
        composeRule.onNodeWithText("Main 1 · Mise 50 · Vous gagnez · +50 jetons").assertIsDisplayed()
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.history_hand, 0,
            2, 50, context.getString(R.string.outcome_push), 0)).assertIsDisplayed()
        composeRule.onNodeWithText("Variation : +50 jetons").assertIsDisplayed()
    }

    @Test fun failedSave_thenRetry_doesNotDrawOrDebitAgain() {
        val game = showGame(Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX, Rank.KING)
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        awaitSaved(game)
        database.openHelper.writableDatabase.execSQL("""
            CREATE TRIGGER fail_round BEFORE UPDATE OF balance ON wallet
            BEGIN SELECT RAISE(ABORT, 'Test failure'); END
        """.trimIndent())
        composeRule.onNodeWithTag(GAME_FAB_TEST_TAG).performClick()
        composeRule.waitUntil(5_000) { game.uiState.value.canRetrySave }
        assertTrue(game.uiState.value.availableActions.isEmpty())
        assertEquals(GamePhase.COMPLETE, game.uiState.value.phase)
        assertTrue(runBlocking { repository.history.first() }.isEmpty())
        assertEquals(950, runBlocking { repository.loadWallet() }.balance)
        assertEquals(3, runBlocking { repository.cardStatistics.first() }.sumOf { it.count })
        database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_round")
        composeRule.onNodeWithTag("retry-save").performClick()
        awaitSaved(game)
        val entry = runBlocking { repository.history.first().single() }
        val cards = runBlocking { repository.readDraws(entry.game.id) }
        assertEquals(5, cards.size)
        assertFalse(cards[1].isObserved)
        assertEquals(-50, entry.game.netChange)
        assertEquals(950, entry.game.balanceAfter)
        composeRule.runOnIdle { game.onRetrySave() }
        assertEquals(1, runBlocking { repository.history.first() }.size)
    }

    @Test fun doubleTapBeforeRecomposition_doesNotStartOrDrawTwice() {
        val game = makeGame(repository, Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX)
        composeRule.runOnIdle {
            game.onGameAction(GameAction.START_ROUND)
            game.onGameAction(GameAction.START_ROUND)
        }
        awaitSaved(game)
        assertEquals(GamePhase.PLAYER_TURN, game.uiState.value.phase)
        assertEquals(950, runBlocking { repository.loadWallet() }.balance)
        assertEquals(3, runBlocking { repository.cardStatistics.first() }.sumOf { it.count })
    }

    @Test fun realNaturalAndInterruptedRound_surviveFileReopen_withoutResumingRound() {
        val name = "bj25-test-${UUID.randomUUID()}.db"
        var disk = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            var storage = GameRepository(disk)
            runBlocking { storage.setDefaultBet(75) }
            val natural = makeGame(storage, Rank.ACE, Rank.FIVE, Rank.KING, Rank.SIX)
            composeRule.runOnIdle { natural.onGameAction(GameAction.START_ROUND) }
            awaitSaved(natural)
            val finished = runBlocking { storage.history.first().single() }
            assertEquals(150, finished.game.netChange)
            assertEquals(1_150, runBlocking { storage.loadWallet() }.balance)
            // Repeating the persisted completion cannot credit the wallet again.
            runBlocking { storage.settleGame(finished.game.id, checkNotNull(finished.game.completedAt), finished.hands, 9_999) }
            composeRule.runOnIdle { store.clear() }
            disk.close()
            disk = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            storage = GameRepository(disk)
            val restarted = makeGame(storage, Rank.TEN, Rank.FIVE, Rank.SEVEN, Rank.SIX)
            assertEquals(1_150, restarted.uiState.value.balance)
            assertEquals(75, restarted.uiState.value.selectedBet)
            assertEquals(1, runBlocking { storage.history.first() }.size)
            assertEquals(4, runBlocking { storage.cardStatistics.first() }.sumOf { it.count })
            composeRule.runOnIdle { restarted.onGameAction(GameAction.START_ROUND) }
            awaitSaved(restarted)
            composeRule.runOnIdle { store.clear() }
            disk.close()
            disk = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            storage = GameRepository(disk)
            val afterInterruption = makeGame(storage, Rank.TEN)
            assertEquals(GamePhase.AWAITING_START, afterInterruption.uiState.value.phase)
            assertEquals(1_075, afterInterruption.uiState.value.balance)
            assertEquals(1, runBlocking { storage.history.first() }.size)
            assertEquals(7, runBlocking { storage.cardStatistics.first() }.sumOf { it.count })
        } finally {
            composeRule.runOnIdle { store.clear() }
            disk.close()
            context.deleteDatabase(name)
        }
    }

    private fun showGame(vararg ranks: Rank): GameViewModel {
        val game = makeGame(repository, *ranks)
        lateinit var records: RecordsViewModel
        composeRule.runOnIdle {
            records = RecordsViewModel(repository)
            store.put("records", records)
        }
        composeRule.setContent { TP1_BlackJackTheme { BlackjackApp(game, records) } }
        return game
    }

    private fun makeGame(storage: GameRepository, vararg ranks: Rank): GameViewModel {
        val source = object : CardSource {
            val cards = ArrayDeque(ranks.map { Card(it, Suit.CLUBS) })
            override val remainingCount get() = cards.size
            override fun prepareForNewRound() = false
            override fun draw() = cards.removeFirst()
        }
        lateinit var game: GameViewModel
        composeRule.runOnIdle {
            game = GameViewModel(BlackjackEngine(source), storage)
            store.put(UUID.randomUUID().toString(), game)
        }
        composeRule.waitUntil(5_000) { !game.uiState.value.isLoading }
        assertFalse(game.uiState.value.hasStorageError)
        return game
    }

    private fun awaitSaved(game: GameViewModel) {
        composeRule.waitUntil(5_000) { !game.uiState.value.isSaving }
        assertFalse(game.uiState.value.hasStorageError)
        assertFalse(game.uiState.value.canRetrySave)
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
}
