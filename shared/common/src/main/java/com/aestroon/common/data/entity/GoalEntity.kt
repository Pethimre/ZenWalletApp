package com.aestroon.common.data.entity

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.aestroon.common.presentation.IconProvider
import java.util.UUID

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val targetDate: Long?,
    val iconName: String?,
    val color: String,
    val isSynced: Boolean = false
) {
    @get:Ignore
    val composeColor: Color
        get() = try {
            Color(color.toColorInt())
        } catch (e: Exception) {
            Color.Gray
        }

    @get:Ignore
    val icon: ImageVector
        get() = IconProvider.getGoalIcon(iconName)
}