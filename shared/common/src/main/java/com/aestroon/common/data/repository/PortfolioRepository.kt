package com.aestroon.common.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.aestroon.common.data.dao.PortfolioDao
import com.aestroon.common.data.entity.PortfolioEntity
import com.aestroon.common.data.entity.PortfolioInstrumentEntity
import com.aestroon.common.data.serializable.Portfolio
import com.aestroon.common.data.serializable.PortfolioInstrument
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant

interface PortfolioRepository {
    fun getPortfolios(userId: String): Flow<List<PortfolioEntity>>
    fun getInstrumentsForPortfolio(portfolioId: String): Flow<List<PortfolioInstrumentEntity>>
    fun getInstrumentById(instrumentId: String): Flow<PortfolioInstrumentEntity?>

    suspend fun addPortfolio(portfolio: PortfolioEntity)
    suspend fun addInstrument(instrument: PortfolioInstrumentEntity)

    suspend fun updatePortfolio(portfolio: PortfolioEntity)
    suspend fun updateInstrument(instrument: PortfolioInstrumentEntity)

    suspend fun deletePortfolio(portfolioId: String)
    suspend fun deleteInstrument(instrumentId: String)

    suspend fun syncAll(userId: String)
}

@RequiresApi(Build.VERSION_CODES.O)
class PortfolioRepositoryImpl(
    private val portfolioDao: PortfolioDao,
    private val postgrest: Postgrest,
    private val connectivityObserver: ConnectivityObserver
) : PortfolioRepository {

    override fun getPortfolios(userId: String): Flow<List<PortfolioEntity>> {
        return portfolioDao.getPortfoliosForUser(userId)
    }

    override fun getInstrumentsForPortfolio(portfolioId: String): Flow<List<PortfolioInstrumentEntity>> {
        return portfolioDao.getInstrumentsForPortfolio(portfolioId)
    }

    override fun getInstrumentById(instrumentId: String): Flow<PortfolioInstrumentEntity?> {
        return portfolioDao.getInstrumentById(instrumentId)
    }

    override suspend fun addPortfolio(portfolio: PortfolioEntity) {
        portfolioDao.insertPortfolio(portfolio.copy(isSynced = false))
        syncAll(portfolio.userId)
    }

    override suspend fun addInstrument(instrument: PortfolioInstrumentEntity) {
        portfolioDao.insertInstrument(instrument.copy(isSynced = false))
        val portfolio = portfolioDao.getPortfolioById(instrument.portfolioId).first()
        syncAll(portfolio.userId)
    }

    override suspend fun updatePortfolio(portfolio: PortfolioEntity) {
        portfolioDao.updatePortfolio(portfolio.copy(isSynced = false))
        syncAll(portfolio.userId)
    }

    override suspend fun updateInstrument(instrument: PortfolioInstrumentEntity) {
        portfolioDao.updateInstrument(instrument.copy(isSynced = false))
        val portfolio = portfolioDao.getPortfolioById(instrument.portfolioId).first()
        syncAll(portfolio.userId)
    }

    override suspend fun deletePortfolio(portfolioId: String) {
        portfolioDao.deletePortfolio(portfolioId)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from("Portfolios").delete { filter { eq("id", portfolioId) } }
        }
    }

    override suspend fun deleteInstrument(instrumentId: String) {
        portfolioDao.deleteInstrument(instrumentId)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from("Portfolio_entry").delete { filter { eq("id", instrumentId) } }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncAll(userId: String) {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) return
        val unsyncedPortfolios = portfolioDao.getUnsyncedPortfolios().first()
        if (unsyncedPortfolios.isNotEmpty()) {
            val networkPortfolios = unsyncedPortfolios.map {
                Portfolio(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    user_id = it.userId,
                    balance = it.balance,
                    color = it.color,
                    description = it.description,
                    icon_url = it.iconName
                )
            }
            postgrest.from("Portfolios").upsert(networkPortfolios)
        }

        val unsyncedInstruments = portfolioDao.getUnsyncedInstruments().first()
        if (unsyncedInstruments.isNotEmpty()) {
            val networkInstruments = unsyncedInstruments.map { entity ->
                PortfolioInstrument(
                    id = entity.id,
                    portfolio_id = entity.portfolioId,
                    user_id = userId,
                    symbol = entity.symbol,
                    name = entity.name,
                    quantity = entity.quantity,
                    average_buy_price = entity.averageBuyPrice,
                    currency = entity.currency,
                    maturity_date = entity.maturityDate?.let { Instant.ofEpochMilli(it).toString() },
                    coupon_rate = entity.couponRate
                )
            }
            postgrest.from("Portfolio_entry").upsert(networkInstruments)
        }

        val remotePortfolios = postgrest.from("Portfolios").select {
            filter { eq("user_id", userId) }
        }.decodeList<Portfolio>()

        // This mapping is now corrected to include all fields
        portfolioDao.clearAndInsertPortfolios(remotePortfolios.map {
            PortfolioEntity(
                id = it.id,
                name = it.name,
                description = it.description,
                balance = it.balance,
                color = it.color,
                iconName = it.icon_url,
                type = it.type,
                userId = it.user_id,
                isSynced = true
            )
        })

        val remoteInstruments = postgrest.from("Portfolio_entry").select {
            filter { eq("user_id", userId) }
        }.decodeList<PortfolioInstrument>()

        portfolioDao.clearAndInsertInstruments(remoteInstruments.map { remote ->
            PortfolioInstrumentEntity(
                id = remote.id,
                portfolioId = remote.portfolio_id,
                symbol = remote.symbol,
                name = remote.name,
                quantity = remote.quantity,
                averageBuyPrice = remote.average_buy_price,
                currency = remote.currency,
                maturityDate = remote.maturity_date?.let {
                    try {
                        Instant.parse(it).toEpochMilli()
                    } catch (e: Exception) {
                        Log.e("PortfolioRepository", "${e.localizedMessage}")
                        null
                    }
                },
                couponRate = remote.coupon_rate,
                isSynced = true
            )
        })
    }
}
