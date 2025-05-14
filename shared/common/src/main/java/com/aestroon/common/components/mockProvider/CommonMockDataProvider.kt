package com.aestroon.common.components.mockProvider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aestroon.common.theme.FaintBlueChipColor
import com.aestroon.common.theme.PrimaryColor
import java.util.Date

const val MOCK_BASE_CURRENCY = "HUF"

val sampleUpcomingTransactions = listOf(
    TransactionItemData(
        id = "1",
        title = "CR Discount",
        subtitle = "University Credit Fee Rebate",
        amount = 362.00,
        transactionType = TransactionType.INCOME,
        dueDate = "DUE ON TUE, 20 MAY",
        categoryIcon = Icons.Filled.School,
        categoryIconBackgroundColor = Color(0xFF6F42C1), // Purple like
        categoryIconContentColor = Color.White,
        tags = listOf(
            TransactionTag(
                "Fees",
                Icons.AutoMirrored.Filled.ReceiptLong,
                backgroundColor = Color(0xFFDC3545).copy(alpha = 0.8f),
                contentColor = Color.White
            ), // Reddish
            TransactionTag(
                "UniCredit",
                Icons.Filled.AccountBalance,
                backgroundColor = Color.DarkGray.copy(alpha = 0.5f),
                contentColor = Color.White
            )
        ),
        actions = listOf(
            TransactionAction("Skip", {}, icon = Icons.Filled.SkipNext),
            TransactionAction("Get", {}, isPrimary = true, icon = Icons.Filled.CheckCircle)
        ),
    ),
    TransactionItemData(
        id = "2",
        title = "Gym Membership",
        subtitle = "Monthly subscription",
        amount = 5000.00,
        transactionType = TransactionType.EXPENSE,
        dueDate = "DUE ON FRI, 23 MAY",
        categoryIcon = Icons.Filled.FitnessCenter,
        tags = listOf(
            TransactionTag("Subscription", Icons.Filled.Autorenew),
            TransactionTag("Bank", Icons.Default.AttachMoney)
        ),
        actions = listOf(
            TransactionAction("Skip", {}, icon = Icons.Filled.SkipNext),
            TransactionAction("Pay", {}, isPrimary = true, icon = Icons.Filled.CheckCircle)
        ),
    )
)

val sampleOverdueTransactions = listOf(
    TransactionItemData(
        id = "3",
        title = "Electricity Bill",
        amount = 4499.00,
        transactionType = TransactionType.EXPENSE,
        categoryIcon = Icons.Filled.Lightbulb,
        tags = listOf(TransactionTag("Utilities", Icons.Filled.HomeWork), TransactionTag("T212", Icons.Default.Museum)),
        actions = listOf(
            TransactionAction("Skip", {}, icon = Icons.Filled.SkipNext),
            TransactionAction("Pay", {}, isPrimary = true, icon = Icons.Filled.CheckCircle)
        ),
    )
)

val sampleDailyTransactions = listOf(
    TransactionItemData(
        id = "4",
        title = "RHM",
        subtitle = "Dividend from Rheinmetall",
        amount = 135.50,
        transactionType = TransactionType.INCOME,
        categoryIcon = Icons.Filled.TrendingUp, // Using a generic icon
        tags = listOf(
            TransactionTag(
                "Dividends",
                Icons.Filled.PieChart,
                backgroundColor = Color(0xFF007BFF).copy(alpha = 0.8f),
                contentColor = Color.White
            ), // Blueish
            TransactionTag(
                "QMMF",
                Icons.Filled.AccountBalanceWallet,
                backgroundColor = Color.DarkGray.copy(alpha = 0.5f),
                contentColor = Color.White
            )
        )
    ),
    TransactionItemData(
        id = "5",
        title = "SAP",
        subtitle = "Stock Dividend",
        amount = 39.39,
        transactionType = TransactionType.INCOME,
        categoryIcon = Icons.Filled.TrendingUp,
        tags = listOf(
            TransactionTag(
                "Dividends",
                Icons.Filled.PieChart,
                backgroundColor = Color(0xFF007BFF).copy(alpha = 0.8f),
                contentColor = Color.White
            ),
            TransactionTag(
                "QMMF",
                Icons.Filled.AccountBalanceWallet,
                backgroundColor = Color.DarkGray.copy(alpha = 0.5f),
                contentColor = Color.White
            )
        )
    ),
    TransactionItemData(
        id = "6",
        title = "Lunch at Cafe",
        subtitle = "With colleagues",
        amount = 3500.00,
        transactionType = TransactionType.EXPENSE,
        categoryIcon = Icons.Filled.Fastfood,
        tags = listOf(TransactionTag("Food", Icons.Filled.Restaurant))
    )
)

enum class TransactionType {
    INCOME, EXPENSE
}

data class TransactionTag(
    val label: String,
    val icon: ImageVector? = null, // Optional icon for the tag
    val backgroundColor: Color = Color.DarkGray.copy(alpha = 0.3f),
    val contentColor: Color = Color.White.copy(alpha = 0.8f)
)

data class TransactionAction(
    val label: String,
    val onClick: () -> Unit,
    val isPrimary: Boolean = false,
    val icon: ImageVector? = null
)

data class TransactionItemData(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val amount: Double,
    val currencySymbol: String = "HUF",
    val transactionType: TransactionType,
    val date: Date? = null, // For display or sorting
    val dueDate: String? = null, // e.g., "DUE ON TUE, 20 MAY"
    val categoryIcon: ImageVector = Icons.Filled.Category,
    val categoryIconBackgroundColor: Color = PrimaryColor,
    val categoryIconContentColor: Color = FaintBlueChipColor,
    val tags: List<TransactionTag> = emptyList(),
    val actions: List<TransactionAction> = emptyList()
)