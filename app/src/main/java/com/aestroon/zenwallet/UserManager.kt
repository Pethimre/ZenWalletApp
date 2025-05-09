package com.aestroon.zenwallet

import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.user.UserInfo

class UserManager(private val auth: Auth) {

    val currentUserInfo: UserInfo?
        get() = auth.currentSessionOrNull()?.user

    fun isLoggedIn(): Boolean = currentUserInfo != null

    suspend fun logout() {
        auth.signOut()
    }
}
