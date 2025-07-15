package com.aestroon.common.data.dao

import androidx.room.*
import com.aestroon.common.data.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY name ASC")
    fun getLoansForUser(userId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    fun getLoanById(loanId: String): Flow<LoanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: String)

    @Query("SELECT * FROM loans WHERE isSynced = 0")
    fun getUnsyncedLoans(): Flow<List<LoanEntity>>

    @Query("UPDATE loans SET isSynced = 1 WHERE id = :loanId")
    suspend fun markLoanAsSynced(loanId: String)
}