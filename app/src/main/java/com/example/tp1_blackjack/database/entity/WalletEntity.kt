package com.example.tp1_blackjack.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Int = 1_000,
    val defaultBet: Int = 50,
)
