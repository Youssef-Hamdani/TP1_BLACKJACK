package com.example.tp1_blackjack.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val deckCount: Int,
    val initialBet: Int,
    val completedAt: Long?,
    val netChange: Int?,
    val balanceAfter: Int?,
)
