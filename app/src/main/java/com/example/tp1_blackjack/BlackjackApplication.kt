package com.example.tp1_blackjack

import android.app.Application
import androidx.room.Room
import com.example.tp1_blackjack.database.AppDatabase
import com.example.tp1_blackjack.repository.GameRepository
import com.example.tp1_blackjack.repository.PlayerProfileRepository

/** One database and repository per application process, shared by every screen. */
class BlackjackApplication : Application() {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "blackjack.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    val repository: GameRepository by lazy { GameRepository(database) }
    val playerProfileRepository: PlayerProfileRepository by lazy {
        PlayerProfileRepository(applicationContext)
    }
}
