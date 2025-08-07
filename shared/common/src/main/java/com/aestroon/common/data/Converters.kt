package com.aestroon.common.data

import androidx.room.TypeConverter
import com.aestroon.common.data.entity.LoanEntity
import com.aestroon.common.data.entity.LoanEntryEntity
import com.aestroon.common.data.entity.LoanType
import com.aestroon.common.data.entity.RecurrenceType
import com.aestroon.common.data.entity.TransactionEntity
import com.aestroon.common.data.entity.TransactionType
import com.aestroon.common.data.serializable.Loan
import com.aestroon.common.data.serializable.LoanEntry
import com.aestroon.common.data.serializable.Transaction
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun toTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun fromRecurrenceType(value: String): RecurrenceType {
        return RecurrenceType.valueOf(value)
    }

    @TypeConverter
    fun toRecurrenceType(type: RecurrenceType): String {
        return type.name
    }

    @TypeConverter
    fun fromLoanType(value: String): LoanType {
        return LoanType.valueOf(value)
    }

    @TypeConverter
    fun toLoanType(type: LoanType): String {
        return type.name
    }
}
