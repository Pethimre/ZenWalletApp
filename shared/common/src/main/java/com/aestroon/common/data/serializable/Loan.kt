package com.aestroon.common.data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Loan(
    val id: String,
    val name: String,
    val description: String?,
    val principal: Long,
    val remaining: Long,
    @SerialName("icon_name")
    val iconName: String?,
    val color: String,
    val type: String,
    @SerialName("user_id")
    val userId: String
)
