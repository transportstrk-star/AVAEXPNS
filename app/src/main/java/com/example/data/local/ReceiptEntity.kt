package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(), // Column A
    val invoiceNumber: String = "",                  // Column B
    val date: String = "",                           // Column C (e.g. 2026-09-01)
    val supplier: String = "",                       // Column D
    val trn: String = "",                            // Column E (15-digit UAE TRN)
    val description: String = "",                    // Column F
    val totalAed: Double = 0.0,                      // Column G
    val vatAed: Double = 0.0,                        // Column H
    val netAed: Double = 0.0,                        // Column I
    val vatRate: Double = 5.0,                       // Column J (Default 5%)
    val imageUrl: String = "",                       // Column K
    val ocrPreview: String = "",                     // Column L
    val status: String = "Verified",                 // Column M ("Verified", "Pending Review", "Exported", "Flagged")
    val category: String = "General",
    val ocrEngineUsed: String = "Dual OCR (Vision + Local)",
    val notes: String = ""
) {
    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            return sdf.format(Date(timestamp))
        }

    val formattedTotal: String
        get() = String.format(Locale.US, "AED %.2f", totalAed)

    val formattedVat: String
        get() = String.format(Locale.US, "AED %.2f", vatAed)

    val formattedNet: String
        get() = String.format(Locale.US, "AED %.2f", netAed)

    val formattedVatRate: String
        get() = "${vatRate.toInt()}%"
}
