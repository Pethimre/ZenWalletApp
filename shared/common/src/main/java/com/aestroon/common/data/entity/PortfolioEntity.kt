package com.aestroon.common.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "portfolios")
data class PortfolioEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String?,
    val balance: Long,
    val color: String,
    val iconName: String?,
    val type: String,
    val userId: String,
    val isSynced: Boolean = false
)