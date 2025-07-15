package com.aestroon.common.data.dao

import androidx.room.*
import com.aestroon.common.data.entity.LoanEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanEntryDao {
    @Query("SELECT * FROM loan_entries WHERE loanId = :loanId ORDER BY date DESC")
    fun getEntriesForLoan(loanId: String): Flow<List<LoanEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LoanEntryEntity)

    @Query("DELETE FROM loan_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: String)

    @Query("SELECT * FROM loan_entries WHERE isSynced = 0")
    fun getUnsyncedEntries(): Flow<List<LoanEntryEntity>>

    @Query("UPDATE loan_entries SET isSynced = 1 WHERE id = :entryId")
    suspend fun markEntryAsSynced(entryId: String)
}