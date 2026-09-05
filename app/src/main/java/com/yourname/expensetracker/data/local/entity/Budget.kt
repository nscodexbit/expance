package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("categoryId")]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val categoryId: Long,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Long
)

enum class BudgetPeriod {
    WEEKLY,
    MONTHLY
}
