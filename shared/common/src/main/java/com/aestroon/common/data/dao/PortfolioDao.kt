package com.aestroon.common.data.dao

import androidx.room.*
import com.aestroon.common.data.entity.PortfolioEntity
import com.aestroon.common.data.entity.PortfolioInstrumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    @Query("SELECT * FROM portfolios WHERE userId = :userId")
    fun getPortfoliosForUser(userId: String): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolio_instruments WHERE portfolioId = :portfolioId")
    fun getInstrumentsForPortfolio(portfolioId: String): Flow<List<PortfolioInstrumentEntity>>

    @Query("SELECT * FROM portfolios WHERE id = :portfolioId LIMIT 1")
    fun getPortfolioById(portfolioId: String): Flow<PortfolioEntity>

    @Query("SELECT * FROM portfolio_instruments WHERE id = :instrumentId LIMIT 1")
    fun getInstrumentById(instrumentId: String): Flow<PortfolioInstrumentEntity>

    @Query("SELECT * FROM portfolios WHERE isSynced = 0")
    fun getUnsyncedPortfolios(): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolio_instruments WHERE isSynced = 0")
    fun getUnsyncedInstruments(): Flow<List<PortfolioInstrumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstrument(instrument: PortfolioInstrumentEntity)

    @Update
    suspend fun updatePortfolio(portfolio: PortfolioEntity)

    @Update
    suspend fun updateInstrument(instrument: PortfolioInstrumentEntity)

    @Query("DELETE FROM portfolios WHERE id = :portfolioId")
    suspend fun deletePortfolio(portfolioId: String)

    @Query("DELETE FROM portfolio_instruments WHERE id = :instrumentId")
    suspend fun deleteInstrument(instrumentId: String)

    @Transaction
    suspend fun clearAndInsertPortfolios(portfolios: List<PortfolioEntity>) {
        clearPortfolios()
        portfolios.forEach { insertPortfolio(it) }
    }

    @Query("DELETE FROM portfolios")
    suspend fun clearPortfolios()

    @Transaction
    suspend fun clearAndInsertInstruments(instruments: List<PortfolioInstrumentEntity>) {
        clearInstruments()
        instruments.forEach { insertInstrument(it) }
    }

    @Query("DELETE FROM portfolio_instruments")
    suspend fun clearInstruments()
}