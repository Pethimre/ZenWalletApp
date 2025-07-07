package com.aestroon.common.data.repository

import android.util.Log
import com.aestroon.common.data.WALLETS_TABLE_NAME
import com.aestroon.common.data.dao.WalletDao
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.serializable.Wallet
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface WalletRepository {
    fun getWalletsForUser(userId: String): Flow<List<WalletEntity>>
    fun getWalletById(walletId: String): Flow<WalletEntity?>
    suspend fun addWallet(wallet: WalletEntity): Result<Unit>
    suspend fun updateWallet(wallet: WalletEntity): Result<Unit>
    suspend fun deleteWallet(wallet: WalletEntity): Result<Unit>
    suspend fun syncPendingWallets(): Result<Int>
    suspend fun fetchRemoteWallets(userId: String): Result<Unit>
}

class WalletRepositoryImpl(
    private val postgrest: Postgrest,
    private val walletDao: WalletDao,
    private val connectivityObserver: ConnectivityObserver
) : WalletRepository {

    override fun getWalletsForUser(userId: String): Flow<List<WalletEntity>> {
        return walletDao.getWalletsForUser(userId)
    }

    override fun getWalletById(walletId: String): Flow<WalletEntity?> {
        return walletDao.getWalletById(walletId)
    }

    override suspend fun addWallet(wallet: WalletEntity): Result<Unit> = runCatching {
        walletDao.insertWallet(wallet.copy(isSynced = false))
        syncPendingWallets()
    }

    override suspend fun updateWallet(wallet: WalletEntity): Result<Unit> = runCatching {
        walletDao.insertWallet(wallet.copy(isSynced = false))
        syncPendingWallets()
    }

    override suspend fun fetchRemoteWallets(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching
        }
        val remoteWallets = postgrest.from(WALLETS_TABLE_NAME).select {
            filter { eq("owner_id", userId) }
        }.decodeList<Wallet>()

        remoteWallets.forEach { networkWallet ->
            val localEntity = WalletEntity(
                id = networkWallet.id,
                displayName = networkWallet.display_name,
                balance = networkWallet.balance,
                color = networkWallet.color,
                currency = networkWallet.currency,
                ownerId = networkWallet.owner_id,
                iconName = networkWallet.icon_name,
                included = networkWallet.included,
                goalAmount = networkWallet.goal_amount,
                isSynced = true ,
            )
            walletDao.insertWallet(localEntity)
        }
    }

    override suspend fun syncPendingWallets(): Result<Int> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching 0
        }

        val unsyncedWallets = walletDao.getUnsyncedWallets().first()
        if (unsyncedWallets.isEmpty()) return@runCatching 0

        val networkWallets = unsyncedWallets.map { entity ->
            Wallet(
                id = entity.id,
                display_name = entity.displayName,
                balance = entity.balance,
                color = entity.color,
                currency = entity.currency,
                owner_id = entity.ownerId,
                icon_name = entity.iconName,
                included = entity.included,
                goal_amount = entity.goalAmount
            )
        }

        postgrest.from(WALLETS_TABLE_NAME).upsert(networkWallets)
        unsyncedWallets.forEach { walletDao.markWalletAsSynced(it.id) }
        unsyncedWallets.size
    }

    override suspend fun deleteWallet(wallet: WalletEntity): Result<Unit> = runCatching {
        walletDao.deleteWalletById(wallet.id)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from(WALLETS_TABLE_NAME).delete { filter { eq("id", wallet.id) } }
        }
    }
}
