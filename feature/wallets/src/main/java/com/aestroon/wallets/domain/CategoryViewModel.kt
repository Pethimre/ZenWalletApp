package com.aestroon.wallets.domain

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.authentication.data.AuthRepository
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import toHexString

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    private val _uiState = MutableStateFlow<WalletsUiState>(WalletsUiState.Idle)
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
    }

    fun onEnterScreen() {
        viewModelScope.launch {
            authRepository.getUpdatedUser().getOrNull()?.id?.let {
                categoryRepository.syncCategories(it)
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            authRepository.getUpdatedUser().getOrNull()?.id?.let { userId ->
                categoryRepository.getCategoriesForUser(userId).collect {
                    _categories.value = it
                }
            }
        }
    }

    fun addOrUpdateCategory(
        existingCategory: CategoryEntity?,
        name: String,
        color: Color,
        iconName: String
    ) {
        viewModelScope.launch {
            val userId = authRepository.getUpdatedUser().getOrNull()?.id
            if (userId == null) {
                _uiState.value = WalletsUiState.Error("User not found."); return@launch
            }

            val category = existingCategory?.copy(
                name = name,
                color = color.toHexString(),
                iconName = iconName
            ) ?: CategoryEntity(
                name = name,
                color = color.toHexString(),
                iconName = iconName,
                userId = userId
            )

            categoryRepository.addOrUpdateCategory(category)
                .onFailure { _uiState.value = WalletsUiState.Error("Failed to save category.") }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
                .onFailure { _uiState.value = WalletsUiState.Error("Failed to delete category.") }
        }
    }
}