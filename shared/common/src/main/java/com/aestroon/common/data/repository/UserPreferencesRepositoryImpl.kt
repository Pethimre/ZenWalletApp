package com.aestroon.common.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.AEADBadTagException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

interface UserPreferencesRepository {
    val isBiometricLockEnabled: StateFlow<Boolean>
    fun setBiometricLockEnabled(enabled: Boolean)
}

class UserPreferencesRepositoryImpl(
    private val context: Context
) : UserPreferencesRepository {

    companion object {
        private const val PREFS_FILENAME = "zen_wallet_user_prefs"
        private const val MASTER_KEY_ALIAS = "_zen_wallet_master_key_"
        private const val KEY_BIOMETRIC_LOCK_ENABLED = "biometric_lock_enabled"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        createEncryptedPrefs()
    }

    /**
     * Creates EncryptedSharedPreferences with a try-catch block to handle decryption failures.
     * If decryption fails (e.g., after an app reinstall or data clear), it deletes the
     * corrupt preferences file and creates a new, empty one.
     */
    private fun createEncryptedPrefs(): SharedPreferences {
        try {
            val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            if (e is AEADBadTagException || e is IOException) {
                Log.w("UserPrefsRepo", "Failed to decrypt preferences, resetting.", e)

                // Delete the corrupted SharedPreferences file.
                context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE).edit().clear().apply()

                // Retry creating the EncryptedSharedPreferences. This will now create a fresh file.
                return createEncryptedPrefs()
            } else {
                // For any other unexpected exceptions, re-throw them to crash the app,
                // as it might indicate a more serious, unrecoverable issue.
                Log.e("UserPrefsRepo", "Failed to create encrypted preferences", e)
                throw e
            }
        }
    }

    private val _isBiometricLockEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_BIOMETRIC_LOCK_ENABLED, false)
    )
    override val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()

    override fun setBiometricLockEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_BIOMETRIC_LOCK_ENABLED, enabled)
            .apply()
        _isBiometricLockEnabled.value = enabled
    }
}
