package com.aestroon.common.data.serializable

import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val display_name: String?,
    val worth_goal: Long,
    val email: String,
    val base_currency: String = DEFAULT_BASE_CURRENCY,
    val worth_goal_currency: String = DEFAULT_BASE_CURRENCY,
)