package com.aestroon.common.data.model

import com.aestroon.common.data.entity.WalletEntity

data class WalletsSummary(
    val totalBalance: Long = 0L,
    val balanceBreakdown: List<Pair<WalletEntity, Float>> = emptyList()
)