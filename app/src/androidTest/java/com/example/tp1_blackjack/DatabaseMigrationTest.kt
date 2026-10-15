package com.example.tp1_blackjack

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.tp1_blackjack.database.AppDatabase
import com.example.tp1_blackjack.repository.GameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

class DatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun versionOneSplitRoundMigratesAndRefundsOnceWithoutLosingObservedCards() = runBlocking {
        val name = "migration-${UUID.randomUUID()}.db"
        val old = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
        old.execSQL("CREATE TABLE games (id TEXT NOT NULL PRIMARY KEY, startedAt INTEGER NOT NULL, deckCount INTEGER NOT NULL, initialBet INTEGER NOT NULL, completedAt INTEGER, netChange INTEGER, balanceAfter INTEGER)")
        old.execSQL("CREATE TABLE hand_results (gameId TEXT NOT NULL, handIndex INTEGER NOT NULL, bet INTEGER NOT NULL, outcome TEXT NOT NULL, netChange INTEGER NOT NULL, PRIMARY KEY(gameId, handIndex), FOREIGN KEY(gameId) REFERENCES games(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        old.execSQL("CREATE TABLE card_draws (gameId TEXT NOT NULL, drawOrder INTEGER NOT NULL, rank TEXT NOT NULL, suit TEXT NOT NULL, deckIndex INTEGER NOT NULL, owner TEXT NOT NULL, handIndex INTEGER, isObserved INTEGER NOT NULL, PRIMARY KEY(gameId, drawOrder), FOREIGN KEY(gameId) REFERENCES games(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        old.execSQL("CREATE TABLE wallet (id INTEGER NOT NULL PRIMARY KEY, balance INTEGER NOT NULL, defaultBet INTEGER NOT NULL)")
        old.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
        old.execSQL("INSERT INTO room_master_table VALUES (42, '31f029f41780860082aa49c354b2b562')")
        old.execSQL("INSERT INTO wallet VALUES (1, 900, 50)")
        old.execSQL("INSERT INTO games VALUES ('split', 10, 1, 50, NULL, NULL, NULL)")
        old.execSQL("INSERT INTO card_draws VALUES ('split', 0, 'EIGHT', 'CLUBS', 0, 'PLAYER', 0, 1)")
        old.execSQL("INSERT INTO card_draws VALUES ('split', 1, 'TEN', 'CLUBS', 0, 'DEALER', NULL, 0)")
        old.execSQL("INSERT INTO card_draws VALUES ('split', 2, 'EIGHT', 'HEARTS', 0, 'PLAYER', 1, 1)")
        old.version = 1
        old.close()

        var database = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        try {
            var repository = GameRepository(database)
            assertEquals(2, database.gameDao().findUnfinished().single().committedBetCount)
            assertEquals(1, repository.recoverInterruptedRounds())
            assertEquals(1_000, repository.loadWallet().balance)
            assertEquals(2, repository.cardStatistics.first().sumOf { it.count })
            assertNotNull(repository.history.first().single().game.abandonedAt)
            database.close()

            database = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()
            repository = GameRepository(database)
            assertEquals(0, repository.recoverInterruptedRounds())
            assertEquals(1_000, repository.loadWallet().balance)
            assertEquals(2, repository.cardStatistics.first().sumOf { it.count })
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
