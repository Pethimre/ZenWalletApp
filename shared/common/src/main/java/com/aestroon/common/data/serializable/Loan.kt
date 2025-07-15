package com.aestroon.common.data.serializable

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Loan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String?,
    val principal: Long,
    val remaining: Long,
    val icon_name: String?,
    val color: String,
    val type: String, // "LENT" or "BORROWED"
    val user_id: String
)
