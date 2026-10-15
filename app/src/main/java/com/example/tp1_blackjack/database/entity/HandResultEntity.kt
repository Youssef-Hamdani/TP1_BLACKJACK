package com.example.tp1_blackjack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.tp1_blackjack.model.results.HandOutcome

@Entity(
    tableName = "hand_results",
    primaryKeys = ["gameId", "handIndex"],
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"], childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class HandResultEntity(
    val gameId: String,
    val handIndex: Int,
    val bet: Int,
    val outcome: HandOutcome,
    val netChange: Int,
)
