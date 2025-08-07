package com.aestroon.common.data.serializable

import com.aestroon.common.data.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Date

@Serializable
data class Transaction(
    val id: String,
    val amount: Long,
    val currency: String,
    val name: String,
    val description: String?,
    @SerialName("created_at")
    @Serializable(with = DateSerializer::class)
    val date: Date,
    @SerialName("user_id")
    val userId: String,
    @SerialName("wallet_id")
    val walletId: String,
    @SerialName("category_id")
    val categoryId: String?,
    @SerialName("transaction_type")
    val transactionType: String,
    @SerialName("to_wallet_id")
    val toWalletId: String?
)