package com.aestroon.common.data.entity

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.aestroon.common.presentation.IconProvider
import java.util.UUID

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconName: String,
    val color: String,
    val userId: String,
    val isSynced: Boolean = false
) {
    @get:Ignore
    val composeColor: Color
        get() = Color(color.toColorInt())

    @get:Ignore
    val icon: ImageVector
        get() = IconProvider.getCategoryIcon(iconName)
}