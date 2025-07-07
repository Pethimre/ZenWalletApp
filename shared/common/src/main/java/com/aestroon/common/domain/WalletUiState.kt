package com.aestroon.common.domain

sealed interface WalletsUiState {
    object Idle : WalletsUiState
    object Loading : WalletsUiState
    data class Error(val message: String) : WalletsUiState
}