package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE profileId = :profileId AND kind = :kind ORDER BY name ASC")
    fun getCategoriesByProfileAndKind(profileId: Long, kind: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE profileId = :profileId ORDER BY name ASC")
    fun getCategoriesByProfile(profileId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM categories WHERE profileId = :profileId")
    suspend fun getCategoryCount(profileId: Long): Int
}
