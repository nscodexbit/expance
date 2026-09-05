package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_items",
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
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val barcode: String? = null,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStockAlert: Double = 5.0,
    val unit: String = "pcs",
    val updatedAt: Long = System.currentTimeMillis()
)
