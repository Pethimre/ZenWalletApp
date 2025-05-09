package com.aestroon.zenwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.zenwallet.data.AuthRepository
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
        _isLoggedIn.value = userManager.isLoggedIn()
    }

    suspend fun restoreSession() {
        SupabaseClientProvider.restoreSession()
        _isLoggedIn.value = userManager.isLoggedIn()
        _restoreComplete.value = true
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
