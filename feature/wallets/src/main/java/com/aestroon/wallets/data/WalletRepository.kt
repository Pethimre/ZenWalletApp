package com.aestroon.wallets.data

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
    suspend fun addWallet(wallet: WalletEntity): Result<Unit>
    suspend fun updateWallet(wallet: WalletEntity): Result<Unit>
    suspend fun deleteWallet(wallet: WalletEntity): Result<Unit>
    suspend fun syncPendingWallets(): Result<Int>
}

class WalletRepositoryImpl(
    private val postgrest: Postgrest,
    private val walletDao: WalletDao,
    private val connectivityObserver: ConnectivityObserver
) : WalletRepository {

    override fun getWalletsForUser(userId: String): Flow<List<WalletEntity>> {
        return walletDao.getWalletsForUser(userId)
    }

    override suspend fun addWallet(wallet: WalletEntity): Result<Unit> = runCatching {
        walletDao.insertWallet(wallet)
    }

    override suspend fun updateWallet(wallet: WalletEntity): Result<Unit> = runCatching {
        walletDao.insertWallet(wallet.copy(isSynced = false))
    }

    override suspend fun syncPendingWallets(): Result<Int> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching 0
        }

        val unsyncedWallets = walletDao.getUnsyncedWallets().first()
        if (unsyncedWallets.isEmpty()) return@runCatching 0

        Log.d("WalletRepository", "Syncing ${unsyncedWallets.size} pending wallets.")

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
        Log.d("WalletRepository", "Sync complete for ${unsyncedWallets.size} wallets.")
        unsyncedWallets.size
    }

    override suspend fun deleteWallet(wallet: WalletEntity): Result<Unit> = runCatching {
        walletDao.deleteWalletById(wallet.id)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from(WALLETS_TABLE_NAME).delete { filter { eq("id", wallet.id) } }
        }
    }
}
