package com.aestroon.common.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.aestroon.common.data.dao.GoalDao
import com.aestroon.common.data.entity.GoalEntity
import com.aestroon.common.data.serializable.Goal
import com.aestroon.common.data.toEntity
import com.aestroon.common.data.toNetworkModel
import com.aestroon.common.utilities.network.ConnectivityObserver
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface GoalRepository {
    fun getGoals(userId: String): Flow<List<GoalEntity>>
    suspend fun addOrUpdateGoal(goal: GoalEntity): Result<Unit>
    suspend fun deleteGoal(goal: GoalEntity): Result<Unit>
    suspend fun syncGoals(userId: String): Result<Unit>
}

class GoalRepositoryImpl(
    private val goalDao: GoalDao,
    private val postgrest: Postgrest,
    private val connectivityObserver: ConnectivityObserver
) : GoalRepository {

    override fun getGoals(userId: String): Flow<List<GoalEntity>> = goalDao.getGoalsForUser(userId)

    override suspend fun addOrUpdateGoal(goal: GoalEntity): Result<Unit> = runCatching {
        goalDao.insertGoal(goal.copy(isSynced = false))
        syncGoals(goal.userId)
    }

    override suspend fun deleteGoal(goal: GoalEntity): Result<Unit> = runCatching {
        goalDao.deleteGoalById(goal.id)
        if (connectivityObserver.observe().first() == ConnectivityObserver.Status.Available) {
            postgrest.from("Goals").delete { filter { eq("id", goal.id) } }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncGoals(userId: String): Result<Unit> = runCatching {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) return@runCatching

        goalDao.getUnsyncedGoals().first().takeIf { it.isNotEmpty() }?.let {
            postgrest.from("Goals").upsert(it.map(GoalEntity::toNetworkModel))
            it.forEach { goal -> goalDao.markAsSynced(goal.id) }
        }

        val remoteGoals = postgrest.from("Goals").select {
            filter { eq("user_id", userId) }
        }.decodeList<Goal>()
        remoteGoals.forEach { goalDao.insertGoal(it.toEntity()) }
    }
}