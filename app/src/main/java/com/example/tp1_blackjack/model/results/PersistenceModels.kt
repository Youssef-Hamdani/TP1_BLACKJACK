package com.example.tp1_blackjack.model.results

import com.example.tp1_blackjack.model.betting.Bet
import com.example.tp1_blackjack.model.cards.Card

// Storage-facing values are plain Kotlin; the ViewModels never need Room entities.
data class GameRecord(
    val id: String,
    val startedAt: Long,
    val deckCount: Int,
    val initialBet: Int,
    val completedAt: Long? = null,
    val netChange: Int? = null,
    val balanceAfter: Int? = null,
    val abandonedAt: Long? = null,
    val committedBetCount: Int = 1,
)

data class HandRecord(
    val gameId: String,
    val handIndex: Int,
    val bet: Int,
    val outcome: HandOutcome,
    val netChange: Int,
)

data class GameHistoryEntry(val game: GameRecord, val hands: List<HandRecord>)

enum class CardOwner { PLAYER, DEALER }

// Round-local journal: drawOrder never changes when a card moves during Split.
data class DrawnCard(
    val drawOrder: Int,
    val card: Card,
    val owner: CardOwner,
    val handIndex: Int?,
    val isObserved: Boolean,
)

data class CardDrawRecord(
    val gameId: String,
    val drawOrder: Int,
    val card: Card,
    val owner: CardOwner,
    val handIndex: Int?,
    val isObserved: Boolean,
)

data class Wallet(val balance: Int = 1_000, val defaultBet: Int = Bet.DEFAULT_AMOUNT)

// Value 1 is the separate Ace category; 10 groups TEN/JACK/QUEEN/KING (DEC-08).
data class CardValueStats(val value: Int, val count: Int, val percentage: Double)
