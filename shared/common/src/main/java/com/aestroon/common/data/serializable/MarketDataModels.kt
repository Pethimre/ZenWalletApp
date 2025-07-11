package com.aestroon.common.data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinGeckoHistoryResponse(
    val prices: List<List<Double>>
)

@Serializable
data class AlphaVantageResponse(
    @SerialName("Time Series (Daily)")
    val timeSeries: Map<String, AlphaVantageDayEntry>? = null,
    @SerialName("Note")
    val note: String? = null,
    @SerialName("Information")
    val information: String? = null,
    @SerialName("Error Message")
    val errorMessage: String? = null
)

@Serializable
data class AlphaVantageDayEntry(
    @SerialName("4. close")
    val close: String
)