package com.aestroon.calendar.mockProvider

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
import com.aestroon.common.theme.BlueChipColor
import com.aestroon.common.theme.DeepOrangeChipColor
import com.aestroon.common.theme.GreenChipColor
import com.aestroon.common.theme.OrangeChipColor
import com.aestroon.common.theme.Yellow
import com.aestroon.common.theme.DarkBlueChipColor
import com.aestroon.common.theme.GreyChipColor
import com.aestroon.common.theme.LightBlueChipColor

@Composable
fun WalletsScreen() {
    Text("Shared Screen")
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
            "This description is not very entertaining",
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
            "These cost more than they should huhu",
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
            "Carpe diem",
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
            "I will never financially recover from this",
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
            "This is freelance money. Don't touch",
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
            "Rent is due soon bruh",
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
    val description: String,
    val time: String,
    val currency: String,
    val amount: Double,
    val icon: ImageVector,
    val iconBackgroundColor: Color,
)