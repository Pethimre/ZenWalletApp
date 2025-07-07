package com.aestroon.common.data.serializable

import com.aestroon.common.data.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Long,
    val currency: String,
    val name: String,
    val description: String?,
    @Serializable(with = DateSerializer::class)
    val created_at: Date,
    val user_id: String,
    val wallet_id: String,
    val category_id: String?,
    val transaction_type: String,
    val to_wallet_id: String?
)