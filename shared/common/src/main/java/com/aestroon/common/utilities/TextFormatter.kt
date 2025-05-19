package com.aestroon.common.utilities

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round
import kotlin.math.abs

object TextFormatter {

    enum class CurrencyPosition {
        BEFORE, AFTER
    }

    private val twoDecimalFormat = DecimalFormat("#,##0.00")

    fun toBasicFormat(amount: Double) = "%,.2f".format(amount)

    fun toPrettyAmountWithCurrency(
        amount: Double,
        currency: String = "$",
        round: Boolean = false,
        currencyPosition: CurrencyPosition = CurrencyPosition.BEFORE
    ): String {
        val formattedAmount = toPrettyAmount(amount, round)
        return when (currencyPosition) {
            CurrencyPosition.BEFORE -> "$currency$formattedAmount"
            CurrencyPosition.AFTER -> "$formattedAmount $currency"
        }
    }

    fun toPrettyAmount(
        amount: Double,
        round: Boolean = false
    ): String {
        val absAmount = abs(amount)
        val sign = if (amount < 0) "-" else ""

        val suffixes = listOf("", "K", "M", "B", "T")
        var value = absAmount
        var index = 0

        while (value >= 1000 && index < suffixes.size - 1) {
            value /= 1000
            index++
        }

        val formatted = if (round) {
            DecimalFormat("#,###").format(round(value))
        } else {
            twoDecimalFormat.format(value)
        }

        return "$sign$formatted${suffixes[index]}"
    }

    fun formatPercentage(value: Double, locale: Locale = Locale.getDefault()): String {
        return if (value % 1.0 == 0.0) {
            String.format(locale, "%d%%", value.toInt())
        } else {
            String.format(locale, "%.2f%%", value)
        }
    }

    fun toPrettyCompactDecimal(
        amount: Double,
        currency: String = "$",
        round: Boolean = false,
        currencyPosition: CurrencyPosition = CurrencyPosition.BEFORE
    ): String {
        val formattedAmount = if (abs(amount) < 100_000) {
            twoDecimalFormat.format(amount)
        } else {
            toPrettyAmount(amount, round)
        }

        return when (currencyPosition) {
            CurrencyPosition.BEFORE -> "$currency$formattedAmount"
            CurrencyPosition.AFTER -> "$formattedAmount$currency"
        }
    }

    fun formatNewsDate(date: Date?, pattern: String = "MMM dd, yyyy 'at' hh:mma", locale: Locale = Locale.getDefault()): String {
        return date?.let { SimpleDateFormat(pattern, locale).format(it) } ?: "Date N/A"
    }
    fun formatNewsTimestamp(date: Date?, locale: Locale = Locale.getDefault()): String {
        return date?.let {
            val sdf = SimpleDateFormat("hh:mma - EEEE", locale) // e.g., 10:30AM - Monday
            sdf.format(it)
        } ?: ""
    }
}
