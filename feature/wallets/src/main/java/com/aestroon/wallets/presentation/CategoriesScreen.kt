package com.aestroon.wallets.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.domain.CategoriesViewModel
import com.aestroon.wallets.presentation.composables.AddEditCategoryDialog
import com.aestroon.wallets.presentation.composables.CategoryListItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onEnterScreen()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                categoryToEdit = null
                showAddEditDialog = true
            }) {
                Icon(Icons.Default.Add, "Add Category")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryListItem(
                    category = category,
                    onEdit = {
                        categoryToEdit = category
                        showAddEditDialog = true
                    },
                    onDelete = { viewModel.deleteCategory(category) }
                )
            }
        }
    }

    if (showAddEditDialog) {
        AddEditCategoryDialog(
            existingCategory = categoryToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { name, color, iconName ->
                viewModel.addOrUpdateCategory(categoryToEdit, name, color, iconName)
                showAddEditDialog = false
            }
        )
    }
}
