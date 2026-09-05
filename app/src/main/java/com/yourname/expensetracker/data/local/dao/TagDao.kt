package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE profileId = :profileId ORDER BY name ASC")
    fun getTagsByProfile(profileId: Long): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Long): Tag?

    @Insert
    suspend fun insert(tag: Tag): Long

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)
}
