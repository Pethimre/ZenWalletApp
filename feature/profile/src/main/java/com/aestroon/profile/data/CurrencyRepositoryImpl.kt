package com.aestroon.profile.data

import android.util.Log
import com.aestroon.common.utilities.Tags.CURRENCY_REPOSITORY
import com.aestroon.profile.BuildConfig
import com.aestroon.profile.data.serializable.Currency
import com.aestroon.profile.data.serializable.ExchangeRateResponse
import com.aestroon.profile.data.serializable.SupportedCodesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.io.IOException

interface CurrencyRepository {
    suspend fun getSupportedCurrencies(): Result<List<Currency>>
    suspend fun getExchangeRates(baseCurrency: String): Result<ExchangeRateResponse>
}

class CurrencyRepositoryImpl(private val client: HttpClient) : CurrencyRepository {
    private val apiKey = BuildConfig.EXCHANGE_RATE_API_KEY
    private val baseUrl = "${BuildConfig.EXCHANGE_RATE_API_URL}$apiKey"

    override suspend fun getSupportedCurrencies(): Result<List<Currency>> {
        val url = "$baseUrl/codes"
        return try {
            val response = client.get(url).body<SupportedCodesResponse>()
            Result.success(
                response.supportedCodes.map { Currency(it[0], it[1]) }.sortedBy { it.name }
            )
        } catch (e: Exception) {
            Log.e(CURRENCY_REPOSITORY, "Error fetching supported currencies", e)
            Result.failure(e)
        }
    }

    override suspend fun getExchangeRates(baseCurrency: String): Result<ExchangeRateResponse> {
        val url = "$baseUrl/latest/$baseCurrency"
        Log.d(CURRENCY_REPOSITORY, "Fetching exchange rates from: $url")

        return try {
            val response = client.get(url).body<ExchangeRateResponse>()

            if (response.result == "success") {
                Log.d(CURRENCY_REPOSITORY, "Successfully parsed success response for base: ${response.base_code}")
                Result.success(response)
            } else {
                val errorMessage = "API returned a non-success result: ${response.result}"
                Log.e(CURRENCY_REPOSITORY, errorMessage)
                Result.failure(IOException(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(CURRENCY_REPOSITORY, "Exception in getExchangeRates", e)
            Result.failure(IOException("Failed to communicate with the server or parse the response.", e))
        }
    }
}
