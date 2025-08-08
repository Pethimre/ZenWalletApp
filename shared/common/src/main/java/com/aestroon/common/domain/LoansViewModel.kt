package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.LoanEntity
import com.aestroon.common.data.entity.LoanEntryEntity
import com.aestroon.common.data.entity.WalletEntity
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.LoanRepository
import com.aestroon.common.data.repository.UserRepository
import com.aestroon.common.data.repository.WalletRepository
import com.aestroon.common.utilities.DEFAULT_BASE_CURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class LoansViewModel(
    private val loanRepository: LoanRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val userId = authRepository.userIdFlow.filterNotNull()

    val loans: StateFlow<List<LoanEntity>> = userId.flatMapLatest { id ->
        loanRepository.getLoans(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallets: StateFlow<List<WalletEntity>> = userId.flatMapLatest { id ->
        walletRepository.getWalletsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _baseCurrency = MutableStateFlow(DEFAULT_BASE_CURRENCY)
    val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

    init {
        viewModelScope.launch {
            userId.firstOrNull()?.let { id ->
                loanRepository.fetchRemoteLoans(id)
                userRepository.getUserProfile(id).getOrNull()?.let { userProfile ->
                    _baseCurrency.value = userProfile.base_currency
                }
            }
        }
    }

    fun getLoanById(loanId: String): Flow<LoanEntity?> {
        return loanRepository.getLoanById(loanId)
    }

    fun getLoanWithEntries(loanId: String): Flow<Pair<LoanEntity?, List<LoanEntryEntity>>> {
        return loanRepository.getLoanById(loanId).combine(loanRepository.getLoanEntries(loanId)) { loan, entries ->
            Pair(loan, entries)
        }
    }

    fun addOrUpdateLoan(loan: LoanEntity) {
        viewModelScope.launch {
            loanRepository.addOrUpdateLoan(loan)
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            loanRepository.deleteLoan(loan)
        }
    }

    fun addLoanEntry(
        loan: LoanEntity,
        wallet: WalletEntity,
        amount: Long,
        date: Date,
        note: String?,
        isInterest: Boolean,
        createMainTransaction: Boolean
    ) {
        viewModelScope.launch {
            loanRepository.addLoanEntry(loan, wallet, amount, date, note, isInterest, createMainTransaction)
        }
    }

    fun deleteLoanEntry(entry: LoanEntryEntity) {
        viewModelScope.launch {
            loanRepository.deleteLoanEntry(entry)
        }
    }
}
