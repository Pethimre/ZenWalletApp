package com.aestroon.profile.domain

sealed interface ProfileSettingsUiState {
    object Idle : ProfileSettingsUiState
    object Loading : ProfileSettingsUiState
    data class Success(val message: String) : ProfileSettingsUiState
    data class Error(val message: String) : ProfileSettingsUiState
}