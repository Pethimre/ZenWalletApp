package com.aestroon.common.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aestroon.common.data.entity.GoalEntity
import com.aestroon.common.data.repository.AuthRepository
import com.aestroon.common.data.repository.GoalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.userIdFlow.filterNotNull()

    val goals: StateFlow<List<GoalEntity>> = userId.flatMapLatest { id ->
        goalRepository.getGoals(id)
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _goalReachedEvent = MutableSharedFlow<GoalEntity>()
    val goalReachedEvent = _goalReachedEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            userId.firstOrNull()?.let { goalRepository.syncGoals(it) }
        }
    }

    fun addOrUpdateGoal(goalFromUi: GoalEntity) = viewModelScope.launch {
        val currentUserId = userId.first()

        val goalToSave = if (goalFromUi.id.isBlank()) {
            goalFromUi.copy(
                id = UUID.randomUUID().toString(),
                userId = currentUserId
            )
        } else {
            goalFromUi.copy(userId = currentUserId)
        }

        goalRepository.addOrUpdateGoal(goalToSave)
    }

    fun deleteGoal(goal: GoalEntity) = viewModelScope.launch {
        goalRepository.deleteGoal(goal)
    }

    fun addFundsToGoal(goal: GoalEntity, amountToAdd: Double) = viewModelScope.launch {
        val amountToAddAsLong = (amountToAdd * 100).toLong()
        val newCurrentAmount = goal.currentAmount + amountToAddAsLong
        val updatedGoal = goal.copy(
            currentAmount = newCurrentAmount
        )
        addOrUpdateGoal(updatedGoal)

        if (goal.currentAmount < goal.targetAmount && newCurrentAmount >= goal.targetAmount) {
            _goalReachedEvent.emit(updatedGoal)
        }
    }
}
