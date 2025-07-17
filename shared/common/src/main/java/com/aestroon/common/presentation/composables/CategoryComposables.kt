package com.aestroon.common.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aestroon.common.data.entity.CategoryEntity
import com.aestroon.common.presentation.IconProvider
import defaultWalletColors
import generateRandomColor

@Composable
fun CategoryListItem(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.padding(horizontal = 8.dp),
        headlineContent = { Text(category.name, fontWeight = FontWeight.SemiBold) },
        leadingContent = {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = category.composeColor,
                modifier = Modifier.size(32.dp)
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
            }
        }
    )
}

@Composable
fun AddEditCategoryDialog(
    existingCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Color, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(existingCategory?.name ?: "") }
    var selectedColor by remember { mutableStateOf(existingCategory?.composeColor ?: generateRandomColor()) }
    var selectedIconName by remember { mutableStateOf(existingCategory?.iconName ?: IconProvider.categoryIcons.first().name) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (existingCategory == null) "Add New Category" else "Edit Category",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                Text("Icon:")
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(IconProvider.categoryIcons, key = { it.name }) { iconResource ->
                        IconButton(
                            onClick = { selectedIconName = iconResource.name },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(
                                if (selectedIconName == iconResource.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = iconResource.icon,
                                contentDescription = iconResource.name,
                                tint = if (selectedIconName == iconResource.name) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text("Color:")
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultWalletColors) { color ->
                        Box(
                            modifier = Modifier.size(36.dp).background(color, CircleShape)
                                .border(
                                    width = if (color == selectedColor) 2.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onConfirm(name, selectedColor, selectedIconName) },
                        enabled = name.isNotBlank()
                    ) {
                        Text(if (existingCategory == null) "Add" else "Save")
                    }
                }
            }
        }
    }
}
