package com.aestroon.common.data.dao

import androidx.room.*
import com.aestroon.common.data.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE ownerId = :userId ORDER BY displayName ASC")
    fun getWalletsForUser(userId: String): Flow<List<WalletEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE isSynced = 0")
    fun getUnsyncedWallets(): Flow<List<WalletEntity>>

    @Query("UPDATE wallets SET isSynced = 1 WHERE id = :walletId")
    suspend fun markWalletAsSynced(walletId: String)

    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWalletById(walletId: String)
}
