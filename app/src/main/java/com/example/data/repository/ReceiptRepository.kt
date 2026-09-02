package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.ReceiptDao
import com.example.data.local.ReceiptEntity
import com.example.data.remote.SheetUploadService
import com.example.data.remote.UploadResult
import com.example.domain.DualOcrEngine
import com.example.domain.OcrScanResult
import com.example.domain.SampleReceiptsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptRepository(
    private val dao: ReceiptDao,
    private val dualOcrEngine: DualOcrEngine = DualOcrEngine(),
    private val sheetUploadService: SheetUploadService = SheetUploadService()
) {
    val allReceipts: Flow<List<ReceiptEntity>> = dao.getAllReceipts()

    suspend fun initializeSamplesIfEmpty() {
        val existing = dao.getAllReceipts().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val samples = SampleReceiptsData.sampleList.mapIndexed { index, sample ->
                sample.entity.copy(
                    timestamp = System.currentTimeMillis() - (index * 86400000L * 2)
                )
            }
            dao.insertAll(samples)
        }
    }

    suspend fun saveReceipt(receipt: ReceiptEntity): Long {
        return dao.insertReceipt(receipt)
    }

    suspend fun updateReceipt(receipt: ReceiptEntity) {
        dao.updateReceipt(receipt)
    }

    suspend fun deleteReceipt(receipt: ReceiptEntity) {
        dao.deleteReceipt(receipt)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    fun search(query: String): Flow<List<ReceiptEntity>> {
        return if (query.isBlank()) dao.getAllReceipts() else dao.searchReceipts(query.trim())
    }

    suspend fun runDualOcr(bitmap: Bitmap?, rawText: String? = null): OcrScanResult {
        return dualOcrEngine.processReceipt(bitmap, rawText)
    }

    suspend fun uploadToCloud(context: Context, receipts: List<ReceiptEntity>, webhookUrl: String? = null): UploadResult {
        val result = sheetUploadService.uploadReceiptsToCloud(context, receipts, webhookUrl)
        if (result is UploadResult.Success) {
            // Mark uploaded receipts as "Exported"
            receipts.forEach { receipt ->
                dao.updateReceipt(receipt.copy(status = "Exported"))
            }
        }
        return result
    }

    fun getSavedWebhookUrl(context: Context): String = sheetUploadService.getSavedWebhookUrl(context)
    fun saveWebhookUrl(context: Context, url: String) = sheetUploadService.saveWebhookUrl(context, url)

    /**
     * Parses imported CSV or TSV data and saves new records into database
     */
    suspend fun importSheetData(content: String): Int {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return 0

        var importedCount = 0
        val dataRows = lines.drop(1) // Skip header

        for (line in dataRows) {
            val delimiter = if (line.contains("\t")) "\t" else ","
            val parts = line.split(delimiter).map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 7) {
                try {
                    val invoice = parts.getOrNull(1).orEmpty().ifBlank { "INV-${(100000..999999).random()}" }
                    val date = parts.getOrNull(2).orEmpty().ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
                    val supplier = parts.getOrNull(3).orEmpty().ifBlank { "Imported Supplier" }
                    val trn = parts.getOrNull(4).orEmpty()
                    val desc = parts.getOrNull(5).orEmpty().ifBlank { "Imported Expense" }
                    val total = parts.getOrNull(6)?.toDoubleOrNull() ?: 0.0
                    val vat = parts.getOrNull(7)?.toDoubleOrNull() ?: (total - (total / 1.05))
                    val net = parts.getOrNull(8)?.toDoubleOrNull() ?: (total - vat)
                    val imgUrl = parts.getOrNull(10).orEmpty()
                    val ocrPrev = parts.getOrNull(11).orEmpty()
                    val status = parts.getOrNull(12).orEmpty().ifBlank { "Verified" }

                    if (total > 0.0 || supplier.isNotBlank()) {
                        dao.insertReceipt(
                            ReceiptEntity(
                                timestamp = System.currentTimeMillis(),
                                invoiceNumber = invoice,
                                date = date,
                                supplier = supplier,
                                trn = trn,
                                description = desc,
                                totalAed = total,
                                vatAed = vat,
                                netAed = net,
                                vatRate = 5.0,
                                imageUrl = imgUrl,
                                ocrPreview = ocrPrev,
                                status = status,
                                category = "General",
                                ocrEngineUsed = "📥 CSV / Sheet Import",
                                notes = "Imported record"
                            )
                        )
                        importedCount++
                    }
                } catch (e: Exception) {
                    // Skip malformed row
                }
            }
        }
        return importedCount
    }

    /**
     * Generates exact Google Sheets TSV / CSV Export format matching columns A to M:
     * A: Timestamp | B: Invoice # | C: Date | D: Supplier | E: TRN/VAT # | F: Description |
     * G: Total (AED) | H: VAT (AED) | I: Net (AED) | J: VAT % | K: Image URL | L: OCR Preview | M: Status
     */
    fun generateSheetExportData(receipts: List<ReceiptEntity>, delimiter: String = "\t"): String {
        val headers = listOf(
            "Timestamp",
            "Invoice #",
            "Date",
            "Supplier",
            "TRN/VAT #",
            "Description",
            "Total (AED)",
            "VAT (AED)",
            "Net (AED)",
            "VAT %",
            "Image URL",
            "OCR Preview",
            "Status"
        ).joinToString(delimiter)

        val rows = receipts.map { r ->
            listOf(
                r.formattedTimestamp,
                escapeField(r.invoiceNumber, delimiter),
                escapeField(r.date, delimiter),
                escapeField(r.supplier, delimiter),
                escapeField(r.trn, delimiter),
                escapeField(r.description, delimiter),
                String.format(Locale.US, "%.2f", r.totalAed),
                String.format(Locale.US, "%.2f", r.vatAed),
                String.format(Locale.US, "%.2f", r.netAed),
                "${r.vatRate.toInt()}%",
                escapeField(r.imageUrl, delimiter),
                escapeField(r.ocrPreview.replace("\n", " "), delimiter),
                r.status
            ).joinToString(delimiter)
        }

        return (listOf(headers) + rows).joinToString("\n")
    }

    /**
     * Generates a formatted UAE FTA Financial Summary Report for audit & tax filing
     */
    fun generateFinancialReportSummary(receipts: List<ReceiptEntity>): String {
        val totalGross = receipts.sumOf { it.totalAed }
        val totalVat = receipts.sumOf { it.vatAed }
        val totalNet = receipts.sumOf { it.netAed }
        val count = receipts.size
        val verifiedCount = receipts.count { it.status == "Verified" || it.status == "Exported" }

        val supplierBreakdown = receipts.groupBy { it.supplier }
            .mapValues { (_, list) -> list.sumOf { it.totalAed } }
            .entries.sortedByDescending { it.value }

        val categoryBreakdown = receipts.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.totalAed } }
            .entries.sortedByDescending { it.value }

        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

        return buildString {
            appendLine("==================================================")
            appendLine("     UAE VAT & EXPENSE FINANCIAL REPORT           ")
            appendLine("     Generated: $now                              ")
            appendLine("==================================================")
            appendLine("SUMMARY METRICS:")
            appendLine(" • Total Invoices Processed: $count")
            appendLine(" • Verified / Compliant:    $verifiedCount (${if (count > 0) (verifiedCount * 100 / count) else 0}%)")
            appendLine(String.format(Locale.US, " • Total Expenses (Gross):   AED %,.2f", totalGross))
            appendLine(String.format(Locale.US, " • Total Net Expenses:       AED %,.2f", totalNet))
            appendLine(String.format(Locale.US, " • Total Reclaimable VAT:    AED %,.2f (5%% UAE VAT)", totalVat))
            appendLine("--------------------------------------------------")
            appendLine("CATEGORY SPENDING BREAKDOWN:")
            categoryBreakdown.forEach { (cat, amount) ->
                val pct = if (totalGross > 0) (amount / totalGross * 100) else 0.0
                appendLine(String.format(Locale.US, " • %-22s: AED %,9.2f (%4.1f%%)", cat, amount, pct))
            }
            appendLine("--------------------------------------------------")
            appendLine("TOP UAE SUPPLIERS:")
            supplierBreakdown.take(6).forEach { (sup, amount) ->
                appendLine(String.format(Locale.US, " • %-22s: AED %,9.2f", sup, amount))
            }
            appendLine("==================================================")
            appendLine("Federal Tax Authority (FTA) Compliance Summary: Ready for Sheet / Drive Export.")
        }
    }

    private fun escapeField(field: String, delimiter: String): String {
        val clean = field.replace("\"", "\"\"")
        return if (clean.contains(delimiter) || clean.contains("\"") || clean.contains("\n")) {
            "\"$clean\""
        } else {
            clean
        }
    }
}
