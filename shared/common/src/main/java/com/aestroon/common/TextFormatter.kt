package com.aestroon.common

import java.text.DecimalFormat
import kotlin.math.round
import kotlin.math.abs

object TextFormatter {

    enum class CurrencyPosition {
        BEFORE, AFTER
    }

    private val decimalFormat = DecimalFormat("#,###.##")

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
            decimalFormat.format(value)
        }

        return "$sign$formatted${suffixes[index]}"
    }

    fun toPrettyCompactDecimal(
        amount: Double,
        currency: String = "$",
        round: Boolean = false,
        currencyPosition: CurrencyPosition = CurrencyPosition.BEFORE
    ): String {
        val formattedAmount = if (abs(amount) < 100_000) {
            decimalFormat.format(amount)
        } else {
            toPrettyAmount(amount, round)
        }

        return when (currencyPosition) {
            CurrencyPosition.BEFORE -> "$currency$formattedAmount"
            CurrencyPosition.AFTER -> "$formattedAmount$currency"
        }
    }
}
