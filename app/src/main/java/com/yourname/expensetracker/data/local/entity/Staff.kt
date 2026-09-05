package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "staff",
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
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val pinHash: String = "",
    val permissionLevel: PermissionLevel = PermissionLevel.STAFF,
    val phone: String? = null,
    val monthlySalary: Double = 0.0,
    val advanceBalance: Double = 0.0
)

enum class PermissionLevel {
    OWNER,
    STAFF
}
