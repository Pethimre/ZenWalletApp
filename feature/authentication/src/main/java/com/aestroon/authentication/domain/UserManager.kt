package com.aestroon.authentication.domain

import com.aestroon.authentication.data.AuthRepository
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.user.UserInfo

class UserManager(private val authRepository: AuthRepository) {
    suspend fun logout() {
        authRepository.logout()
    }
    // You could add other user-related business logic here in the future
}
