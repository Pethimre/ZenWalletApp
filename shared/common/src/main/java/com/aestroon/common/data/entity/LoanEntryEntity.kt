package com.aestroon.common.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aestroon.common.data.Converters
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "loan_entries",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("loanId"), Index("walletId")]
)
@TypeConverters(Converters::class)
data class LoanEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val loanId: String,
    val transactionId: String?,
    val userId: String,
    val walletId: String?,
    val amount: Long,
    val date: Date,
    val note: String?,
    val isInterest: Boolean,
    val isSynced: Boolean = false
)
