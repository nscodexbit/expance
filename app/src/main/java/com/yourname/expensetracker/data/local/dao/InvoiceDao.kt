package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Invoice
import com.yourname.expensetracker.data.local.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
)

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices WHERE profileId = :profileId ORDER BY date DESC")
    fun getInvoicesByProfile(profileId: Long): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE profileId = :profileId ORDER BY date DESC")
    fun getInvoicesWithItems(profileId: Long): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("SELECT COUNT(*) FROM invoices WHERE profileId = :profileId")
    suspend fun countInvoices(profileId: Long): Int

    @Query("SELECT SUM(grandTotal) FROM invoices WHERE profileId = :profileId AND date BETWEEN :start AND :end")
    fun getTotalSalesInRange(profileId: Long, start: Long, end: Long): Flow<Double?>

    @Query("SELECT SUM(taxAmount) FROM invoices WHERE profileId = :profileId AND date BETWEEN :start AND :end")
    fun getTotalGstInRange(profileId: Long, start: Long, end: Long): Flow<Double?>
}
