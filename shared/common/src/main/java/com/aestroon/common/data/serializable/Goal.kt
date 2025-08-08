package com.aestroon.common.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    val id: String,
    val user_id: String,
    val name: String,
    val target_amount: Long,
    val current_amount: Long,
    val target_date: String?,
    val icon_name: String?,
    val color: String
)