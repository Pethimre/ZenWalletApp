package com.aestroon.common.data.serializable

import kotlinx.serialization.Serializable

@Serializable
data class PortfolioInstrument(
    val id: String,
    val portfolio_id: String,
    val user_id: String,
    val symbol: String,
    val name: String,
    val quantity: Double,
    val average_buy_price: Double,
    val currency: String,
    val maturity_date: String? = null,
    val coupon_rate: Double? = null,
    val lookup_price: Boolean = true,
    val last_updated_price: Double? = null,
    val last_updated_date: String? = null
)