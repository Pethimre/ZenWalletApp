package com.aestroon.common.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aestroon.common.data.Converters
import java.util.Date
import java.util.UUID

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Long,
    val currency: String,
    val name: String,
    val description: String?,
    val date: Date,
    val userId: String,
    val walletId: String,
    val categoryId: String?,
    val transactionType: TransactionType,
    val toWalletId: String?,
    val isSynced: Boolean = false
)