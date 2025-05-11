package com.aestroon.home.mockProvider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.aestroon.components.theme.BlueChipColor
import com.aestroon.components.theme.DeepOrangeChipColor
import com.aestroon.components.theme.GreenChipColor
import com.aestroon.components.theme.OrangeChipColor
import com.aestroon.components.theme.Yellow
import com.aestroon.components.theme.DarkBlueChipColor
import com.aestroon.components.theme.GreyChipColor
import com.aestroon.components.theme.LightBlueChipColor

@Composable
fun WalletsScreen() {
    Text("Shared Screen")
}

@Composable
fun PortfolioScreen() {
    Text("Portfolio Screen")
}

@Composable
fun CalendarScreen() {
    Text("Calendar Screen")
}

@Composable
fun SettingsScreen() {
    Text("Settings Screen")
}

val BANK_CARDS = listOf(
    BankCard("Visa", "Visa card for spending", 22840.10, "EUR", Icons.Default.CreditCard , DarkBlueChipColor),
    BankCard("MasterCard", "Savings account", 758964.55, "EUR", Icons.Default.AccountBalance , GreenChipColor),
    BankCard("Amex", "Dollar account for work", 125500.75, "EUR", Icons.Default.Work , GreyChipColor)
)

val MOCK_TRANSACTIONS = listOf(
    List(8) {
        Transaction(
            "0$it",
            "Entertainment",
            "4:${30 + it} PM",
            "EUR",
            5.84 + it,
            Icons.Default.ShoppingCart,
            LightBlueChipColor
        )
    } + List(5) {
        Transaction(
            "1$it",
            "Bills",
            "5:${10 + it} PM",
            "EUR",
            -(20.00 + it),
            Icons.Default.Receipt,
            DarkBlueChipColor
        )
    },
    List(6) {
        Transaction(
            "2$it",
            "Delivery",
            "6:${10 + it} PM",
            "EUR",
            6.32 + it,
            Icons.Default.Fastfood,
            OrangeChipColor
        )
    } + List(4) {
        Transaction(
            "3$it",
            "Groceries",
            "7:${10 + it} PM",
            "EUR",
            -(30.00 + it),
            Icons.Default.LocalGroceryStore,
            DeepOrangeChipColor
        )
    },
    List(7) {
        Transaction(
            "4$it",
            "Freelance",
            "3:${10 + it} PM",
            "EUR",
            150.0 + it * 10,
            Icons.Default.Work,
            Yellow
        )
    } + List(6) {
        Transaction(
            "5$it",
            "Rent",
            "8:${10 + it} AM",
            "EUR",
            -(300.00 + it * 10),
            Icons.Default.Home,
            BlueChipColor
        )
    }
)

// --- Mock Data and Models ---
data class BankCard(
    val type: String,
    val accountDescription: String,
    val balance: Double,
    val currency: String,
    val icon: ImageVector,
    val iconBackgroundColor: Color,
)

data class Transaction(
    val id: String,
    val title: String,
    val time: String,
    val currency: String,
    val amount: Double,
    val icon: ImageVector,
    val iconBackgroundColor: Color,
)