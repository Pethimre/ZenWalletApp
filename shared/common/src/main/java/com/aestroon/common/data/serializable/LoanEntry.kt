package com.aestroon.common.data.serializable

import com.aestroon.common.data.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class LoanEntry(
    val id: String,
    @SerialName("loan_id")
    val loanId: String,
    @SerialName("transaction_id")
    val transactionId: String?,
    @SerialName("user_id")
    val userId: String,
    @SerialName("wallet_id")
    val walletId: String?,
    val amount: Long,
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val note: String?,
    @SerialName("interest_payment")
    val isInterest: Boolean
)
