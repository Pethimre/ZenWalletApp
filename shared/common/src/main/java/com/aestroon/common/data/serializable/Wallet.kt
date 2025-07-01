package com.aestroon.common.data.serializable

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Wallet(
    val id: String = UUID.randomUUID().toString(),
    val display_name: String,
    val balance: Long,
    val color: String,
    val currency: String,
    val owner_id: String,
    val icon_name: String,
    val included: Boolean,
    val goal_amount: Long,
)