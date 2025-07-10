package com.aestroon.common.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aestroon.common.utilities.KEYSTORE_MASTERKEY
import com.aestroon.common.utilities.USER_PREF_MASTERKEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface UserPreferencesRepository {
    val isBiometricLockEnabled: StateFlow<Boolean>
    fun setBiometricLockEnabled(enabled: Boolean)
}

class UserPreferencesRepositoryImpl(
    context: Context
) : UserPreferencesRepository {

    private val masterKey = MasterKey.Builder(context, USER_PREF_MASTERKEY)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        KEYSTORE_MASTERKEY,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _isBiometricLockEnabled = MutableStateFlow(
        sharedPreferences.getBoolean("biometric_lock_enabled", false)
    )
    override val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()

    override fun setBiometricLockEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("biometric_lock_enabled", enabled)
            .apply()
        _isBiometricLockEnabled.value = enabled
    }
}