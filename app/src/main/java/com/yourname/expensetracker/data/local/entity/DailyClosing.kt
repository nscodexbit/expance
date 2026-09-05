package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_closings",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class DailyClosing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val date: Long = System.currentTimeMillis(),
    val openingCash: Double = 0.0,
    val cashSales: Double = 0.0,
    val cashExpenses: Double = 0.0,
    val expectedCash: Double = 0.0,
    val actualCashCounted: Double = 0.0,
    val difference: Double = 0.0,
    val notes: String? = null
)
