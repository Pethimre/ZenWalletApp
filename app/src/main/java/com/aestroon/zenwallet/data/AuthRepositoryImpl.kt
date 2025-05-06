package com.aestroon.zenwallet.data

import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepositoryImpl(private val auth: Auth) : AuthRepository {

    override suspend fun login(email: String, password: String): Boolean = try {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        true
    } catch (e: Exception) {
        false
    }

    override suspend fun signUp(email: String, password: String): Boolean = try {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        true
    } catch (e: Exception) {
        false
    }
}
