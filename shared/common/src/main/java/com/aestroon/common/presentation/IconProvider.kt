package com.aestroon.common.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.ui.graphics.vector.ImageVector

data class AppIcon(val name: String, val icon: ImageVector)

object IconProvider {
    // Wallet Icons
    private val walletAccountBalance = AppIcon("AccountBalance", Icons.Default.AccountBalance)
    private val walletCreditCard = AppIcon("CreditCard", Icons.Default.CreditCard)
    private val walletSavings = AppIcon("Savings", Icons.Default.Savings)
    private val walletBusiness = AppIcon("Business", Icons.Default.Business)
    private val walletPayments = AppIcon("Payments", Icons.Default.Payments)
    private val walletHome = AppIcon("Home", Icons.Default.Home)
    val walletIcons = listOf(walletAccountBalance, walletCreditCard, walletSavings, walletBusiness, walletPayments, walletHome)

    // Category Icons
    private val categoryShopping = AppIcon("Shopping", Icons.Default.ShoppingCart)
    private val categoryFood = AppIcon("Food", Icons.Default.Fastfood)
    private val categoryTransport = AppIcon("Transport", Icons.Default.DirectionsCar)
    private val categoryBills = AppIcon("Bills", Icons.Default.ReceiptLong)
    private val categoryEntertainment = AppIcon("Entertainment", Icons.Default.Theaters)
    private val categoryHealth = AppIcon("Health", Icons.Default.LocalHospital)
    private val categoryOther = AppIcon("Other", Icons.Default.MoreHoriz)
    val categoryIcons = listOf(categoryShopping, categoryFood, categoryTransport, categoryBills, categoryEntertainment, categoryHealth, categoryOther)

    fun getWalletIcon(name: String?): ImageVector {
        return walletIcons.find { it.name == name }?.icon ?: walletAccountBalance.icon
    }

    fun getCategoryIcon(name: String?): ImageVector {
        return categoryIcons.find { it.name == name }?.icon ?: categoryOther.icon
    }
}
