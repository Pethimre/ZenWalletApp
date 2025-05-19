package com.aestroon.wallets.mockProvider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Date
import kotlin.random.Random

enum class WalletType(val displayName: String, val icon: ImageVector) {
    DEBIT_CARD("Debit Card", Icons.Filled.CreditCard),
    CREDIT_CARD("Credit Card", Icons.Filled.Payment),
    E_WALLET("E-Wallet", Icons.Filled.AccountBalanceWallet),
    CASH("Cash", Icons.Filled.Money),
    PREPAID_CARD("Prepaid Card", Icons.Filled.CardMembership),
    OTHER("Other", Icons.Filled.Wallet)
}

data class MiniTransaction(
    val id: String,
    val description: String,
    val amount: Double, // Positive for income, negative for expense
    val date: Date
)

data class SpendingWallet(
    val id: String,
    var name: String,
    var type: WalletType,
    var balance: Double,
    val currency: String = "HUF",
    var color: Color, // For UI theming/identification, can be generated or user-picked
    val lastTransactions: List<MiniTransaction> = emptyList(), // Optional for quick glance
    val monthlyIncome: Double = Random.nextDouble(50000.0, 500000.0), // Mock
    val monthlyExpense: Double = Random.nextDouble(20000.0, 300000.0) // Mock
)

data class MonthlyFlow(
    val monthYear: String, // e.g., "May 2025"
    val income: Double,
    val expense: Double
)

data class WalletsScreenSummary(
    val totalBalance: Double,
    val totalMonthlyIncome: Double,
    val totalMonthlyExpense: Double,
    val balanceBreakdown: List<Pair<SpendingWallet, Float>>, // Wallet and its percentage of total
    val recentMonthlyFlows: List<MonthlyFlow> // Last 3-6 months
)

fun mockMiniTransactions(count: Int): List<MiniTransaction> {
    return List(count) {
        MiniTransaction(
            id = "tx_$it",
            description = listOf(
                "Groceries",
                "Salary",
                "Online Shopping",
                "Dinner Out",
                "Subscription"
            )[it % 5],
            amount = Random.nextDouble(-50000.0, 100000.0),
            date = Date(System.currentTimeMillis() - Random.nextLong(1000L * 60 * 60 * 24 * 5))
        )
    }
}

fun mockSpendingWallets(count: Int): List<SpendingWallet> {
    val names = listOf(
        "OTP Debit Card",
        "PayPal Balance",
        "Revolut Standard",
        "Cash Wallet",
        "K&H Credit Card",
        "Wise Multi-Currency"
    )
    return List(count) { i ->
        val type = WalletType.entries[i % WalletType.entries.size]
        SpendingWallet(
            id = "wallet_$i",
            name = names[i % names.size],
            type = type,
            balance = Random.nextDouble(0.0, 1000000.0),
            currency = "HUF",
            color = generateRandomColor(),
            lastTransactions = mockMiniTransactions(Random.nextInt(0, 4))
        )
    }
}

val defaultWalletColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFFC107),
    Color(0xFF9C27B0),
    Color(0xFFE91E63),
    Color(0xFF00BCD4),
    Color(0xFFF44336),
    Color(0xFF795548)
) // Added more colors

fun generateRandomColor(): Color = defaultWalletColors.random()
