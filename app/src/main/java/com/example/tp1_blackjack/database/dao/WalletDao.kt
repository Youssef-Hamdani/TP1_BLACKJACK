package com.example.tp1_blackjack.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tp1_blackjack.database.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun initialize(wallet: WalletEntity)

    @Query("UPDATE wallet SET balance = :balance WHERE id = 1")
    suspend fun setBalance(balance: Int)

    @Query("UPDATE wallet SET defaultBet = :amount WHERE id = 1")
    suspend fun setDefaultBet(amount: Int)

    @Query("SELECT * FROM wallet WHERE id = 1")
    suspend fun read(): WalletEntity?

    @Query("SELECT * FROM wallet WHERE id = 1")
    fun observe(): Flow<WalletEntity?>
}
