package com.aestroon.profile.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class Currency(
    val code: String,
    val name: String
)