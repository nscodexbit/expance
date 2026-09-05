package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_templates",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("accountId"), Index("categoryId")]
)
data class RecurringTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val accountId: Long,
    val categoryId: Long,
    val amount: Double,
    val label: String,
    val frequency: Frequency,
    val nextDueDate: Long
)

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY
}
