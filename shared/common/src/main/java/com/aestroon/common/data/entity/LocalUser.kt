package com.aestroon.common.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class LocalUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val displayName: String,
    @ColumnInfo(index = true) val email: String,
    val passwordHash: String, // Store a hash, not the plaintext password
    val baseCurrencyId: Int = 1, // Default value
    val worthGoal: Long = 0, // Default value
    val photoUrl: String? = null, // Default value
    var isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = System.currentTimeMillis()
)