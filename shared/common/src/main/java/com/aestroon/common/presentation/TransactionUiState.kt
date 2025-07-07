package com.aestroon.common.presentation

sealed interface TransactionUiState {
    object Idle : TransactionUiState
    object Loading : TransactionUiState
    data class Error(val message: String) : TransactionUiState
}