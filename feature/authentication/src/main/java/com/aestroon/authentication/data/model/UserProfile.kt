package com.aestroon.authentication.data.model

import kotlinx.serialization.Serializable

// Naming scheme has to match the database fields
@Serializable
data class UserProfile(
    val id: String,
    val display_name: String,
    val worth_goal: Long,
    val email: String,
    val base_currency: String = "HUF"
)