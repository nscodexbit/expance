package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "split_participants",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class SplitParticipant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val participantName: String,
    val amountOwed: Double,
    val isSettled: Boolean = false
)
