package com.aestroon.common.data.dao

import androidx.room.*
import com.aestroon.common.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesForUser(userId: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE isSynced = 0")
    fun getUnsyncedCategories(): Flow<List<CategoryEntity>>

    @Query("UPDATE categories SET isSynced = 1 WHERE id = :categoryId")
    suspend fun markCategoryAsSynced(categoryId: String)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)
}