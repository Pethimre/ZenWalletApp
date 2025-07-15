package com.aestroon.common.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aestroon.common.data.Converters
import java.util.UUID

enum class LoanType {
    LENT, BORROWED
}

@Entity(tableName = "loans")
@TypeConverters(Converters::class)
data class LoanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String?,
    val principal: Long,
    var remaining: Long,
    val color: String,
    val iconName: String?,
    val type: LoanType,
    val userId: String,
    val isSynced: Boolean = false
)