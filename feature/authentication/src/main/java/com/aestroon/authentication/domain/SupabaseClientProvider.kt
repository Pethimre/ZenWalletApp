package com.aestroon.authentication.domain

import com.aestroon.authentication.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter

object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            autoLoadFromStorage = true
            alwaysAutoRefresh = true
        }
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }

    suspend fun restoreSession() {
        client.auth.loadFromStorage()

        client.auth.sessionStatus
            .filter { it is SessionStatus.Authenticated || it is SessionStatus.NotAuthenticated }
            .first()
    }
}
