package com.aestroon.zenwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.zenwallet.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val success = authRepository.login(email, password)
            _uiState.value = if (success) LoginUiState.Success else LoginUiState.Error("Login failed")
        }
    }

    sealed interface LoginUiState {
        object Idle : LoginUiState
        object Loading : LoginUiState
        object Success : LoginUiState
        data class Error(val message: String) : LoginUiState
    }
}
