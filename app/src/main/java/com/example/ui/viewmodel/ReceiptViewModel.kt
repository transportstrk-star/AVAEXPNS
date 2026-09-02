package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ReceiptEntity
import com.example.data.model.VatBreakdown
import com.example.data.model.VatCalculator
import com.example.data.remote.UploadResult
import com.example.data.repository.ReceiptRepository
import com.example.data.util.BitmapUtils
import com.example.domain.OcrScanResult
import com.example.domain.SampleReceiptItem
import com.example.domain.SampleReceiptsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Processing : ScanUiState
    data class Success(val result: OcrScanResult, val imageUri: String? = null) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

sealed interface CloudUploadUiState {
    object Idle : CloudUploadUiState
    object Uploading : CloudUploadUiState
    data class Success(val message: String) : CloudUploadUiState
    data class Error(val message: String) : CloudUploadUiState
}

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ReceiptRepository(database.receiptDao())

    val allReceipts: StateFlow<List<ReceiptEntity>> = repository.allReceipts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter = _statusFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All Categories")
    val categoryFilter = _categoryFilter.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState = _scanState.asStateFlow()

    private val _uploadState = MutableStateFlow<CloudUploadUiState>(CloudUploadUiState.Idle)
    val uploadState = _uploadState.asStateFlow()

    private val _webhookUrl = MutableStateFlow(repository.getSavedWebhookUrl(application))
    val webhookUrl = _webhookUrl.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    // Filtered receipts for UI display
    val filteredReceipts: StateFlow<List<ReceiptEntity>> = combine(
        allReceipts,
        _searchQuery,
        _statusFilter,
        _categoryFilter
    ) { list, query, status, category ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.supplier.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.invoiceNumber.contains(query, ignoreCase = true) ||
                    item.trn.contains(query, ignoreCase = true)

            val matchesStatus = status == "All" || item.status.equals(status, ignoreCase = true)
            val matchesCategory = category == "All Categories" || item.category.equals(category, ignoreCase = true)

            matchesQuery && matchesStatus && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeSamplesIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun resetUploadState() {
        _uploadState.value = CloudUploadUiState.Idle
    }

    fun setWebhookUrl(url: String) {
        _webhookUrl.value = url
        repository.saveWebhookUrl(getApplication(), url)
    }

    fun processReceiptScan(bitmap: Bitmap?, rawText: String? = null, imageUri: String? = null) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Processing
            try {
                // Ensure bitmap is safely scaled & software RGB to prevent any hardware crash
                val safeBitmap = bitmap?.let { BitmapUtils.scaleBitmap(it, 1280) }
                val result = repository.runDualOcr(safeBitmap, rawText)
                _scanState.value = ScanUiState.Success(result, imageUri)
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(e.message ?: "Failed to process receipt with Dual OCR")
            }
        }
    }

    fun processReceiptUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Processing
            try {
                val bitmap = BitmapUtils.decodeSampledBitmapFromUri(context, uri, maxDimension = 1280)
                val result = repository.runDualOcr(bitmap, null)
                _scanState.value = ScanUiState.Success(result, uri.toString())
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(e.message ?: "Failed to decode and scan receipt image.")
            }
        }
    }

    fun processSampleReceipt(sample: SampleReceiptItem) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Processing
            try {
                val result = repository.runDualOcr(null, sample.rawOcrText)
                _scanState.value = ScanUiState.Success(
                    result = result.copy(
                        supplier = sample.supplier,
                        invoiceNumber = sample.entity.invoiceNumber,
                        date = sample.entity.date,
                        description = sample.entity.description,
                        totalAed = sample.entity.totalAed,
                        vatAed = sample.entity.vatAed,
                        netAed = sample.entity.netAed,
                        trn = sample.entity.trn,
                        category = sample.entity.category
                    ),
                    imageUri = sample.entity.imageUrl
                )
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(e.message ?: "Failed to process sample")
            }
        }
    }

    fun resetScanState() {
        _scanState.value = ScanUiState.Idle
    }

    fun saveReceipt(
        supplier: String,
        trn: String,
        invoiceNumber: String,
        date: String,
        description: String,
        totalAed: Double,
        vatAed: Double,
        netAed: Double,
        vatRate: Double,
        category: String,
        imageUrl: String,
        ocrPreview: String,
        status: String = "Verified",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entity = ReceiptEntity(
                timestamp = System.currentTimeMillis(),
                invoiceNumber = invoiceNumber.ifBlank { "INV-${(100000..999999).random()}" },
                date = date.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) },
                supplier = supplier.ifBlank { "UAE Merchant" },
                trn = trn,
                description = description.ifBlank { "General Purchase" },
                totalAed = totalAed,
                vatAed = vatAed,
                netAed = netAed,
                vatRate = vatRate,
                imageUrl = imageUrl.ifBlank { "https://drive.google.com/file/d/receipt_${System.currentTimeMillis()}/view" },
                ocrPreview = ocrPreview,
                status = status,
                category = category,
                ocrEngineUsed = "🤖 Dual OCR (ML Kit + Vision)",
                notes = notes
            )
            repository.saveReceipt(entity)
            _scanState.value = ScanUiState.Idle
            _snackbarMessage.value = "Receipt saved to Database & synced with Sheet layout!"
        }
    }

    fun updateReceipt(receipt: ReceiptEntity) {
        viewModelScope.launch {
            repository.updateReceipt(receipt)
            _snackbarMessage.value = "Receipt updated successfully!"
        }
    }

    fun deleteReceipt(receipt: ReceiptEntity) {
        viewModelScope.launch {
            repository.deleteReceipt(receipt)
            _snackbarMessage.value = "Receipt deleted from record."
        }
    }

    fun quickVerify(receipt: ReceiptEntity) {
        viewModelScope.launch {
            repository.updateReceipt(receipt.copy(status = "Verified"))
            _snackbarMessage.value = "Marked as Verified for UAE VAT Reclaim."
        }
    }

    /**
     * Uploads receipts to Google Sheets Cloud Webhook / Apps Script endpoint
     */
    fun uploadReceiptsToCloud(context: Context, specificReceipts: List<ReceiptEntity>? = null) {
        viewModelScope.launch {
            _uploadState.value = CloudUploadUiState.Uploading
            val targetList = specificReceipts ?: allReceipts.value
            if (targetList.isEmpty()) {
                _uploadState.value = CloudUploadUiState.Error("No receipts found to upload.")
                _snackbarMessage.value = "No receipts to upload."
                return@launch
            }

            val result = repository.uploadToCloud(context, targetList, _webhookUrl.value)
            when (result) {
                is UploadResult.Success -> {
                    _uploadState.value = CloudUploadUiState.Success(result.message)
                    _snackbarMessage.value = result.message
                }
                is UploadResult.Error -> {
                    _uploadState.value = CloudUploadUiState.Error(result.message)
                    _snackbarMessage.value = result.message
                }
            }
        }
    }

    /**
     * Imports Sheet CSV / TSV text data
     */
    fun importSheetData(csvText: String) {
        viewModelScope.launch {
            try {
                val count = repository.importSheetData(csvText)
                if (count > 0) {
                    _snackbarMessage.value = "Successfully imported $count receipt records into Database!"
                } else {
                    _snackbarMessage.value = "No valid records could be parsed from the provided text."
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    fun copySheetDataToClipboard(context: Context) {
        val list = allReceipts.value
        val tsvData = repository.generateSheetExportData(list, "\t")
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("UAE VAT Sheet Data (A-M)", tsvData)
        clipboard.setPrimaryClip(clip)
        _snackbarMessage.value = "Copied 13-Column Google Sheet format (A to M) to clipboard!"
    }

    fun shareSheetCsv(context: Context) {
        val list = allReceipts.value
        val csvData = repository.generateSheetExportData(list, ",")
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvData)
            putExtra(Intent.EXTRA_TITLE, "UAE_VAT_Expenses_Sheet.csv")
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export UAE VAT Sheet CSV")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareFinancialReport(context: Context) {
        val list = allReceipts.value
        val report = repository.generateFinancialReportSummary(list)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, report)
            putExtra(Intent.EXTRA_TITLE, "UAE_VAT_Financial_Audit_Report.txt")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share UAE Financial Report")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun resetToDefaultSamples() {
        viewModelScope.launch {
            repository.clearAll()
            val samples = SampleReceiptsData.sampleList.mapIndexed { index, sample ->
                sample.entity.copy(
                    id = 0,
                    timestamp = System.currentTimeMillis() - (index * 86400000L * 2)
                )
            }
            database.receiptDao().insertAll(samples)
            _snackbarMessage.value = "Reset to UAE Sample Invoices (ADNOC, Lulu, Carrefour, Sharaf DG, etc.)"
        }
    }
}
