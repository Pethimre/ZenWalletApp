package com.aestroon.common.data.repository

import com.aestroon.common.data.CATEGORIES_TABLE_NAME
import com.aestroon.common.data.dao.CategoryDao
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.serializable.Category
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface CategoryRepository {
    fun getCategoriesForUser(userId: String): Flow<List<CategoryEntity>>
    suspend fun addOrUpdateCategory(category: CategoryEntity): Result<Unit>
    suspend fun deleteCategory(category: CategoryEntity): Result<Unit>
    suspend fun syncCategories(userId: String): Result<Unit>
}

class CategoryRepositoryImpl(
    private val postgrest: Postgrest,
    private val categoryDao: CategoryDao,
    private val connectivityObserver: ConnectivityObserver
) : CategoryRepository {

    override fun getCategoriesForUser(userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesForUser(userId)
    }

    override suspend fun addOrUpdateCategory(category: CategoryEntity): Result<Unit> = runCatching {
        categoryDao.insertCategory(category.copy(isSynced = false))
        syncCategories(category.userId)
    }

    override suspend fun deleteCategory(category: CategoryEntity): Result<Unit> = runCatching {
        categoryDao.deleteCategoryById(category.id)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from(CATEGORIES_TABLE_NAME).delete { filter { eq("id", category.id) } }
        }
    }

    override suspend fun syncCategories(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            return@runCatching
        }

        // Push local changes
        val unsynced = categoryDao.getUnsyncedCategories().first()
        if (unsynced.isNotEmpty()) {
            val networkCategories = unsynced.map { Category(it.id, it.name, it.iconName, it.color, it.userId) }
            postgrest.from(CATEGORIES_TABLE_NAME).upsert(networkCategories)
            unsynced.forEach { categoryDao.markCategoryAsSynced(it.id) }
        }

        // Pull remote changes
        val remote = postgrest.from(CATEGORIES_TABLE_NAME).select { filter { eq("user_id", userId) } }.decodeList<Category>()
        remote.forEach {
            val entity = CategoryEntity(it.id, it.name, it.icon_name, it.color, it.user_id, isSynced = true)
            categoryDao.insertCategory(entity)
        }
    }
}