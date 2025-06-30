package com.aestroon.authentication.data

import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserInfo?>
    suspend fun signUp(displayName: String, email: String, password: String): Result<UserInfo?>
    suspend fun syncPendingUsers()
    suspend fun resendVerificationEmail(email: String): Result<Unit>
    fun isUserVerified(user: UserInfo?): Boolean
    suspend fun refreshSession(refreshToken: String): UserInfo?
    fun getRefreshToken(): String?
    suspend fun getUpdatedUser(): Result<UserInfo?>
    suspend fun clearPendingUsers()
    suspend fun logout()
    suspend fun updateUser(displayName: String, phone: String): Result<UserInfo>
    suspend fun updatePassword(newPassword: String): Result<UserInfo>
    fun sessionStatus(): Flow<SessionStatus>
}
