package com.aestroon.common.data.repository

import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

interface CurrencyConversionRepository {
    val baseCurrency: StateFlow<String>
    val exchangeRates: StateFlow<Map<String, Double>?>

    fun setBaseCurrency(newCurrency: String)

    suspend fun loadInitialCurrency()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyConversionRepositoryImpl(
    private val userRepository: UserRepository,
    private val currencyRepository: CurrencyRepository,
    private val authRepository: AuthRepository
) : CurrencyConversionRepository {

    private val _baseCurrency = MutableStateFlow(DEFAULT_BASE_CURRENCY)
    override val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

    override val exchangeRates: StateFlow<Map<String, Double>?> = _baseCurrency
        .flatMapLatest { currency ->
            flow {
                emit(currencyRepository.getExchangeRates(currency).getOrNull()?.conversion_rates)
            }
        }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)

    override suspend fun loadInitialCurrency() {
        authRepository.userIdFlow.firstOrNull()?.let { userId ->
            val userProfile = userRepository.getUserProfile(userId).getOrNull()
            _baseCurrency.value = userProfile?.base_currency ?: DEFAULT_BASE_CURRENCY
        }
    }

    override fun setBaseCurrency(newCurrency: String) {
        _baseCurrency.value = newCurrency
    }
}
