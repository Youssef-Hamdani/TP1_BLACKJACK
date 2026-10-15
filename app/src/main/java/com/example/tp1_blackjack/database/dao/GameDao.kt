package com.example.tp1_blackjack.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.tp1_blackjack.database.entity.GameEntity
import com.example.tp1_blackjack.database.entity.HandResultEntity
import kotlinx.coroutines.flow.Flow

data class GameWithHands(
    @Embedded val game: GameEntity,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val hands: List<HandResultEntity>,
)

@Dao
interface GameDao {
    @Insert
    suspend fun insert(game: GameEntity)

    @Update
    suspend fun update(game: GameEntity): Int

    @Insert
    suspend fun insertHands(hands: List<HandResultEntity>)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun find(id: String): GameEntity?

    @Query("SELECT * FROM games WHERE completedAt IS NULL ORDER BY startedAt, id")
    suspend fun findUnfinished(): List<GameEntity>

    @Query("DELETE FROM games WHERE id = :id AND completedAt IS NULL")
    suspend fun deleteUnfinished(id: String): Int

    @Transaction
    @Query("SELECT * FROM games WHERE completedAt IS NOT NULL ORDER BY completedAt DESC, id DESC")
    fun observeHistory(): Flow<List<GameWithHands>>
}
