package com.example.tp1_blackjack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.tp1_blackjack.database.entity.CardDrawEntity
import com.example.tp1_blackjack.model.results.CardValueStats
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDrawDao {
    @Insert
    suspend fun insert(draws: List<CardDrawEntity>)

    @Upsert
    suspend fun saveSnapshot(draws: List<CardDrawEntity>)

    @Query("SELECT MAX(handIndex) FROM card_draws WHERE gameId = :gameId AND owner = 'PLAYER'")
    suspend fun highestPlayerHandIndex(gameId: String): Int?

    @Query("SELECT * FROM card_draws WHERE gameId = :gameId ORDER BY drawOrder")
    suspend fun readForGame(gameId: String): List<CardDrawEntity>

    @Query("UPDATE card_draws SET isObserved = 1 WHERE gameId = :gameId AND drawOrder = :drawOrder")
    suspend fun reveal(gameId: String, drawOrder: Int): Int

    @Query("UPDATE card_draws SET handIndex = :handIndex WHERE gameId = :gameId AND drawOrder = :drawOrder AND owner = 'PLAYER'")
    suspend fun moveToHand(gameId: String, drawOrder: Int, handIndex: Int): Int

    // Both numerator and denominator exclude hidden cards. One query gives one consistent snapshot.
    @Query("""
        SELECT CASE rank
            WHEN 'ACE' THEN 1 WHEN 'TWO' THEN 2 WHEN 'THREE' THEN 3
            WHEN 'FOUR' THEN 4 WHEN 'FIVE' THEN 5 WHEN 'SIX' THEN 6
            WHEN 'SEVEN' THEN 7 WHEN 'EIGHT' THEN 8 WHEN 'NINE' THEN 9
            ELSE 10 END AS value,
            COUNT(*) AS count,
            100.0 * COUNT(*) / (SELECT COUNT(*) FROM card_draws WHERE isObserved = 1) AS percentage
        FROM card_draws WHERE isObserved = 1
        GROUP BY value ORDER BY value
    """)
    fun observeValueStats(): Flow<List<CardValueStats>>
}
