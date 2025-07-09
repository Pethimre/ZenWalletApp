package com.aestroon.common.data.repository

import com.aestroon.common.data.USERS_TABLE_NAME
import com.aestroon.common.data.serializable.UserProfile
import io.github.jan.supabase.postgrest.Postgrest

interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<UserProfile?>
    suspend fun upsertUserProfile(profile: UserProfile): Result<Unit>
}

class UserRepositoryImpl(private val postgrest: Postgrest) : UserRepository {
    override suspend fun getUserProfile(userId: String): Result<UserProfile?> = runCatching {
        postgrest.from(USERS_TABLE_NAME).select {
            filter {
                eq("id", userId)
            }
        }.decodeSingleOrNull<UserProfile>()
    }

    override suspend fun upsertUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        postgrest.from(USERS_TABLE_NAME).upsert(profile)
    }
}
