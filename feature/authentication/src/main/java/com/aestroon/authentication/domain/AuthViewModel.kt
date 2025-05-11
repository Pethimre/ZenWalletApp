package com.aestroon.authentication.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.authentication.data.AuthRepository
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _restoreComplete = MutableStateFlow(false)
    val restoreComplete: StateFlow<Boolean> = _restoreComplete

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        observeSessionStatus()
    }

    private fun observeSessionStatus() {
        viewModelScope.launch {
            SupabaseClientProvider.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _isLoggedIn.value = true
                        _restoreComplete.value = true
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _isLoggedIn.value = false
                        _restoreComplete.value = true
                    }
                    else -> {
                        // Do nothing for Loading, or unknown statuses
                    }
                }
            }
        }
    }

    fun restoreSession() {
        viewModelScope.launch {
            SupabaseClientProvider.restoreSession()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val success = authRepository.login(email, password)
            if (success) {
                _isLoggedIn.value = true
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error("Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userManager.logout()
            _isLoggedIn.value = false
        }
    }

    sealed interface LoginUiState {
        object Idle : LoginUiState
        object Loading : LoginUiState
        object Success : LoginUiState
        data class Error(val message: String) : LoginUiState
    }
}
