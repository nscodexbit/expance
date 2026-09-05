package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE profileId = :profileId ORDER BY name ASC")
    fun getAccountsByProfile(profileId: Long): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE profileId = :profileId ORDER BY name ASC")
    suspend fun getAccountsListByProfile(profileId: Long): List<Account>

    @Query("SELECT COUNT(*) FROM accounts WHERE profileId = :profileId")
    suspend fun getAccountCount(profileId: Long): Int

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>): List<Long>

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
