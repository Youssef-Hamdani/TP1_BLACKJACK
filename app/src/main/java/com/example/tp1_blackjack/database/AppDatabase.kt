package com.example.tp1_blackjack.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tp1_blackjack.database.dao.CardDrawDao
import com.example.tp1_blackjack.database.dao.GameDao
import com.example.tp1_blackjack.database.dao.WalletDao
import com.example.tp1_blackjack.database.entity.CardDrawEntity
import com.example.tp1_blackjack.database.entity.GameEntity
import com.example.tp1_blackjack.database.entity.HandResultEntity
import com.example.tp1_blackjack.database.entity.WalletEntity

@Database(
    entities = [GameEntity::class, HandResultEntity::class, CardDrawEntity::class, WalletEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun cardDrawDao(): CardDrawDao
    abstract fun walletDao(): WalletDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN abandonedAt INTEGER")
                db.execSQL("ALTER TABLE games ADD COLUMN committedBetCount INTEGER NOT NULL DEFAULT 1")
                // Recover the stake count for rounds created by version 1 before Split was recorded directly.
                db.execSQL("""
                    UPDATE games SET committedBetCount = 2
                    WHERE completedAt IS NULL AND id IN (
                        SELECT gameId FROM card_draws WHERE owner = 'PLAYER' AND handIndex = 1
                    )
                """.trimIndent())
            }
        }
    }
}
