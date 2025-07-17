package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.CurrencyConversionRepository
import com.aestroon.common.data.repository.UserRepository
import com.aestroon.common.data.serializable.UserProfile
import com.aestroon.common.data.repository.CurrencyRepository
import com.aestroon.common.data.repository.UserPreferencesRepository
import com.aestroon.common.data.serializable.Currency
import com.aestroon.common.presentation.state.CurrencyListUiState
import com.aestroon.common.presentation.state.ExchangeRateUiState
import com.aestroon.common.presentation.state.ProfileSettingsUiState
import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyConversionRepository: CurrencyConversionRepository
) : ViewModel() {

    private val _profileSettingsUiState = MutableStateFlow<ProfileSettingsUiState>(
        ProfileSettingsUiState.Idle)
    val profileSettingsUiState: StateFlow<ProfileSettingsUiState> = _profileSettingsUiState.asStateFlow()

    private val _exchangeRateUiState = MutableStateFlow<ExchangeRateUiState>(ExchangeRateUiState.Idle)
    val exchangeRateUiState: StateFlow<ExchangeRateUiState> = _exchangeRateUiState.asStateFlow()

    private val _currencyListUiState = MutableStateFlow<CurrencyListUiState>(CurrencyListUiState.Loading)
    val currencyListUiState: StateFlow<CurrencyListUiState> = _currencyListUiState.asStateFlow()

    private val _currencySearchQuery = MutableStateFlow("")
    val currencySearchQuery: StateFlow<String> = _currencySearchQuery.asStateFlow()
    val worthGoalInput = MutableStateFlow("")

    private val _savedWorthGoal = MutableStateFlow(0L)
    val savedWorthGoal: StateFlow<Long> = _savedWorthGoal.asStateFlow()

    private val _savedWorthGoalCurrency = MutableStateFlow(DEFAULT_BASE_CURRENCY)
    val savedWorthGoalCurrency: StateFlow<String> = _savedWorthGoalCurrency.asStateFlow()

    val baseCurrency: StateFlow<String> = currencyConversionRepository.baseCurrency
    val exchangeRates: StateFlow<Map<String, Double>?> = currencyConversionRepository.exchangeRates

    val isBiometricLockEnabled: StateFlow<Boolean> = userPreferencesRepository.isBiometricLockEnabled

    val displayName = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val worthGoal = MutableStateFlow("")

    val user: StateFlow<UserInfo?> = authRepository.userIdFlow.map { userId ->
        if (userId != null) authRepository.getUpdatedUser().getOrNull() else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val filteredCurrencies: StateFlow<List<Currency>> =
        combine(currencyListUiState, currencySearchQuery) { state, query ->
            when (state) {
                is CurrencyListUiState.Success -> {
                    if (query.isBlank()) {
                        state.currencies
                    } else {
                        state.currencies.filter {
                            it.name.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true)
                        }
                    }
                }
                else -> emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        observeUserProfile()
        viewModelScope.launch {
            currencyConversionRepository.loadInitialCurrency()
        }

        viewModelScope.launch {
            combine(
                baseCurrency,
                exchangeRates,
                _savedWorthGoal,
                _savedWorthGoalCurrency
            ) { currentBase, rates, savedGoal, savedCurrency ->
                val goalAsDouble = savedGoal.toDouble()
                if (rates == null || savedCurrency == currentBase) {
                    worthGoalInput.value = savedGoal.toString()
                } else {
                    val baseRate = rates[currentBase]
                    val goalRate = rates[savedCurrency]
                    if (baseRate != null && goalRate != null && goalRate != 0.0) {
                        val convertedValue = goalAsDouble * (baseRate / goalRate)
                        worthGoalInput.value = convertedValue.toLong().toString()
                    } else {
                        worthGoalInput.value = savedGoal.toString()
                    }
                }
            }.collect()
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            user.collect { userInfo ->
                if (userInfo != null) {
                    displayName.value = userInfo.userMetadata?.get("display_name")?.toString()?.trim('"') ?: ""
                    phone.value = userInfo.userMetadata?.get("phone")?.toString()?.trim('"') ?: ""
                    userRepository.getUserProfile(userInfo.id).getOrNull()?.let { profile ->
                        _savedWorthGoal.value = profile.worth_goal
                        _savedWorthGoalCurrency.value = profile.worth_goal_currency
                        currencyConversionRepository.setBaseCurrency(profile.base_currency)
                    }
                }
            }
        }
    }

    fun loadAllCurrencies() {
        viewModelScope.launch {
            _currencyListUiState.value = CurrencyListUiState.Loading
            currencyRepository.getSupportedCurrencies()
                .onSuccess { currencies ->
                    _currencyListUiState.value = CurrencyListUiState.Success(currencies)
                }
                .onFailure { error ->
                    _currencyListUiState.value = CurrencyListUiState.Error(error.message ?: "Failed to load currencies.")
                }
        }
    }

    fun onCurrencySearchQueryChanged(query: String) {
        _currencySearchQuery.value = query
    }

    fun onBaseCurrencySelected(currencyCode: String) {
        currencyConversionRepository.setBaseCurrency(currencyCode)
    }

    fun fetchExchangeRates() {
        viewModelScope.launch {
            _exchangeRateUiState.value = ExchangeRateUiState.Loading
            currencyRepository.getExchangeRates(baseCurrency.value)
                .onSuccess {
                    _exchangeRateUiState.value = ExchangeRateUiState.Success(it)
                }
                .onFailure {
                    _exchangeRateUiState.value = ExchangeRateUiState.Error("Failed to fetch exchange rates. Please check your connection or API key.")
                }
        }
    }

    fun dismissExchangeRateSheet() {
        _exchangeRateUiState.value = ExchangeRateUiState.Idle
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

            val goalLong = worthGoalInput.value.trim().toLongOrNull() ?: 0L
            val currentBaseCurrency = currencyConversionRepository.baseCurrency.value

            val profileToSave = UserProfile(
                id = userId,
                worth_goal = goalLong,
                email = user.value?.email ?: "unknown_email_provided@error.com",
                display_name = displayName.value,
                base_currency = currentBaseCurrency,
                worth_goal_currency = currentBaseCurrency
            )
            val profileResult = userRepository.upsertUserProfile(profileToSave)

            if (authResult.isSuccess && profileResult.isSuccess) {
                _profileSettingsUiState.value = ProfileSettingsUiState.Success("Profile updated successfully!")
                _savedWorthGoal.value = goalLong
                _savedWorthGoalCurrency.value = currentBaseCurrency
            } else {
                _profileSettingsUiState.value = ProfileSettingsUiState.Error("Failed to update profile.")
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
