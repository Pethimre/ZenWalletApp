package com.aestroon.common.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.aestroon.common.data.dao.UserDao
import com.aestroon.common.data.entity.LocalUser

@Database(
    entities = [LocalUser::class],
    version = 1, // <-- INCREMENT VERSION
    autoMigrations = [
        //AutoMigration(from = 1, to = 2) // <-- ADD AUTO MIGRATION
    ],
    exportSchema = true // <-- Set to true for schema validation and to export schema for migrations
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}