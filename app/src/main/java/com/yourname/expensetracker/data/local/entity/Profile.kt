package com.yourname.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: ProfileType,
    val currency: String = "USD",
    val language: String = "en",
    val createdAt: Long = System.currentTimeMillis()
)

enum class ProfileType {
    PERSONAL,
    SHOP
}
