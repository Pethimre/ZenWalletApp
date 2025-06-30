package com.aestroon.profile.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.authentication.data.AuthRepository
import com.aestroon.authentication.data.UserRepository
import com.aestroon.authentication.data.model.UserProfile
import com.aestroon.profile.data.UserPreferencesRepository
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _profileSettingsUiState = MutableStateFlow<ProfileSettingsUiState>(ProfileSettingsUiState.Idle)
    val profileSettingsUiState: StateFlow<ProfileSettingsUiState> = _profileSettingsUiState.asStateFlow()

    private val _user = MutableStateFlow<UserInfo?>(null)
    val user: StateFlow<UserInfo?> = _user.asStateFlow()

    val isBiometricLockEnabled: StateFlow<Boolean> = userPreferencesRepository.isBiometricLockEnabled

    val displayName = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val worthGoal = MutableStateFlow("")

    init {
        loadInitialData()
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
