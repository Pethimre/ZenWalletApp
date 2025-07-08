package com.aestroon.common.utilities

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round
import kotlin.math.abs
import kotlin.math.absoluteValue

object TextFormatter {

    private val prettyIntFormatter = DecimalFormat("#,##0")
    private val prettyDecimalFormatter = DecimalFormat("#,##0.00")

    fun toPrettyAmount(amount: Double): String {
        val kAmount = amount / 1000
        val mAmount = amount / 1_000_000

        return when {
            amount.absoluteValue.mod(1.0) < 0.01 -> prettyIntFormatter.format(amount)
            else -> prettyDecimalFormatter.format(amount)
        }.let {
            when {
                mAmount >= 1 -> "${toPrettyAmount(mAmount)}M"
                kAmount >= 1 -> "${toPrettyAmount(kAmount)}K"
                else -> it
            }
        }
    }

    fun toPrettyAmountWithCurrency(
        amount: Double,
        currency: String,
        withSign: Boolean = false,
        currencyPosition: CurrencyPosition = CurrencyPosition.BEFORE
    ): String {
        val sign = if (withSign && amount > 0) "+" else ""
        val formattedAmount = "$sign${prettyDecimalFormatter.format(amount)}"
        return when (currencyPosition) {
            CurrencyPosition.BEFORE -> "$currency $formattedAmount"
            CurrencyPosition.AFTER -> "$formattedAmount $currency"
        }
    }

    fun toBasicFormat(amount: Double): String {
        return prettyDecimalFormatter.format(amount)
    }

    fun formatNewsTimestamp(date: Date?, locale: Locale = Locale.getDefault()): String {
        return date?.let {
            val sdf = SimpleDateFormat("hh:mma - EEEE", locale) // e.g., 10:30AM - Monday
            sdf.format(it)
        } ?: ""
    }

    fun formatPercentage(value: Double): String {
        val percentageFormatter = DecimalFormat("0.00'%'")
        return percentageFormatter.format(value)
    }

    enum class CurrencyPosition { BEFORE, AFTER }
}
