package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
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
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val invoiceNumber: String,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val subtotal: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val gstRate: Double = 0.0, // 0%, 5%, 12%, 18%, 28%
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paymentMode: String = "CASH", // CASH, UPI, CREDIT
    val notes: String? = null,
    val createdBy: String? = "Owner",
    val date: Long = System.currentTimeMillis()
)
