package com.aestroon.common.data.serializable

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon_name: String,
    val color: String,
    val user_id: String
)