package com.yourname.expensetracker.data.local

import androidx.room.TypeConverter
import com.yourname.expensetracker.data.local.entity.*

class Converters {
    @TypeConverter
    fun fromProfileType(value: ProfileType): String = value.name

    @TypeConverter
    fun toProfileType(value: String): ProfileType = ProfileType.valueOf(value)

    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromTransactionKind(value: TransactionKind): String = value.name

    @TypeConverter
    fun toTransactionKind(value: String): TransactionKind = TransactionKind.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromFrequency(value: Frequency): String = value.name

    @TypeConverter
    fun toFrequency(value: String): Frequency = Frequency.valueOf(value)

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod): String = value.name

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod = BudgetPeriod.valueOf(value)

    @TypeConverter
    fun fromCreditType(value: CreditType): String = value.name

    @TypeConverter
    fun toCreditType(value: String): CreditType = CreditType.valueOf(value)

    @TypeConverter
    fun fromPermissionLevel(value: PermissionLevel): String = value.name

    @TypeConverter
    fun toPermissionLevel(value: String): PermissionLevel = PermissionLevel.valueOf(value)

    @TypeConverter
    fun fromBackupType(value: BackupType): String = value.name

    @TypeConverter
    fun toBackupType(value: String): BackupType = BackupType.valueOf(value)

    @TypeConverter
    fun fromBackupStatus(value: BackupStatus): String = value.name

    @TypeConverter
    fun toBackupStatus(value: String): BackupStatus = BackupStatus.valueOf(value)
}
