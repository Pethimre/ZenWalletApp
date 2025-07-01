package com.aestroon.common.data.entity

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.aestroon.common.presentation.WalletIconProvider
import java.util.UUID

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val balance: Long,
    val color: String,
    val currency: String,
    val ownerId: String,
    val iconName: String,
    val included: Boolean = true,
    val goalAmount: Long = 0L,
    val isSynced: Boolean = false
) {
    @get:Ignore
    val composeColor: Color
        get() = Color(color.toColorInt())

    @get:Ignore
    val icon: ImageVector
        get() = WalletIconProvider.getIconByName(iconName)
}