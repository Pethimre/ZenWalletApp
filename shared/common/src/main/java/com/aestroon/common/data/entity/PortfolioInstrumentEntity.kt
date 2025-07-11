package com.aestroon.common.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "portfolio_instruments",
    foreignKeys = [
        ForeignKey(
            entity = PortfolioEntity::class,
            parentColumns = ["id"],
            childColumns = ["portfolioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PortfolioInstrumentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val portfolioId: String,
    val symbol: String,
    val name: String,
    val quantity: Double,
    val averageBuyPrice: Double,
    val currency: String,
    val maturityDate: Long? = null,
    val couponRate: Double? = null,
    val isSynced: Boolean = false
)