package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.InvoiceDao
import com.yourname.expensetracker.data.local.dao.InvoiceWithItems
import com.yourname.expensetracker.data.local.entity.Invoice
import com.yourname.expensetracker.data.local.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao
) {
    fun getInvoices(profileId: Long): Flow<List<Invoice>> =
        invoiceDao.getInvoicesByProfile(profileId)

    fun getInvoicesWithItems(profileId: Long): Flow<List<InvoiceWithItems>> =
        invoiceDao.getInvoicesWithItems(profileId)

    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? =
        invoiceDao.getInvoiceWithItemsById(id)

    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = invoiceDao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceDao.insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.deleteInvoice(invoice)
    }

    fun getTotalSalesInRange(profileId: Long, start: Long, end: Long): Flow<Double?> =
        invoiceDao.getTotalSalesInRange(profileId, start, end)

    fun getTotalGstInRange(profileId: Long, start: Long, end: Long): Flow<Double?> =
        invoiceDao.getTotalGstInRange(profileId, start, end)
}
