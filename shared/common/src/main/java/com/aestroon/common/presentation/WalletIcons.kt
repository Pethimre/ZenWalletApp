package com.aestroon.common.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

data class WalletIconResource(val name: String, val icon: ImageVector)

object WalletIconProvider {
    val AccountBalance = WalletIconResource("AccountBalance", Icons.Default.AccountBalance)
    val CreditCard = WalletIconResource("CreditCard", Icons.Default.CreditCard)
    val Savings = WalletIconResource("Savings", Icons.Default.Savings)
    val Business = WalletIconResource("Business", Icons.Default.Business)
    val Payments = WalletIconResource("Payments", Icons.Default.Payments)
    val Home = WalletIconResource("Home", Icons.Default.Home)

    val allIcons = listOf(AccountBalance, CreditCard, Savings, Business, Payments, Home)

    fun getIconByName(name: String?): ImageVector {
        return allIcons.find { it.name == name }?.icon ?: AccountBalance.icon
    }
}
