package com.aestroon.common.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aestroon.common.data.dao.CategoryDao
import com.aestroon.common.data.dao.UserDao
import com.aestroon.common.data.dao.WalletDao
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.entity.LocalUser
import com.aestroon.common.data.entity.WalletEntity

@Database(
    entities = [LocalUser::class, WalletEntity::class, CategoryEntity::class],
    version = 3, // <-- INCREMENT VERSION
    autoMigrations = [
        //AutoMigration(from = 1, to = 2) // <-- ADD AUTO MIGRATION
    ],
    exportSchema = true // <-- Set to true for schema validation and to export schema for migrations
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
}