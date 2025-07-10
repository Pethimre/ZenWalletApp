package com.aestroon.common.data.entity

import com.aestroon.common.data.Converters
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import java.util.UUID

enum class RecurrenceType {
    ONCE, DAILY, WEEKLY, MONTHLY, YEARLY
}

@Entity(tableName = "planned_payments")
@TypeConverters(Converters::class)
data class PlannedPaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String?,
    var dueDate: Date,
    val amount: Long,
    val currency: String,
    val recurrenceType: RecurrenceType,
    val recurrenceValue: Int,
    val userId: String,
    val walletId: String,
    val categoryId: String?,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val toWalletId: String? = null,
    val isSynced: Boolean = false
)