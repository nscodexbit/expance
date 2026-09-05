package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_entries",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class CreditEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: CreditType,
    val amount: Double,
    val date: Long,
    val note: String? = null,
    val reminderSentAt: Long? = null
)

enum class CreditType {
    CREDIT_GIVEN,
    PAYMENT_RECEIVED
}
