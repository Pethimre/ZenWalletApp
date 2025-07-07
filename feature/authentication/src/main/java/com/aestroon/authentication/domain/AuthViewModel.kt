package com.aestroon.authentication.domain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _loginUiState = MutableStateFlow<UiState>(UiState.Idle)
    val loginUiState: StateFlow<UiState> = _loginUiState

    private val _signUpUiState = MutableStateFlow<UiState>(UiState.Idle)
    val signUpUiState: StateFlow<UiState> = _signUpUiState

    private val _verificationUiState = MutableStateFlow<UiState>(UiState.Idle)
    val verificationUiState: StateFlow<UiState> = _verificationUiState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _restoreComplete = MutableStateFlow(false)
    val restoreComplete: StateFlow<Boolean> = _restoreComplete

    private val _networkStatus = MutableStateFlow(ConnectivityObserver.Status.Available)
    val networkStatus: StateFlow<ConnectivityObserver.Status> = _networkStatus

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _isAuthFlowActive = MutableStateFlow(false)

    init {
        observeSessionStatus()
        viewModelScope.launch(Dispatchers.IO) {
            connectivityObserver.observe().collect { status ->
                if (status == ConnectivityObserver.Status.Available && !_isAuthFlowActive.value) {
                    Log.d("AuthViewModel", "Network is available and auth flow is not active, attempting to sync pending users.")
                    authRepository.syncPendingUsers()
                }
            }
        }
    }

    fun observeSessionStatus() {
        viewModelScope.launch {
            authRepository.sessionStatus().collect { status ->
                val user = if (status is SessionStatus.Authenticated) status.session.user else null
                _isLoggedIn.value = authRepository.isUserVerified(user)
            }
        }
    }

    fun setAuthFlowActive(isActive: Boolean) {
        _isAuthFlowActive.value = isActive
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = UiState.Loading
            authRepository.login(email, password)
                .onSuccess { user ->
                    if (authRepository.isUserVerified(user)) {
                        _loginUiState.value = UiState.Success("Login Successful!")
                        _isLoggedIn.value = true
                    } else {
                        _navigationEvent.emit(NavigationEvent.ToVerifyEmail(user?.email ?: ""))
                        _loginUiState.value = UiState.Idle // Reset to idle after event
                    }
                }
                .onFailure {
                    _loginUiState.value = UiState.Error(it.message ?: "Unknown login error")
                }
        }
    }

    fun signUp(displayName: String, email: String, password: String) {
        viewModelScope.launch {
            _signUpUiState.value = UiState.Loading

            authRepository.signUp(displayName, email, password)
                .onSuccess { user ->
                    if (user != null) {
                        _navigationEvent.emit(NavigationEvent.ToVerifyEmail(user.email ?: ""))
                        _signUpUiState.value = UiState.Idle
                    } else {
                        _signUpUiState.value = UiState.Success("Signup successful! You are offline. Please connect to verify your email.")
                    }
                }
                .onFailure { error ->
                    _signUpUiState.value = UiState.Error(error.message ?: "An unknown signup error occurred.")
                }
        }
    }

    fun checkVerificationStatus() {
        _verificationUiState.value = UiState.Success("Verification complete! Please log in to continue.")
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _verificationUiState.value = UiState.Loading
            authRepository.resendVerificationEmail(email)
                .onSuccess { _verificationUiState.value = UiState.Success("Verification email sent!") }
                .onFailure { _verificationUiState.value = UiState.Error(it.message ?: "Failed to send email.") }
        }
    }

    fun resetVerificationState() {
        _verificationUiState.value = UiState.Idle
    }

    fun restoreSession() {
        viewModelScope.launch {
            val refreshToken = authRepository.getRefreshToken()
            if (refreshToken != null) {
                val user = authRepository.refreshSession(refreshToken)
                _isLoggedIn.value = authRepository.isUserVerified(user)
            } else {
                _isLoggedIn.value = false
            }
            _restoreComplete.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            userManager.logout()
            _isLoggedIn.value = false
        }
    }

    sealed interface UiState {
        object Idle : UiState
        object Loading : UiState
        data class Success(val message: String) : UiState
        data class Error(val message: String) : UiState
    }

    sealed interface NavigationEvent {
        data class ToVerifyEmail(val email: String) : NavigationEvent
    }
}
