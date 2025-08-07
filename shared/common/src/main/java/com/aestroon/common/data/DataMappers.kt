package com.aestroon.common.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.aestroon.common.data.entity.*
import com.aestroon.common.data.serializable.*
import java.time.Instant

fun LoanEntity.toNetworkModel() = Loan(
    id = this.id,
    name = this.name,
    description = this.description,
    principal = this.principal,
    remaining = this.remaining,
    iconName = this.iconName,
    color = this.color,
    type = this.type.name,
    userId = this.userId
)

fun LoanEntryEntity.toNetworkModel() = LoanEntry(
    id = this.id,
    loanId = this.loanId,
    transactionId = this.transactionId,
    userId = this.userId,
    walletId = this.walletId,
    amount = this.amount,
    date = this.date,
    note = this.note,
    isInterest = this.isInterest
)

fun TransactionEntity.toNetworkModel() = Transaction(
    id = this.id,
    amount = this.amount,
    currency = this.currency,
    name = this.name,
    description = this.description,
    date = this.date,
    userId = this.userId,
    walletId = this.walletId,
    categoryId = this.categoryId,
    transactionType = this.transactionType.name,
    toWalletId = this.toWalletId
)

fun Loan.toEntity(sanitizeColor: (String?) -> String) = LoanEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    principal = this.principal,
    remaining = this.remaining,
    color = sanitizeColor(this.color),
    iconName = this.iconName,
    type = try { LoanType.valueOf(this.type) } catch (e: Exception) { LoanType.LENT },
    userId = this.userId,
    isSynced = true
)

fun LoanEntry.toEntity() = LoanEntryEntity(
    id = this.id,
    loanId = this.loanId,
    transactionId = this.transactionId,
    userId = this.userId,
    walletId = this.walletId,
    amount = this.amount,
    date = this.date,
    note = this.note,
    isInterest = this.isInterest,
    isSynced = true
)

fun Transaction.toEntity() = TransactionEntity(
    id = this.id,
    amount = this.amount,
    currency = this.currency,
    name = this.name,
    description = this.description,
    date = this.date,
    userId = this.userId,
    walletId = this.walletId,
    categoryId = this.categoryId,
    transactionType = try {
        TransactionType.valueOf(this.transactionType)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    },
    toWalletId = this.toWalletId,
    isSynced = true
)

fun PortfolioEntity.toNetworkModel() = Portfolio(
    id = this.id,
    name = this.name,
    type = this.type,
    user_id = this.userId,
    balance = this.balance,
    color = this.color,
    description = this.description,
    icon_url = this.iconName
)

@RequiresApi(Build.VERSION_CODES.O)
fun PortfolioInstrumentEntity.toNetworkModel(userId: String) = PortfolioInstrument(
    id = this.id,
    portfolio_id = this.portfolioId,
    user_id = userId,
    symbol = this.symbol,
    name = this.name,
    quantity = this.quantity,
    average_buy_price = this.averageBuyPrice,
    currency = this.currency,
    maturity_date = this.maturityDate?.let { Instant.ofEpochMilli(it).toString() },
    coupon_rate = this.couponRate,
    lookup_price = this.lookupPrice,
    last_updated_price = this.lastUpdatedPrice,
    last_updated_date = this.lastUpdatedDate?.let { Instant.ofEpochMilli(it).toString() }
)

fun Portfolio.toEntity() = PortfolioEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    balance = this.balance,
    color = this.color,
    iconName = this.icon_url,
    type = this.type,
    userId = this.user_id,
    isSynced = true
)

@RequiresApi(Build.VERSION_CODES.O)
fun PortfolioInstrument.toEntity() = PortfolioInstrumentEntity(
    id = this.id,
    portfolioId = this.portfolio_id,
    symbol = this.symbol,
    name = this.name,
    quantity = this.quantity,
    averageBuyPrice = this.average_buy_price,
    currency = this.currency,
    maturityDate = this.maturity_date?.let { Instant.parse(it).toEpochMilli() },
    couponRate = this.coupon_rate,
    isSynced = true,
    lookupPrice = this.lookup_price,
    lastUpdatedPrice = this.last_updated_price,
    lastUpdatedDate = this.last_updated_date?.let { Instant.parse(it).toEpochMilli() }
)
