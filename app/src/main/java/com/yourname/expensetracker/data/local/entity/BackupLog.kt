package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_logs")
data class BackupLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: BackupType,
    val status: BackupStatus
)

enum class BackupType {
    LOCAL,
    DRIVE
}

enum class BackupStatus {
    SUCCESS,
    FAILED
}
