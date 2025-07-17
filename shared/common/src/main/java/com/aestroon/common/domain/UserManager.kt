package com.aestroon.common.domain

import com.aestroon.common.data.repository.AuthRepository

class UserManager(private val authRepository: AuthRepository) {
    suspend fun logout() {
        authRepository.logout()
    }
}
