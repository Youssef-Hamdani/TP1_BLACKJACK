package com.example.tp1_blackjack

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.tp1_blackjack.database.AppDatabase
import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*
import com.example.tp1_blackjack.repository.GameRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class GameRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: GameRepository
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = GameRepository(database)
    }

    @After
    fun close() { database.close() }

    @Test
    fun emptyDatabase_hasNoStatisticsOrHistoryAndInitializesWalletOnce() = runBlocking {
        assertTrue(repository.cardStatistics.first().isEmpty())
        assertTrue(repository.history.first().isEmpty())
        assertEquals(Wallet(), repository.loadWallet())
        repository.setDefaultBet(75)
        assertEquals(Wallet(defaultBet = 75), repository.loadWallet())
    }

    @Test
    fun statistics_groupFacesAndTen_excludeHiddenCard_thenReactToReveal() = runBlocking {
        repository.createGame(game())
        repository.recordDraws(listOf(
            draw(0, Rank.ACE), draw(1, Rank.TEN), draw(2, Rank.JACK),
            draw(3, Rank.QUEEN), draw(4, Rank.KING),
            draw(5, Rank.TWO, observed = false, owner = CardOwner.DEALER),
        ))
        val initial = repository.cardStatistics.first()
        assertEquals(listOf(1, 10), initial.map { it.value })
        assertEquals(listOf(1, 4), initial.map { it.count })
        assertEquals(listOf(20.0, 80.0), initial.map { it.percentage })

        val next = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(5_000) { repository.cardStatistics.first { rows -> rows.sumOf { it.count } == 6 } }
        }
        repository.revealCard("game", 5)
        val revealed = next.await()
        assertEquals(listOf(1, 2, 10), revealed.map { it.value })
        assertEquals(100.0, revealed.sumOf { it.percentage }, 0.00001)
        assertTrue(revealed.all { it.percentage in 0.0..100.0 })
        repository.revealCard("game", 5)
        assertEquals(6, repository.cardStatistics.first().sumOf { it.count })
    }

    @Test
    fun flowEmitsAfterNewCard_andPhysicalCopiesRemainDistinct() = runBlocking {
        repository.createGame(game().copy(deckCount = 2))
        repository.recordDraws(listOf(draw(0, Rank.ACE)))
        val next = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(5_000) { repository.cardStatistics.first { it.singleOrNull()?.count == 2 } }
        }
        repository.recordDraws(listOf(draw(1, Rank.ACE).copy(card = Card(Rank.ACE, Suit.CLUBS, 1))))
        assertEquals(100.0, next.await().single().percentage, 0.0)
        assertEquals(listOf(0, 1), repository.readDraws("game").map { it.card.deckIndex })
    }

    @Test
    fun duplicateDrawRollsBackWholeBatch_andForeignKeyRejectsOrphan() = runBlocking {
        repository.createGame(game())
        repository.recordDraws(listOf(draw(0, Rank.ACE)))
        expectFailure { repository.recordDraws(listOf(draw(1, Rank.TWO), draw(0, Rank.KING))) }
        assertEquals(listOf(0), repository.readDraws("game").map { it.drawOrder })
        expectFailure { repository.recordDraws(listOf(draw(0, Rank.TWO).copy(gameId = "missing"))) }
        assertTrue(repository.readDraws("missing").isEmpty())
    }

    @Test
    fun movingSplitCard_changesOnlyHandAssignment_notDrawOrderOrCounts() = runBlocking {
        repository.createGame(game())
        repository.recordDraws(listOf(draw(0, Rank.EIGHT), draw(2, Rank.EIGHT)))
        repository.movePlayerCard("game", 2, 1)
        val cards = repository.readDraws("game")
        assertEquals(listOf(0, 2), cards.map { it.drawOrder })
        assertEquals(listOf(0, 1), cards.map { it.handIndex })
        assertEquals(2, repository.cardStatistics.first().single().count)
    }

    @Test
    fun settlementStoresBothHandsAndBalanceAtomically_andRetryDoesNothing() = runBlocking {
        repository.createGame(game())
        repository.setDefaultBet(75)
        assertTrue(repository.settleGame("game", 20, splitHands(), 1_050))
        val entry = repository.history.first().single()
        assertEquals(50, entry.game.netChange)
        assertEquals(listOf(HandOutcome.PLAYER_WIN, HandOutcome.PUSH), entry.hands.map { it.outcome })
        assertEquals(Wallet(1_050, 75), repository.loadWallet())
        assertFalse(repository.settleGame("game", 21, splitHands(), 9_999))
        assertEquals(Wallet(1_050, 75), repository.loadWallet())
    }

    @Test
    fun failedWalletWriteRollsBackHandsAndCompletion() = runBlocking {
        repository.createGame(game())
        repository.loadWallet()
        database.openHelper.writableDatabase.execSQL("""
            CREATE TRIGGER fail_balance BEFORE UPDATE OF balance ON wallet
            BEGIN SELECT RAISE(ABORT, 'Test wallet failure'); END
        """.trimIndent())
        expectFailure { repository.settleGame("game", 20, splitHands(), 1_050) }
        assertTrue(repository.history.first().isEmpty())
        assertEquals(Wallet(), repository.loadWallet())
        database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_balance")
        // If hand inserts survived the failure, this retry would violate their primary key.
        assertTrue(repository.settleGame("game", 20, splitHands(), 1_050))
    }

    @Test
    fun historyIsNewestFirst_andExcludesUnfinishedGames() = runBlocking {
        repository.createGame(game("old"))
        repository.createGame(game("new"))
        repository.createGame(game("unfinished"))
        repository.settleGame("old", 20, listOf(HandRecord("old", 0, 50, HandOutcome.PUSH, 0)), 1_000)
        repository.settleGame("new", 30, listOf(HandRecord("new", 0, 50, HandOutcome.PUSH, 0)), 1_000)
        assertEquals(listOf("new", "old"), repository.history.first().map { it.game.id })
    }

    @Test
    fun dataSurvivesClosingAndReopeningFileDatabase() = runBlocking {
        val name = "bj20-test-${UUID.randomUUID()}.db"
        var diskDatabase = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            var diskRepository = GameRepository(diskDatabase)
            diskRepository.createGame(game())
            diskRepository.recordDraws(listOf(draw(0, Rank.EIGHT)))
            diskRepository.setDefaultBet(75)
            diskRepository.settleGame("game", 20, splitHands(), 1_050)
            diskDatabase.close()
            diskDatabase = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            diskRepository = GameRepository(diskDatabase)
            assertEquals(Wallet(1_050, 75), diskRepository.loadWallet())
            assertEquals(2, diskRepository.history.first().single().hands.size)
            assertEquals(1, diskRepository.cardStatistics.first().single().count)
        } finally {
            diskDatabase.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun retryingSnapshot_doesNotDuplicateCardsOrBets_andCompletedRoundCannotBeOverwritten() = runBlocking {
        val engine = BlackjackEngine(object : CardSource {
            val cards = ArrayDeque(listOf(Rank.EIGHT, Rank.TEN, Rank.EIGHT, Rank.SEVEN, Rank.TEN, Rank.NINE)
                .map { Card(it, Suit.CLUBS) })
            override val remainingCount get() = cards.size
            override fun prepareForNewRound() = false
            override fun draw() = cards.removeFirst()
        })
        val initial = engine.startRound()
        repository.saveRound(game(), initial, null)
        repository.saveRound(game(), initial, null)
        assertEquals(4, repository.readDraws("game").size)
        assertEquals(950, repository.loadWallet().balance)
        val split = engine.split()
        repository.saveRound(game(), split, null)
        repository.saveRound(game(), split, null)
        assertEquals(6, repository.readDraws("game").size)
        assertEquals(1, repository.readDraws("game")[2].handIndex)
        assertEquals(900, repository.loadWallet().balance)
        engine.stand()
        val complete = engine.stand()
        repository.saveRound(game(), complete, 20)
        repository.saveRound(game(), complete, 20)
        repository.saveRound(game(), initial, null)
        assertEquals(1, repository.history.first().size)
        assertEquals(1_050, repository.loadWallet().balance)
        assertEquals(6, repository.cardStatistics.first().sumOf { it.count })
    }

    private fun game(id: String = "game") = GameRecord(id, 10, 1, 50)
    private fun splitHands() = listOf(
        HandRecord("game", 0, 50, HandOutcome.PLAYER_WIN, 50),
        HandRecord("game", 1, 50, HandOutcome.PUSH, 0),
    )
    private fun draw(
        order: Int,
        rank: Rank,
        observed: Boolean = true,
        owner: CardOwner = CardOwner.PLAYER,
    ) = CardDrawRecord("game", order, Card(rank, Suit.CLUBS), owner, if (owner == CardOwner.PLAYER) 0 else null, observed)

    private suspend fun expectFailure(block: suspend () -> Unit) {
        var failed = false
        try { block() } catch (error: Exception) { failed = true }
        assertTrue("Expected storage operation to fail", failed)
    }
}
