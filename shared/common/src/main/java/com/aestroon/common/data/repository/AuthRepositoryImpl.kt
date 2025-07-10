package com.aestroon.common.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aestroon.common.data.dao.UserDao
import com.aestroon.common.utilities.AUTH_MASTERKEY
import com.aestroon.common.utilities.AUTH_PREF_MASTERKEY
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Dispatcher
import java.security.MessageDigest

class AuthRepositoryImpl(
    private val auth: Auth,
    private val userDao: UserDao,
    private val connectivityObserver: ConnectivityObserver,
    private val context: Context
) : AuthRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context, AUTH_MASTERKEY)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            AUTH_PREF_MASTERKEY,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override val userIdFlow: Flow<String?> = sessionStatus().map { status ->
        (status as? SessionStatus.Authenticated)?.session?.user?.id
    }.distinctUntilChanged()


    override fun sessionStatus(): Flow<SessionStatus> = auth.sessionStatus

    override suspend fun login(email: String, password: String): Result<UserInfo?> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val session = auth.currentSessionOrNull()
        session?.refreshToken?.let { saveRefreshToken(it) }
        session?.user
    }

    override suspend fun signUp(displayName: String, email: String, password: String): Result<UserInfo?> = runCatching {
        try {
            val signedUpUser = auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("display_name", displayName)
                }
            }

            if (signedUpUser != null) {
                Log.d("AuthRepository", "Supabase signUpWith call successful for: ${signedUpUser.email}")
            } else {
                throw Exception("Supabase returned no user information after signup.")
            }

            signedUpUser

        } catch (e: Exception) {
            Log.e("AuthRepository", "${e.localizedMessage}")
            Log.e("AuthRepository", "Stack Trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun syncPendingUsers() {
        userDao.getUnsyncedUsers().first().forEach { userToSync ->
            if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
                Log.d("AuthRepository", "Syncing pending user: ${userToSync.email}")
                try {
                    auth.signUpWith(Email) {
                        this.email = userToSync.email
                        this.password = "password123" // Dummy password
                        this.data = buildJsonObject {
                            put("display_name", userToSync.displayName)
                        }
                    }
                    userDao.markUserAsSynced(userToSync.email)
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Failed to sync user: ${userToSync.email}", e)
                }
            }
        }
    }

    override suspend fun resendVerificationEmail(email: String): Result<Unit> = runCatching {
        auth.resendEmail(type = OtpType.Email.SIGNUP, email = email)
    }

    override fun isUserVerified(user: UserInfo?): Boolean {
        return user?.emailConfirmedAt != null
    }

    override suspend fun refreshSession(refreshToken: String): UserInfo? {
        return try {
            val newSession = auth.refreshSession(refreshToken)
            newSession?.refreshToken?.let { saveRefreshToken(it) }
            newSession?.user
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to refresh session", e)
            null
        }
    }

    override suspend fun getUpdatedUser(): Result<UserInfo?> = runCatching {
        val currentUser = auth.retrieveUserForCurrentSession(updateSession = true)
        if (currentUser != null) {
            return@runCatching currentUser
        }

        val refreshToken = getRefreshToken()
        if (refreshToken != null) {
            return@runCatching refreshSession(refreshToken)
        }

        Log.e("AuthRepository", "currentUser is null and no refresh token available")
        null
    }


    override suspend fun clearPendingUsers() {
        // Implementation remains the same
    }

    override suspend fun logout() {
        auth.signOut()
        clearRefreshToken()
        clearPendingUsers()
    }

    private fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    private fun clearRefreshToken() {
        sharedPreferences.edit().remove("refresh_token").apply()
    }

    override fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    override suspend fun updateUser(displayName: String, phone: String): Result<UserInfo> = runCatching {
        auth.updateUser {
            data {
                put("display_name", displayName)
                put("phone", phone)
            }
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<UserInfo> = runCatching {
        auth.updateUser {
            this.password = newPassword
        }
    }
}
