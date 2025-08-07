package com.aestroon.common.data

import com.aestroon.common.data.entity.*
import com.aestroon.common.data.serializable.*

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
