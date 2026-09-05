package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
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
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RecurringTemplate::class,
            parentColumns = ["id"],
            childColumns = ["recurringTemplateId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("profileId"),
        Index("accountId"),
        Index("categoryId"),
        Index("recurringTemplateId"),
        Index("date"),
        Index("type")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val accountId: Long,
    val categoryId: Long? = null,
    val type: TransactionType,
    val amount: Double,
    val date: Long,
    val note: String? = null,
    val receiptPhotoUri: String? = null,
    val recurringTemplateId: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    EXPENSE,
    INCOME,
    CASH_IN,
    CASH_OUT
}
