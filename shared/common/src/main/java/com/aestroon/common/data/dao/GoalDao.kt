package com.aestroon.common.data.dao

import androidx.room.*
import com.aestroon.common.data.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY targetDate ASC")
    fun getGoalsForUser(userId: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: String)

    @Query("SELECT * FROM goals WHERE isSynced = 0")
    fun getUnsyncedGoals(): Flow<List<GoalEntity>>

    @Query("UPDATE goals SET isSynced = 1 WHERE id = :goalId")
    suspend fun markAsSynced(goalId: String)
}