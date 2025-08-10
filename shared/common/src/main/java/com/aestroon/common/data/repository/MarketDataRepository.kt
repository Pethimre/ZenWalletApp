package com.aestroon.common.data.repository

import android.util.Log
import com.aestroon.common.BuildConfig
import com.aestroon.common.data.serializable.AlphaVantageResponse
import com.aestroon.common.data.serializable.CoinGeckoHistoryResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

enum class TimeRange(val days: Long, val displayName: String) {
    DAY(1, "1D"),
    WEEK(7, "1W"),
    MONTH(30, "1M"),
    THREE_MONTHS(90, "3M"),
    YEAR(365, "1Y"),
    FIVE_YEARS(1825, "5Y"),
    ALL_TIME(0, "All"),
}

interface MarketDataRepository {
    suspend fun getHistoricalCryptoData(symbol: String, days: Long): Result<List<Double>>
    suspend fun getHistoricalStockData(symbol: String): Result<List<Double>>
}

class MarketDataRepositoryImpl(
    private val client: HttpClient
) : MarketDataRepository {
    private val alphaVantageKey = BuildConfig.ALPHA_VANTAGE_API_KEY
    private val coingeckoKey = BuildConfig.COINGECKO_API_KEY

    override suspend fun getHistoricalCryptoData(symbol: String, days: Long): Result<List<Double>> {
        return try {
            val id = when (symbol.uppercase()) {
                "BTC" -> "bitcoin"
                "ETH" -> "ethereum"
                "USDT" -> "tether"
                "BNB" -> "binancecoin"
                "SOL" -> "solana"
                else -> symbol.lowercase()
            }

            val url = "https://api.coingecko.com/api/v3/coins/${id}/market_chart?vs_currency=usd&days=$days&x_cg_demo_api_key=$coingeckoKey"
            val response = client.get(url).body<CoinGeckoHistoryResponse>()
            Result.success(response.prices.map { it[1] })
        } catch (e: Exception) {
            Log.e("MarketDataRepo", "CoinGecko API call failed for symbol $symbol", e)
            Result.failure(e)
        }
    }

    override suspend fun getHistoricalStockData(symbol: String): Result<List<Double>> {
        return try {
            val url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=$symbol&outputsize=full&apikey=$alphaVantageKey"
            val response = client.get(url).body<AlphaVantageResponse>()
            val apiError = response.note ?: response.information ?: response.errorMessage
            if (apiError != null) {
                throw Exception("Alpha Vantage API Error: $apiError")
            }
            if (response.timeSeries == null) {
                throw Exception("No time series data available for symbol: $symbol")
            }
            val data = response.timeSeries.entries.sortedBy { it.key }.map { it.value.close.toDouble() }
            Result.success(data)
        } catch (e: Exception) {
            Log.e("MarketDataRepo", "Alpha Vantage API call failed for $symbol", e)
            Result.failure(e)
        }
    }
}
