package com.example.tp1_blackjack.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.tp1_blackjack.model.results.CardOwner
import com.example.tp1_blackjack.model.cards.Rank
import com.example.tp1_blackjack.model.cards.Suit

@Entity(
    tableName = "card_draws",
    primaryKeys = ["gameId", "drawOrder"],
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"], childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class CardDrawEntity(
    val gameId: String,
    val drawOrder: Int,
    val rank: Rank,
    val suit: Suit,
    val deckIndex: Int,
    val owner: CardOwner,
    val handIndex: Int?,
    val isObserved: Boolean,
)
