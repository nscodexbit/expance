package com.yourname.expensetracker.ui.shop.billing

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.yourname.expensetracker.data.local.entity.Invoice
import com.yourname.expensetracker.data.local.entity.InvoiceItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object InvoicePdfHelper {

    data class ShopBranding(
        val name: String = "My Store",
        val phone: String = "",
        val address: String = "",
        val gstin: String = "",
        val header: String = "Tax Invoice / Bill of Supply",
        val footer: String = "Thank you for visiting! Please visit again."
    )

    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        items: List<InvoiceItem>,
        branding: ShopBranding
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            textAlign = Paint.Align.CENTER
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 50f

        // Shop Header
        canvas.drawText(branding.name, 297.5f, y, titlePaint)
        y += 18f

        if (branding.address.isNotBlank()) {
            canvas.drawText(branding.address, 297.5f, y, subPaint)
            y += 15f
        }

        val contactLine = listOfNotNull(
            if (branding.phone.isNotBlank()) "Ph: ${branding.phone}" else null,
            if (branding.gstin.isNotBlank()) "GSTIN: ${branding.gstin}" else null
        ).joinToString(" | ")

        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, 297.5f, y, subPaint)
            y += 15f
        }

        canvas.drawText(branding.header, 297.5f, y, subPaint)
        y += 20f

        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Invoice Meta
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(invoice.date))
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", 40f, y, boldPaint)
        canvas.drawText("Date: $dateStr", 350f, y, textPaint)
        y += 16f

        if (!invoice.customerName.isNullOrBlank()) {
            canvas.drawText("Customer: ${invoice.customerName}", 40f, y, textPaint)
            if (!invoice.customerPhone.isNullOrBlank()) {
                canvas.drawText("Phone: ${invoice.customerPhone}", 350f, y, textPaint)
            }
            y += 16f
        }

        canvas.drawText("Payment Mode: ${invoice.paymentMode.uppercase()}", 40f, y, textPaint)
        y += 20f

        // Table Header
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 14f

        canvas.drawText("Item Name", 45f, y, boldPaint)
        canvas.drawText("Qty", 320f, y, boldPaint)
        canvas.drawText("Rate", 400f, y, boldPaint)
        canvas.drawText("Amount", 480f, y, boldPaint)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // Items
        for (item in items) {
            canvas.drawText(item.itemName.take(30), 45f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%.1f", item.quantity), 320f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", item.unitPrice), 400f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", item.total), 480f, y, textPaint)
            y += 16f
            if (y > 750f) break
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Totals
        canvas.drawText("Subtotal:", 350f, y, textPaint)
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", invoice.subtotal), 480f, y, textPaint)
        y += 16f

        if (invoice.discountAmount > 0) {
            canvas.drawText("Discount:", 350f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "-₹%.2f", invoice.discountAmount), 480f, y, textPaint)
            y += 16f
        }

        if (invoice.taxAmount > 0) {
            canvas.drawText("GST/Tax:", 350f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", invoice.taxAmount), 480f, y, textPaint)
            y += 16f
        }

        canvas.drawLine(350f, y, 555f, y, linePaint)
        y += 18f

        boldPaint.textSize = 14f
        canvas.drawText("Grand Total:", 350f, y, boldPaint)
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", invoice.grandTotal), 480f, y, boldPaint)
        y += 35f

        // Footer
        if (branding.footer.isNotBlank()) {
            canvas.drawText(branding.footer, 297.5f, y, subPaint)
        }

        document.finishPage(page)

        val invoicesDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val outputFile = File(invoicesDir, "${invoice.invoiceNumber}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
    }

    fun generateThermalReceipt(
        invoice: Invoice,
        items: List<InvoiceItem>,
        branding: ShopBranding
    ): String {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(invoice.date))
        val sb = StringBuilder()
        val sep = "--------------------------------\n"

        sb.append("        ${branding.name.take(20)}\n")
        if (branding.address.isNotBlank()) sb.append("  ${branding.address.take(28)}\n")
        if (branding.phone.isNotBlank()) sb.append("     Phone: ${branding.phone}\n")
        if (branding.gstin.isNotBlank()) sb.append("     GSTIN: ${branding.gstin}\n")
        sb.append(sep)
        sb.append("Inv: ${invoice.invoiceNumber}  $dateStr\n")
        if (!invoice.customerName.isNullOrBlank()) {
            sb.append("Cust: ${invoice.customerName.take(24)}\n")
        }
        sb.append("Mode: ${invoice.paymentMode}\n")
        sb.append(sep)
        sb.append(String.format("%-16s %4s %9s\n", "Item", "Qty", "Amount"))
        sb.append(sep)

        for (item in items) {
            sb.append(String.format("%-16s %4.0f %9.2f\n", item.itemName.take(16), item.quantity, item.total))
        }

        sb.append(sep)
        sb.append(String.format("%-20s %10.2f\n", "Subtotal:", invoice.subtotal))
        if (invoice.discountAmount > 0) {
            sb.append(String.format("%-20s -%9.2f\n", "Discount:", invoice.discountAmount))
        }
        if (invoice.taxAmount > 0) {
            sb.append(String.format("%-20s %10.2f\n", "GST/Tax:", invoice.taxAmount))
        }
        sb.append(sep)
        sb.append(String.format("%-20s %10.2f\n", "GRAND TOTAL (INR):", invoice.grandTotal))
        sb.append(sep)
        sb.append("   ${branding.footer.take(26)}\n\n\n")

        return sb.toString()
    }
}
