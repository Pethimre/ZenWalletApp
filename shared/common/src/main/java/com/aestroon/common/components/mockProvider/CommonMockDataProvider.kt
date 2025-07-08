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
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.theme.FaintBlueChipColor
import com.aestroon.common.theme.PrimaryColor
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Helper function to create dates relative to the current day for realistic previews.
 * @param offsetDays Number of days to offset from today. 0 is today, -1 is yesterday, etc.
 * @param hour The hour of the day (0-23).
 * @param minute The minute of the hour (0-59).
 * @return A Date object with the specified offset.
 */
private fun createDate(offsetDays: Int, hour: Int, minute: Int): Date {
    return Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offsetDays)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.time
}

/**
 * A list of sample transactions used for UI previews and testing.
 */
val sampleDailyTransactions: List<TransactionEntity> = listOf(
    // Today's Transactions
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 550000, // 5500.00
        currency = "HUF",
        name = "Lunch at Vapiano",
        description = "Pasta and pizza",
        date = createDate(0, 13, 15),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_food",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    ),
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 12000000, // 120000.00
        currency = "HUF",
        name = "Salary",
        description = "July Salary",
        date = createDate(0, 9, 5),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_income",
        transactionType = TransactionType.INCOME,
        toWalletId = null
    ),
    // Yesterday's Transactions
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 250000, // 2500.00
        currency = "HUF",
        name = "Groceries from Lidl",
        description = "Weekly shopping",
        date = createDate(-1, 18, 30),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_shopping",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    ),
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 1000000, // 10000.00
        currency = "HUF",
        name = "Transfer to Savings",
        description = null,
        date = createDate(-1, 10, 0),
        userId = "user1",
        walletId = "wallet1",
        toWalletId = "wallet2",
        categoryId = null,
        transactionType = TransactionType.TRANSFER
    ),
    // Two days ago
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 899000, // 8990.00
        currency = "HUF",
        name = "Cinema City",
        description = "Movie night",
        date = createDate(-2, 20, 45),
        userId = "user1",
        walletId = "wallet2",
        categoryId = "cat_entertainment",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    ),
    // A week ago
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 1500000, // 15000.00
        currency = "HUF",
        name = "Freelance Project Payment",
        description = "Logo design",
        date = createDate(-7, 15, 0),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_income",
        transactionType = TransactionType.INCOME,
        toWalletId = null
    )
)

/**
 * A list of sample transactions used for UI previews and testing.
 */
val sampleUpcomingTransactions: List<TransactionEntity> = listOf(
    // Today's Transactions
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 550000, // 5500.00
        currency = "HUF",
        name = "Lunch at Vapiano",
        description = "Pasta and pizza",
        date = createDate(0, 13, 15),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_food",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    ),
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 12000000, // 120000.00
        currency = "HUF",
        name = "Salary",
        description = "July Salary",
        date = createDate(0, 9, 5),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_income",
        transactionType = TransactionType.INCOME,
        toWalletId = null
    ),
    // Yesterday's Transactions
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 250000, // 2500.00
        currency = "HUF",
        name = "Groceries from Lidl",
        description = "Weekly shopping",
        date = createDate(-1, 18, 30),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_shopping",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    ),
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 1000000, // 10000.00
        currency = "HUF",
        name = "Transfer to Savings",
        description = null,
        date = createDate(-1, 10, 0),
        userId = "user1",
        walletId = "wallet1",
        toWalletId = "wallet2",
        categoryId = null,
        transactionType = TransactionType.TRANSFER
    ),
    // Two days ago
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 899000, // 8990.00
        currency = "HUF",
        name = "Cinema City",
        description = "Movie night",
        date = createDate(-2, 20, 45),
        userId = "user1",
        walletId = "wallet2",
        categoryId = "cat_entertainment",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    ),
    // A week ago
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 1500000, // 15000.00
        currency = "HUF",
        name = "Freelance Project Payment",
        description = "Logo design",
        date = createDate(-7, 15, 0),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_income",
        transactionType = TransactionType.INCOME,
        toWalletId = null
    )
)

/**
 * A list of sample overdue transactions for UI previews.
 */
val sampleOverdueTransactions: List<TransactionEntity> = listOf(
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 15000000, // 150000.00
        currency = "HUF",
        name = "Rent Payment",
        description = "July Rent",
        date = createDate(-5, 9, 0),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_bills",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    ),
    TransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = 350000, // 3500.00
        currency = "HUF",
        name = "Internet Bill",
        description = "Digi Internet",
        date = createDate(-2, 11, 0),
        userId = "user1",
        walletId = "wallet1",
        categoryId = "cat_bills",
        transactionType = TransactionType.EXPENSE,
        toWalletId = null
    )
)

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