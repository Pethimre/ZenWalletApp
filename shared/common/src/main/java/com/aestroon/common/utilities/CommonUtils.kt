package com.aestroon.common.utilities

import androidx.compose.ui.graphics.Color
import java.util.Calendar

val defaultWalletColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFF44336),
    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF009688)
)

fun generateRandomColor() = defaultWalletColors.random()

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun String.toColor(): Color {
    return try {
        if (this.startsWith("#")) Color(android.graphics.Color.parseColor(this)) else Color(android.graphics.Color.parseColor("#$this"))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }
}
