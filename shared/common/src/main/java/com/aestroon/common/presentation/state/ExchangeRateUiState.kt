package com.aestroon.common.presentation.state

import com.aestroon.common.data.serializable.ExchangeRateResponse

sealed interface ExchangeRateUiState {
    object Idle : ExchangeRateUiState
    object Loading : ExchangeRateUiState
    data class Success(val rates: ExchangeRateResponse) : ExchangeRateUiState
    data class Error(val message: String) : ExchangeRateUiState
}