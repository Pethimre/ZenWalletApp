package com.aestroon.common.data.serializable

import com.aestroon.common.data.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class LoanEntry(
    val id: String = UUID.randomUUID().toString(),
    val loan_id: String,
    val transaction_id: String?,
    val user_id: String,
    val wallet_id: String,
    val amount: Long,
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val note: String?,
    val is_interest: Boolean
)
