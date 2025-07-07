package com.aestroon.common.utilities

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

fun formatDayAndMonth(date: Date, locale: Locale = Locale.getDefault()): String {
    return SimpleDateFormat("MMMM d", locale).format(date)
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun Color.toHexString(): String {
    return String.format("#%08X", this.toArgb())
}

fun String.toColor(): Color {
    return try {
        if (this.startsWith("#")) Color(android.graphics.Color.parseColor(this)) else Color(android.graphics.Color.parseColor("#$this"))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }
}