package com.example.tp1_blackjack.repository

import androidx.room.withTransaction
import com.example.tp1_blackjack.database.AppDatabase
import com.example.tp1_blackjack.database.entity.CardDrawEntity
import com.example.tp1_blackjack.database.entity.GameEntity
import com.example.tp1_blackjack.database.entity.HandResultEntity
import com.example.tp1_blackjack.database.entity.WalletEntity
import com.example.tp1_blackjack.model.betting.PayoutCalculator
import com.example.tp1_blackjack.model.betting.Bet
import com.example.tp1_blackjack.model.cards.Card
import com.example.tp1_blackjack.model.cards.Rank
import com.example.tp1_blackjack.model.cards.Suit
import com.example.tp1_blackjack.model.cards.Shoe
import com.example.tp1_blackjack.model.game.GamePhase
import com.example.tp1_blackjack.model.game.GameState
import com.example.tp1_blackjack.model.results.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class GameRepository(private val database: AppDatabase) {
    private val games = database.gameDao()
    private val draws = database.cardDrawDao()
    private val wallet = database.walletDao()

    val history: Flow<List<GameHistoryEntry>> = games.observeHistory().map { rows ->
        rows.map { row ->
            GameHistoryEntry(
                game = row.game.toRecord(),
                hands = row.hands.sortedBy { it.handIndex }.map {
                    HandRecord(it.gameId, it.handIndex, it.bet, it.outcome, it.netChange)
                },
            )
        }
    }.distinctUntilChanged()

    val cardStatistics: Flow<List<CardValueStats>> = draws.observeValueStats().distinctUntilChanged()

    val savedWallet: Flow<Wallet> = wallet.observe().map {
        it?.let { entity -> Wallet(entity.balance, entity.defaultBet) } ?: Wallet()
    }.distinctUntilChanged()

    suspend fun loadWallet(): Wallet = database.withTransaction {
        wallet.initialize(WalletEntity())
        val saved = checkNotNull(wallet.read())
        Wallet(saved.balance, saved.defaultBet)
    }

    suspend fun setDefaultBet(amount: Int) {
        requireNotNull(Bet.from(amount))
        database.withTransaction {
            wallet.initialize(WalletEntity())
            // Updating just this column cannot overwrite a concurrently settled balance.
            wallet.setDefaultBet(amount)
        }
    }

    suspend fun createGame(game: GameRecord) {
        require(game.id.isNotBlank() && game.startedAt >= 0)
        require(game.deckCount in Shoe.SUPPORTED_DECK_COUNTS)
        requireNotNull(Bet.from(game.initialBet))
        require(game.completedAt == null && game.netChange == null && game.balanceAfter == null)
        games.insert(GameEntity(game.id, game.startedAt, game.deckCount, game.initialBet, null, null, null))
    }

    suspend fun recordDraws(cards: List<CardDrawRecord>) {
        cards.forEach {
            require(it.drawOrder >= 0 && it.card.deckIndex >= 0)
            require(
                (it.owner == CardOwner.DEALER && it.handIndex == null) ||
                    (it.owner == CardOwner.PLAYER && it.handIndex in 0..1),
            )
        }
        // A failed insert (duplicate order or missing parent) rolls back the entire batch.
        database.withTransaction {
            draws.insert(cards.map {
                CardDrawEntity(
                    it.gameId, it.drawOrder, it.card.rank, it.card.suit, it.card.deckIndex,
                    it.owner, it.handIndex, it.isObserved,
                )
            })
        }
    }

    suspend fun readDraws(gameId: String): List<CardDrawRecord> = draws.readForGame(gameId).map {
        CardDrawRecord(
            it.gameId, it.drawOrder, Card(it.rank, it.suit, it.deckIndex),
            it.owner, it.handIndex, it.isObserved,
        )
    }

    // One action = one transaction. Retrying the same snapshot cannot add duplicate cards.
    suspend fun saveRound(game: GameRecord, state: GameState, completedAt: Long?) = database.withTransaction {
        require(state.balance >= 0 && state.deckCount == game.deckCount)
        require((state.phase == GamePhase.COMPLETE) == (completedAt != null))
        val existing = games.find(game.id)
        if (existing?.completedAt != null) return@withTransaction
        if (existing == null) createGame(game)
        else require(existing.startedAt == game.startedAt && existing.initialBet == game.initialBet)
        require(state.drawnCards.map { it.drawOrder } == state.drawnCards.indices.toList())
        state.drawnCards.forEach {
            require(it.card.deckIndex >= 0)
            require((it.owner == CardOwner.DEALER && it.handIndex == null) ||
                (it.owner == CardOwner.PLAYER && it.handIndex in state.playerHands.hands.indices))
        }
        draws.saveSnapshot(state.drawnCards.map {
            CardDrawEntity(game.id, it.drawOrder, it.card.rank, it.card.suit, it.card.deckIndex,
                it.owner, it.handIndex, it.isObserved)
        })
        if (completedAt != null) {
            val hands = state.outcomes.mapIndexed { index, outcome ->
                val bet = state.committedBets[index]
                HandRecord(game.id, index, bet.amount, outcome,
                    PayoutCalculator.grossPayout(bet, outcome) - bet.amount)
            }
            settleGame(game.id, completedAt, hands, state.balance)
        } else {
            wallet.initialize(WalletEntity())
            wallet.setBalance(state.balance)
        }
    }

    suspend fun revealCard(gameId: String, drawOrder: Int) {
        check(draws.reveal(gameId, drawOrder) == 1) { "Unknown card" }
    }

    suspend fun movePlayerCard(gameId: String, drawOrder: Int, handIndex: Int) {
        require(handIndex in 0..1)
        check(draws.moveToHand(gameId, drawOrder, handIndex) == 1) { "Unknown player card" }
    }

    suspend fun settleGame(
        gameId: String,
        completedAt: Long,
        hands: List<HandRecord>,
        balanceAfter: Int,
    ): Boolean = database.withTransaction {
        val game = checkNotNull(games.find(gameId)) { "Unknown game" }
        // A retry must never credit a previously completed game again.
        if (game.completedAt != null) return@withTransaction false
        require(completedAt >= game.startedAt && balanceAfter >= 0)
        require(hands.size in 1..2 && hands.map { it.handIndex }.sorted() == hands.indices.toList())
        hands.forEach {
            require(it.gameId == gameId && it.bet == game.initialBet)
            val bet = requireNotNull(Bet.from(it.bet))
            require(it.netChange == PayoutCalculator.grossPayout(bet, it.outcome) - it.bet)
        }
        games.insertHands(hands.map {
            HandResultEntity(it.gameId, it.handIndex, it.bet, it.outcome, it.netChange)
        })
        check(games.update(game.copy(
            completedAt = completedAt,
            netChange = hands.sumOf { it.netChange },
            balanceAfter = balanceAfter,
        )) == 1)
        wallet.initialize(WalletEntity())
        wallet.setBalance(balanceAfter)
        true
    }
}

private fun GameEntity.toRecord() = GameRecord(
    id, startedAt, deckCount, initialBet, completedAt, netChange, balanceAfter,
)
