package com.aestroon.common.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class Portfolio(
    val id: String,
    val name: String,
    val type: String,
    val user_id: String,
    val balance: Long,
    val color: String,
    val description: String? = null,
    val icon_url: String? = null
)