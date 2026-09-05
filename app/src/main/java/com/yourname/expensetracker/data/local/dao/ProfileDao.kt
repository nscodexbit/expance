package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    suspend fun getAllProfilesList(): List<Profile>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): Profile?

    @Query("SELECT * FROM profiles WHERE type = :type")
    fun getProfilesByType(type: String): Flow<List<Profile>>

    @Insert
    suspend fun insert(profile: Profile): Long

    @Update
    suspend fun update(profile: Profile)

    @Delete
    suspend fun delete(profile: Profile)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
}
