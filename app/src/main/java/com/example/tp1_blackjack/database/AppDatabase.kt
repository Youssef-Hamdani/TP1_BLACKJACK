package com.example.tp1_blackjack.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tp1_blackjack.database.dao.CardDrawDao
import com.example.tp1_blackjack.database.dao.GameDao
import com.example.tp1_blackjack.database.dao.WalletDao
import com.example.tp1_blackjack.database.entity.CardDrawEntity
import com.example.tp1_blackjack.database.entity.GameEntity
import com.example.tp1_blackjack.database.entity.HandResultEntity
import com.example.tp1_blackjack.database.entity.WalletEntity

@Database(
    entities = [GameEntity::class, HandResultEntity::class, CardDrawEntity::class, WalletEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun cardDrawDao(): CardDrawDao
    abstract fun walletDao(): WalletDao
}
