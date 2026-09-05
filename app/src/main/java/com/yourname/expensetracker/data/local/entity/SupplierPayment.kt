package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supplier_payments",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplierId")]
)
data class SupplierPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val amount: Double,
    val date: Long,
    val note: String? = null
)
