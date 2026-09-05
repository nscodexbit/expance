package com.yourname.expensetracker.data.local.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.yourname.expensetracker.data.local.dao.InvoiceWithItems
import com.yourname.expensetracker.data.local.entity.CreditEntry
import com.yourname.expensetracker.data.local.entity.Customer
import com.yourname.expensetracker.data.local.entity.Transaction
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportTransactionsCsv(
        context: Context,
        transactions: List<Transaction>,
        accountNameMap: Map<Long, String> = emptyMap(),
        categoryNameMap: Map<Long, String> = emptyMap()
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "transactions_${System.currentTimeMillis()}.csv")

        FileWriter(file).use { writer ->
            writer.append("ID,Date,Type,Amount,Account,Category,Notes\n")
            for (t in transactions) {
                val dateStr = dateFormat.format(Date(t.date))
                val account = accountNameMap[t.accountId] ?: "Default"
                val category = categoryNameMap[t.categoryId] ?: "Uncategorized"
                val safeNotes = (t.note ?: "").replace(",", " ").replace("\n", " ")
                writer.append("${t.id},$dateStr,${t.type},${t.amount},\"$account\",\"$category\",\"$safeNotes\"\n")
            }
        }
        return file
    }

    fun exportInvoicesCsv(
        context: Context,
        invoices: List<InvoiceWithItems>
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "invoices_${System.currentTimeMillis()}.csv")

        FileWriter(file).use { writer ->
            writer.append("InvoiceNumber,Date,Customer,Subtotal,Discount,Tax,GrandTotal,PaymentMode,ItemsCount\n")
            for (inv in invoices) {
                val dateStr = dateFormat.format(Date(inv.invoice.date))
                val cust = (inv.invoice.customerName ?: "").replace(",", " ")
                writer.append("${inv.invoice.invoiceNumber},$dateStr,\"$cust\",${inv.invoice.subtotal},${inv.invoice.discountAmount},${inv.invoice.taxAmount},${inv.invoice.grandTotal},${inv.invoice.paymentMode},${inv.items.size}\n")
            }
        }
        return file
    }

    fun exportKhataLedgerCsv(
        context: Context,
        customer: Customer,
        entries: List<CreditEntry>
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "khata_${customer.name.replace(" ", "_")}_${System.currentTimeMillis()}.csv")

        FileWriter(file).use { writer ->
            writer.append("Date,Type,Amount,Note\n")
            for (e in entries) {
                val dateStr = dateFormat.format(Date(e.date))
                val safeNote = (e.note ?: "").replace(",", " ")
                writer.append("$dateStr,${e.type},${e.amount},\"$safeNote\"\n")
            }
        }
        return file
    }

    fun shareCsv(context: Context, file: File, title: String = "Export CSV") {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
