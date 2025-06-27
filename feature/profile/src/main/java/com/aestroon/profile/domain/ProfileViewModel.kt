package com.aestroon.profile.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.authentication.data.AuthRepository
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserInfo?>(null)
    val user: StateFlow<UserInfo?> = _user.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            authRepository.getUpdatedUser()
                .onSuccess {
                    _user.value = it
                    _uiState.value = UiState.Idle
                }
                .onFailure {
                    _uiState.value = UiState.Error(it.message ?: "Failed to load profile.")
                }
        }
    }

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            authRepository.updateUser(displayName)
                .onSuccess { updatedUser ->
                    _user.value = updatedUser // Update the local state with the new user info
                    _uiState.value = UiState.Success("Profile updated successfully!")
                }
                .onFailure {
                    _uiState.value = UiState.Error(it.message ?: "Failed to update profile.")
                }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    sealed interface UiState {
        object Idle : UiState
        object Loading : UiState
        data class Success(val message: String) : UiState
        data class Error(val message: String) : UiState
    }
}