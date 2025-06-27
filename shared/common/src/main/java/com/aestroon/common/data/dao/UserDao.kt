package com.aestroon.common.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aestroon.common.data.entity.LocalUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUser)

    @Query("SELECT * FROM users WHERE isSynced = 0")
    fun getUnsyncedUsers(): Flow<List<LocalUser>>

    @Query("UPDATE users SET isSynced = 1 WHERE email = :email")
    suspend fun markUserAsSynced(email: String)

    @Delete
    suspend fun deleteUser(user: LocalUser)
}