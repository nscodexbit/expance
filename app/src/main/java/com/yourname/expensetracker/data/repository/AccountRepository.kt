package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.AccountDao
import com.yourname.expensetracker.data.local.entity.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    fun getAccountsByProfile(profileId: Long): Flow<List<Account>> =
        accountDao.getAccountsByProfile(profileId)

    suspend fun getAccountsListByProfile(profileId: Long): List<Account> =
        accountDao.getAccountsListByProfile(profileId)

    suspend fun getAccountCount(profileId: Long): Int = accountDao.getAccountCount(profileId)

    suspend fun getAccountById(id: Long): Account? = accountDao.getAccountById(id)

    suspend fun insert(account: Account): Long = accountDao.insert(account)

    suspend fun insertAll(accounts: List<Account>): List<Long> = accountDao.insertAll(accounts)

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(account: Account) = accountDao.delete(account)

    suspend fun deleteById(id: Long) = accountDao.deleteById(id)
}
