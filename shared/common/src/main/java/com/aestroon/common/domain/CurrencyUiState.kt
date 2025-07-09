package com.aestroon.common.domain

import com.aestroon.common.data.serializable.Currency

sealed interface CurrencyListUiState {
    object Loading : CurrencyListUiState
    data class Success(val currencies: List<Currency>) : CurrencyListUiState
    data class Error(val message: String) : CurrencyListUiState
}