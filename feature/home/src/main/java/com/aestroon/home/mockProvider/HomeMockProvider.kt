package com.aestroon.home.mockProvider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReplayCircleFilled
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aestroon.common.components.mockProvider.TransactionAction
import com.aestroon.common.components.mockProvider.TransactionItemData
import com.aestroon.common.components.mockProvider.TransactionTag
import com.aestroon.common.components.mockProvider.TransactionType
import com.aestroon.common.theme.PrimaryColor
import com.aestroon.common.theme.PrimaryFontColor
import com.aestroon.common.theme.RedChipColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Enum to represent the trend of the exchange rate
enum class RateTrend {
    UP, DOWN, STABLE
}

// Data class to hold information for each currency
data class CurrencyExchangeInfo(
    val currencyCode: String,
    val currencyName: String, // e.g., "Euro", "US Dollar"
    val icon: ImageVector,
    val rateInHUF: Double,
    val trend: RateTrend = RateTrend.STABLE
)

const val TOTAL_BALANCE = 5421610.22
const val GOAL = 7500000.0

// Sample data - In a real app, this would come from an API
val sampleExchangeRates = listOf(
    CurrencyExchangeInfo("EUR", "Euro", Icons.Filled.EuroSymbol, 385.25, RateTrend.UP),
    CurrencyExchangeInfo("USD", "US Dollar", Icons.Filled.AttachMoney, 350.70, RateTrend.DOWN),
    CurrencyExchangeInfo("GBP", "British Pound", Icons.Filled.CurrencyPound, 440.10, RateTrend.STABLE),
    CurrencyExchangeInfo("CHF", "Swiss Franc", Icons.Filled.CurrencyFranc, 392.50, RateTrend.UP)
)

val comprehensivePreviewTransactions: List<TransactionItemData> by lazy {
    val calendar = Calendar.getInstance()
    val today = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterday = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, -2)
    val fewDaysAgo = calendar.time
    calendar.time = today // Reset calendar
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val lastWeek = calendar.time

    listOf(
        // --- Today ---
        TransactionItemData(
            id = "today_income_1",
            title = "Project Windfall Bonus",
            subtitle = "Exceptional performance reward for Q2 deliverables and successful project completion ahead of schedule.",
            amount = 150000.0,
            transactionType = TransactionType.INCOME,
            date = today,
            dueDate = "RECEIVED TODAY",
            categoryIcon = Icons.Filled.EmojiEvents,
            categoryIconBackgroundColor = Color(0xFFFAD02C), // Gold
            categoryIconContentColor = Color.Black,
            tags = listOf(
                TransactionTag("Bonus", Icons.Filled.Star, backgroundColor = Color(0xFFFAD02C).copy(alpha = 0.7f), contentColor = Color.Black),
                TransactionTag("Q2 Project", Icons.Filled.BusinessCenter)
            ),
            actions = listOf(
                TransactionAction("Details", {}, icon = Icons.Filled.Info),
                TransactionAction("Allocate", {}, isPrimary = true, icon = Icons.Filled.Savings)
            )
        ),
        TransactionItemData(
            id = "today_expense_1",
            title = "Team Celebration Dinner",
            subtitle = "Post-project success meal at 'The Grand Bistro'",
            amount = 25000.0,
            transactionType = TransactionType.EXPENSE,
            date = today,
            categoryIcon = Icons.Filled.Restaurant,
            categoryIconBackgroundColor = PrimaryColor,
            categoryIconContentColor = PrimaryFontColor,
            tags = listOf(
                TransactionTag("Team Event", Icons.Filled.People),
                TransactionTag("Food", Icons.Filled.Fastfood)
            )
        ),
        // --- Yesterday ---
        TransactionItemData(
            id = "yesterday_income_1",
            title = "Freelance Gig - Logo Design for 'Startup Innovations Inc.'",
            subtitle = "Completed and approved final logo assets and brand guidelines document.",
            amount = 65000.0,
            transactionType = TransactionType.INCOME,
            date = yesterday,
            categoryIcon = Icons.Filled.Palette,
            tags = listOf(TransactionTag("Freelance", Icons.Filled.Work), TransactionTag("Design"))
        ),
        TransactionItemData(
            id = "yesterday_expense_1",
            title = "New Ergonomic Office Chair",
            amount = 48000.0,
            transactionType = TransactionType.EXPENSE,
            date = yesterday,
            categoryIcon = Icons.Filled.Chair,
            categoryIconBackgroundColor = Color(0xFF5C6BC0), // Indigo
            categoryIconContentColor = Color.White,
            tags = listOf(TransactionTag("Office Upgrade", Icons.Filled.Upgrade))
        ),
        // --- Few Days Ago ---
        TransactionItemData(
            id = "fewdays_income_1",
            title = "Online Course Sales",
            subtitle = "Monthly payout from 'LearnPlatform'",
            amount = 18500.0,
            transactionType = TransactionType.INCOME,
            date = fewDaysAgo,
            categoryIcon = Icons.Filled.School
        ),
        TransactionItemData(
            id = "fewdays_expense_1",
            title = "Software Subscription Renewal",
            subtitle = "Annual plan for 'DevTools Suite'",
            amount = 15000.0,
            transactionType = TransactionType.EXPENSE,
            date = fewDaysAgo,
            dueDate = "RENEWED: ${SimpleDateFormat("MMM d", Locale.getDefault()).format(fewDaysAgo)}",
            categoryIcon = Icons.Filled.Computer,
            tags = listOf(
                TransactionTag("Software", Icons.Filled.Code),
                TransactionTag("Subscription", Icons.Filled.Autorenew),
                TransactionTag("Work Tools", Icons.Filled.Build)
            )
        ),
        // --- Last Week ---
        TransactionItemData(
            id = "lastweek_expense_1",
            title = "Concert Tickets: The Rockers",
            amount = 12000.0,
            transactionType = TransactionType.EXPENSE,
            date = lastWeek,
            categoryIcon = Icons.Filled.MusicNote,
            actions = listOf(
                TransactionAction("View E-Ticket", {}, icon = Icons.AutoMirrored.Filled.ReceiptLong),
                TransactionAction("Add to Calendar", {}, isPrimary = true, icon = Icons.Filled.Event)
            )
        ),
        TransactionItemData(
            id = "lastweek_income_1",
            title = "Refund for Returned Item",
            subtitle = "Amazon order #123-4567890",
            amount = 5500.0,
            transactionType = TransactionType.INCOME,
            date = lastWeek,
            categoryIcon = Icons.Filled.Undo,
            tags = listOf(TransactionTag("Refund", Icons.Filled.ReplayCircleFilled))
        )
    )
}
