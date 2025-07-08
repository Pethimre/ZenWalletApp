package com.aestroon.common.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aestroon.common.data.entity.PlannedPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedPaymentDao {
    @Query("SELECT * FROM planned_payments WHERE userId = :userId ORDER BY dueDate ASC")
    fun getPlannedPaymentsForUser(userId: String): Flow<List<PlannedPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedPayment(payment: PlannedPaymentEntity)

    @Update
    suspend fun updatePlannedPayment(payment: PlannedPaymentEntity)

    @Query("DELETE FROM planned_payments WHERE id = :id")
    suspend fun deletePlannedPaymentById(id: String)

    @Query("SELECT * FROM planned_payments WHERE isSynced = 0")
    fun getUnsyncedPayments(): Flow<List<PlannedPaymentEntity>>

    @Query("UPDATE planned_payments SET isSynced = 1 WHERE id = :id")
    suspend fun markPaymentAsSynced(id: String)
}