package com.aestroon.common.data.serializable

import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PlannedPayment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @SerialName("Name")
    val name: String,
    @SerialName("Description")
    val description: String?,
    // Using string for date serialization with Supabase
    val due_date: String,
    val amount: Long,
    val currency: String,
    val recurrence_type: String,
    val recurrence_value: Int,
    val user_id: String,
    val wallet_id: String,
    val category_id: String?,
    val transaction_type: String = "EXPENSE",
    val to_wallet_id: String? = null
)