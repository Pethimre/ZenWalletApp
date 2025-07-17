package com.aestroon.common.domain

import com.aestroon.common.data.entity.CategoryEntity
import java.util.Date

data class TransactionUiModel(
    val id: String,
    val name: String,
    val date: Date,
    val amount: Double,
    val currency: String,
    val type: UiTransactionType,
    val category: CategoryEntity?
)
