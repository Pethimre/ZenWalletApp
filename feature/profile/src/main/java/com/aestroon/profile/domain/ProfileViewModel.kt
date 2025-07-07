package com.aestroon.profile.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.authentication.data.UserRepository
import com.aestroon.authentication.data.model.UserProfile
import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY
import com.aestroon.common.data.repository.CurrencyRepository
import com.aestroon.profile.data.UserPreferencesRepository
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.data.serializable.ExchangeRateResponse
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _profileSettingsUiState = MutableStateFlow<ProfileSettingsUiState>(ProfileSettingsUiState.Idle)
    val profileSettingsUiState: StateFlow<ProfileSettingsUiState> = _profileSettingsUiState.asStateFlow()

    private val _user = MutableStateFlow<UserInfo?>(null)
    val user: StateFlow<UserInfo?> = _user.asStateFlow()

    private val _baseCurrency = MutableStateFlow("HUF")
    val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

    private val _allCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    val allCurrencies: StateFlow<List<Currency>> = _allCurrencies.asStateFlow()

    private val _currencySearchQuery = MutableStateFlow("")
    val currencySearchQuery: StateFlow<String> = _currencySearchQuery.asStateFlow()

    private val _exchangeRates = MutableStateFlow<ExchangeRateResponse?>(null)
    val exchangeRates: StateFlow<ExchangeRateResponse?> = _exchangeRates.asStateFlow()

    val filteredCurrencies: StateFlow<List<Currency>> =
        combine(allCurrencies, currencySearchQuery) { currencies, query ->
            if (query.isBlank()) {
                currencies
            } else {
                currencies.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.code.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isBiometricLockEnabled: StateFlow<Boolean> = userPreferencesRepository.isBiometricLockEnabled

    val displayName = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val worthGoal = MutableStateFlow("")

    init {
        loadInitialData()
        loadAllCurrencies()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _profileSettingsUiState.value = ProfileSettingsUiState.Loading
            authRepository.getUpdatedUser()
                .onSuccess { userInfo ->
                    _user.value = userInfo
                    displayName.value = userInfo?.userMetadata?.get("display_name")?.jsonPrimitive?.content ?: ""
                    phone.value = userInfo?.userMetadata?.get("phone")?.jsonPrimitive?.content ?: ""

                    userInfo?.id?.let { userId ->
                        userRepository.getUserProfile(userId)
                            .onSuccess { userProfile ->
                                worthGoal.value = userProfile?.worth_goal?.toString() ?: "0"
                                _profileSettingsUiState.value = ProfileSettingsUiState.Idle
                                _baseCurrency.value = userProfile?.base_currency ?: DEFAULT_BASE_CURRENCY
                            }
                            .onFailure {
                                worthGoal.value = "0"
                                _profileSettingsUiState.value = ProfileSettingsUiState.Idle
                            }
                    } ?: run {
                        _profileSettingsUiState.value = ProfileSettingsUiState.Error("Could not verify user ID.")
                    }
                }
                .onFailure {
                    _profileSettingsUiState.value = ProfileSettingsUiState.Error("Failed to fetch user data. Please restart the app.")
                }
        }
    }

    private fun loadAllCurrencies() {
        viewModelScope.launch {
            currencyRepository.getSupportedCurrencies()
                .onSuccess { _allCurrencies.value = it }
                .onFailure {
                    // Handle error, maybe show a snackbar
                }
        }
    }

    fun onCurrencySearchQueryChanged(query: String) {
        _currencySearchQuery.value = query
    }

    fun onBaseCurrencySelected(currencyCode: String) {
        _baseCurrency.value = currencyCode
    }

    fun fetchExchangeRates() {
        viewModelScope.launch {
            _profileSettingsUiState.value = ProfileSettingsUiState.Loading
            currencyRepository.getExchangeRates(baseCurrency.value)
                .onSuccess {
                    _exchangeRates.value = it
                    _profileSettingsUiState.value = ProfileSettingsUiState.Idle
                }
                .onFailure {
                    _profileSettingsUiState.value = ProfileSettingsUiState.Error("Failed to fetch exchange rates.")
                }
        }
    }

    fun updateProfile() {
        viewModelScope.launch {
            val userId = user.value?.id
            if (userId == null) {
                _profileSettingsUiState.value = ProfileSettingsUiState.Error("Cannot update profile: User not found.")
                return@launch
            }
            _profileSettingsUiState.value = ProfileSettingsUiState.Loading

            val authResult = authRepository.updateUser(displayName.value, phone.value)

            val goalLong = worthGoal.value.trim().toLongOrNull() ?: 0L
            val profileToSave = UserProfile(
                id = userId,
                worth_goal = goalLong,
                email = user.value?.email ?: "unknown_email_provided@error.com",
                display_name = displayName.value,
                base_currency = baseCurrency.value,
            )
            val profileResult = userRepository.upsertUserProfile(profileToSave)

            if (authResult.isSuccess && profileResult.isSuccess) {
                _profileSettingsUiState.value = ProfileSettingsUiState.Success("Profile updated successfully!")
            } else {
                val authError = if (authResult.isFailure) "Auth update failed. " else ""
                val profileError = if (profileResult.isFailure) "Profile update failed." else ""
                _profileSettingsUiState.value = ProfileSettingsUiState.Error((authError + profileError).trim())
            }
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            _profileSettingsUiState.value = ProfileSettingsUiState.Loading
            authRepository.updatePassword(password)
                .onSuccess {
                    _profileSettingsUiState.value = ProfileSettingsUiState.Success("Password updated successfully!")
                }
                .onFailure {
                    _profileSettingsUiState.value = ProfileSettingsUiState.Error(it.message ?: "Failed to update password.")
                }
        }
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        userPreferencesRepository.setBiometricLockEnabled(enabled)
    }

    fun resetUiState() {
        if (_profileSettingsUiState.value !is ProfileSettingsUiState.Loading) {
            _profileSettingsUiState.value = ProfileSettingsUiState.Idle
        }
    }
}
