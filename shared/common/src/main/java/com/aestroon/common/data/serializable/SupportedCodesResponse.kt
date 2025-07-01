package com.aestroon.common.data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupportedCodesResponse(
    val result: String,
    @SerialName("supported_codes")
    val supportedCodes: List<List<String>>
)