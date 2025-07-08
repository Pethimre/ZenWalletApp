package com.aestroon.common.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.data.repository.CategoryRepository
import com.aestroon.common.utilities.toHexString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletsUiState>(WalletsUiState.Idle)
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<CategoryEntity>> = authRepository.userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            categoryRepository.getCategoriesForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onEnterScreen() {
        viewModelScope.launch {
            authRepository.userIdFlow.firstOrNull()?.let {
                categoryRepository.syncCategories(it)
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
            val userId = authRepository.userIdFlow.first()
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

    private fun Color.toHexString(): String {
        return String.format("#%08X", this.toArgb())
    }
}