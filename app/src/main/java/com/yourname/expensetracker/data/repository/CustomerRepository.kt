package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.CustomerDao
import com.yourname.expensetracker.data.local.dao.CreditEntryDao
import com.yourname.expensetracker.data.local.entity.Customer
import com.yourname.expensetracker.data.local.entity.CreditEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val creditEntryDao: CreditEntryDao
) {
    fun getCustomersByProfile(profileId: Long): Flow<List<Customer>> =
        customerDao.getCustomersByProfile(profileId)

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    fun searchCustomers(profileId: Long, query: String): Flow<List<Customer>> =
        customerDao.searchCustomers(profileId, query)

    suspend fun insert(customer: Customer): Long = customerDao.insert(customer)

    suspend fun update(customer: Customer) = customerDao.update(customer)

    suspend fun delete(customer: Customer) = customerDao.delete(customer)

    fun getEntriesByCustomer(customerId: Long): Flow<List<CreditEntry>> =
        creditEntryDao.getEntriesByCustomer(customerId)

    suspend fun getEntryById(id: Long): CreditEntry? = creditEntryDao.getEntryById(id)

    fun getOutstandingBalance(customerId: Long): Flow<Double> =
        creditEntryDao.getOutstandingBalance(customerId)

    suspend fun insertCreditEntry(entry: CreditEntry): Long = creditEntryDao.insert(entry)

    suspend fun updateCreditEntry(entry: CreditEntry) = creditEntryDao.update(entry)

    suspend fun deleteCreditEntry(entry: CreditEntry) = creditEntryDao.delete(entry)
}
