package com.aestroon.common.utilities

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    fun formatSimpleBalance(balanceInCents: Long, currencyCode: String): String {
        val amount = balanceInCents / 100.0
        return try {
            NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                currency = java.util.Currency.getInstance(currencyCode)
            }.format(amount)
        } catch (e: Exception) {
            "${"%,.2f".format(amount)} $currencyCode"
        }
    }

    fun toPrettyAmountWithCurrency(
        amount: Double,
        currency: String,
        withSign: Boolean = false,
        currencyPosition: CurrencyPosition = CurrencyPosition.BEFORE
    ): String {
        val sign = if (withSign && amount > 0) "+" else ""
        val formatter = if (amount % 1.0 == 0.0) prettyIntFormatter else prettyDecimalFormatter
        val formattedAmount = "$sign${formatter.format(amount)}"

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


    fun Color.toHexString(): String {
        return String.format("#%08X", this.toArgb())
    }

    fun formatDateForDisplay(date: Date, locale: Locale = Locale.getDefault()): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val transactionCal = Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(transactionCal, today) -> "Today"
            isSameDay(transactionCal, yesterday) -> "Yesterday"
            else -> SimpleDateFormat("MMM d", locale).format(date)
        }
    }

    fun formatBalance(balanceInCents: Long, currencyCode: String): String {
        val amount = balanceInCents / 100.0
        return try {
            val currency = java.util.Currency.getInstance(currencyCode)
            val symbols = DecimalFormatSymbols(Locale.getDefault())
            val pattern = "#,##0.00"

            val formatter = DecimalFormat(pattern, symbols).apply {
                this.currency = currency
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }

            "${currency.symbol} ${formatter.format(amount)}"
        } catch (e: Exception) {
            "$currencyCode ${String.format(Locale.getDefault(), "%,.2f", amount)}"
        }
    }

    fun formatDayAndMonth(date: Date, locale: Locale = Locale.getDefault()): String {
        return SimpleDateFormat("MMMM d", locale).format(date)
    }

    enum class CurrencyPosition { BEFORE, AFTER }
}
