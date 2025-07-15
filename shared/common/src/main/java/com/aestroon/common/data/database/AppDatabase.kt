package com.aestroon.common.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aestroon.common.data.dao.CategoryDao
import com.aestroon.common.data.dao.LoanDao
import com.aestroon.common.data.dao.LoanEntryDao
import com.aestroon.common.data.dao.PlannedPaymentDao
import com.aestroon.common.data.dao.PortfolioDao
import com.aestroon.common.data.dao.TransactionDao
import com.aestroon.common.data.dao.UserDao
import com.aestroon.common.data.dao.WalletDao
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.LoanEntity
import com.aestroon.common.data.entity.LoanEntryEntity
import com.aestroon.common.data.entity.LocalUser
import com.aestroon.common.data.entity.PlannedPaymentEntity
import com.aestroon.common.data.entity.PortfolioEntity
import com.aestroon.common.data.entity.PortfolioInstrumentEntity
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.WalletEntity

@Database(
    entities = [
        LocalUser::class,
        WalletEntity::class,
        CategoryEntity::class,
        PortfolioEntity::class,
        TransactionEntity::class,
        PlannedPaymentEntity::class,
        PortfolioInstrumentEntity::class,
        LoanEntity::class,
        LoanEntryEntity::class,
    ],
    version = 10,
    autoMigrations = [
        //AutoMigration(from = 3, to = 4)
    ],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun transactionDao(): TransactionDao
    abstract fun plannedPaymentDao(): PlannedPaymentDao
    abstract fun loanDao(): LoanDao
    abstract fun loanEntryDao(): LoanEntryDao
}