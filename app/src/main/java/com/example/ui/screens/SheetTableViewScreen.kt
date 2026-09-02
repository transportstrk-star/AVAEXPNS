package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReceiptEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusExported
import com.example.ui.theme.StatusFlagged
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusVerified
import com.example.ui.viewmodel.CloudUploadUiState
import com.example.ui.viewmodel.ReceiptViewModel
import java.util.Locale

@Composable
fun SheetTableViewScreen(
    viewModel: ReceiptViewModel,
    onEditReceipt: (ReceiptEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val receipts by viewModel.filteredReceipts.collectAsState()
    val allReceipts by viewModel.allReceipts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val webhookUrl by viewModel.webhookUrl.collectAsState()
    val context = LocalContext.current

    var viewMode by remember { mutableStateOf("Table") } // "Table" or "Cards"
    var showWebhookDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title and Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📊 Google Sheet Structure",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Columns A through M synchronized in real-time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { viewMode = "Table" },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (viewMode == "Table") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = "Spreadsheet Table View",
                        tint = if (viewMode == "Table") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { viewMode = "Cards" },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (viewMode == "Cards") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.ViewAgenda,
                        contentDescription = "Card List View",
                        tint = if (viewMode == "Cards") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sheet_search_input"),
            placeholder = { Text("Search by Supplier, TRN, Invoice #...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(listOf("All", "Verified", "Pending Review", "Exported", "Flagged")) { st ->
                FilterChip(
                    selected = statusFilter == st,
                    onClick = { viewModel.setStatusFilter(st) },
                    label = { Text(st, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cloud Upload & Data Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.uploadReceiptsToCloud(context) },
                modifier = Modifier
                    .weight(1.3f)
                    .height(42.dp)
                    .testTag("upload_to_sheet_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                enabled = uploadState !is CloudUploadUiState.Uploading
            ) {
                if (uploadState is CloudUploadUiState.Uploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Uploading...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload to Sheet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = { viewModel.copySheetDataToClipboard(context) },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("copy_sheet_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy TSV", fontSize = 12.sp)
            }

            IconButton(
                onClick = { showWebhookDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Upload Settings", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(
                onClick = { showImportDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = "Import Data", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Upload Status Banner if active
        if (uploadState is CloudUploadUiState.Success || uploadState is CloudUploadUiState.Error) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (uploadState is CloudUploadUiState.Success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                border = BorderStroke(1.dp, if (uploadState is CloudUploadUiState.Success) EmeraldPrimary else Color.Red)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uploadState is CloudUploadUiState.Success)
                            (uploadState as CloudUploadUiState.Success).message
                        else
                            (uploadState as CloudUploadUiState.Error).message,
                        fontSize = 11.sp,
                        color = if (uploadState is CloudUploadUiState.Success) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.resetUploadState() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content: Table Grid or Card List
        if (receipts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching receipts found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            if (viewMode == "Table") {
                SpreadsheetGridView(
                    receipts = receipts,
                    onEditReceipt = onEditReceipt,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(receipts, key = { it.id }) { receipt ->
                        ReceiptCardItem(
                            receipt = receipt,
                            onEdit = { onEditReceipt(receipt) },
                            onQuickVerify = { viewModel.quickVerify(receipt) },
                            onDelete = { viewModel.deleteReceipt(receipt) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Webhook Configuration Dialog
    if (showWebhookDialog) {
        var tempUrl by remember { mutableStateOf(webhookUrl) }
        AlertDialog(
            onDismissRequest = { showWebhookDialog = false },
            title = { Text("Google Sheets Webhook Setup") },
            text = {
                Column {
                    Text(
                        text = "Enter your Google Apps Script Web App URL or Webhook endpoint to sync columns A-M automatically:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Leave blank for instant simulated cloud preparation.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setWebhookUrl(tempUrl)
                        showWebhookDialog = false
                    }
                ) {
                    Text("Save URL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebhookDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import CSV/TSV Data Dialog
    if (showImportDialog) {
        var importText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Sheet Data (A-M)") },
            text = {
                Column {
                    Text(
                        text = "Paste CSV or TSV lines from Google Sheets (Columns A through M) to upload/import into database:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("Timestamp\tInvoice #\tDate\tSupplier\tTRN\t...") },
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importSheetData(importText)
                        showImportDialog = false
                    },
                    enabled = importText.isNotBlank()
                ) {
                    Text("Import Records")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * 13-Column Spreadsheet Table View (Columns A to M) with Freeze Header & Horizontal Scroll
 */
@Composable
private fun SpreadsheetGridView(
    receipts: List<ReceiptEntity>,
    onEditReceipt: (ReceiptEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 13 Column Spec (A to M)
    val columns = listOf(
        "A: Timestamp" to 130.dp,
        "B: Invoice #" to 120.dp,
        "C: Date" to 100.dp,
        "D: Supplier" to 150.dp,
        "E: TRN/VAT #" to 140.dp,
        "F: Description" to 160.dp,
        "G: Total (AED)" to 110.dp,
        "H: VAT (AED)" to 100.dp,
        "I: Net (AED)" to 100.dp,
        "J: VAT %" to 70.dp,
        "K: Image URL" to 130.dp,
        "L: OCR Preview" to 180.dp,
        "M: Status" to 110.dp
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.horizontalScroll(scrollState)) {
            Column {
                // Table Header Row (A to M)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    columns.forEach { (title, colWidth) ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .width(colWidth)
                                .padding(horizontal = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Table Rows
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(receipts, key = { it.id }) { receipt ->
                        val rowBg = if (receipt.status == "Exported") {
                            Color(0xFFF1F8E9)
                        } else if (receipt.status == "Flagged") {
                            Color(0xFFFFEBEE)
                        } else {
                            Color.Transparent
                        }

                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .clickable { onEditReceipt(receipt) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Col A: Timestamp
                            Text(
                                text = receipt.formattedTimestamp,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(130.dp).padding(horizontal = 6.dp)
                            )
                            // Col B: Invoice #
                            Text(
                                text = receipt.invoiceNumber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(120.dp).padding(horizontal = 6.dp)
                            )
                            // Col C: Date
                            Text(
                                text = receipt.date,
                                fontSize = 11.sp,
                                modifier = Modifier.width(100.dp).padding(horizontal = 6.dp)
                            )
                            // Col D: Supplier
                            Text(
                                text = receipt.supplier,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(150.dp).padding(horizontal = 6.dp)
                            )
                            // Col E: TRN/VAT #
                            Text(
                                text = receipt.trn.ifBlank { "—" },
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (receipt.trn.isNotBlank()) EmeraldPrimary else Color.Gray,
                                modifier = Modifier.width(140.dp).padding(horizontal = 6.dp)
                            )
                            // Col F: Description
                            Text(
                                text = receipt.description,
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.width(160.dp).padding(horizontal = 6.dp)
                            )
                            // Col G: Total (AED)
                            Text(
                                text = String.format(Locale.US, "%.2f", receipt.totalAed),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(110.dp).padding(horizontal = 6.dp)
                            )
                            // Col H: VAT (AED)
                            Text(
                                text = String.format(Locale.US, "%.2f", receipt.vatAed),
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(100.dp).padding(horizontal = 6.dp)
                            )
                            // Col I: Net (AED)
                            Text(
                                text = String.format(Locale.US, "%.2f", receipt.netAed),
                                fontSize = 11.sp,
                                modifier = Modifier.width(100.dp).padding(horizontal = 6.dp)
                            )
                            // Col J: VAT %
                            Text(
                                text = "${receipt.vatRate.toInt()}%",
                                fontSize = 11.sp,
                                modifier = Modifier.width(70.dp).padding(horizontal = 6.dp)
                            )
                            // Col K: Image URL
                            Text(
                                text = "drive.google.com/...",
                                fontSize = 11.sp,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.width(130.dp).padding(horizontal = 6.dp)
                            )
                            // Col L: OCR Preview
                            Text(
                                text = receipt.ocrPreview.replace("\n", " ").take(30),
                                fontSize = 10.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(180.dp).padding(horizontal = 6.dp)
                            )
                            // Col M: Status
                            StatusBadge(
                                status = receipt.status,
                                modifier = Modifier.width(110.dp).padding(horizontal = 4.dp)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptCardItem(
    receipt: ReceiptEntity,
    onEdit: () -> Unit,
    onQuickVerify: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receipt.supplier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Inv: ${receipt.invoiceNumber} • ${receipt.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = receipt.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (receipt.trn.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "TRN: ${receipt.trn}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = receipt.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total (Inc 5% VAT)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "AED %.2f", receipt.totalAed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "VAT (5%)", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                        Text(
                            text = String.format(Locale.US, "AED %.2f", receipt.vatAed),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Net Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(Locale.US, "AED %.2f", receipt.netAed),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (receipt.status != "Verified" && receipt.status != "Exported") {
                    TextButton(onClick = onQuickVerify) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify", color = EmeraldPrimary, fontSize = 12.sp)
                    }
                }

                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        "Verified" -> StatusVerified.copy(alpha = 0.15f) to StatusVerified
        "Exported" -> StatusExported.copy(alpha = 0.15f) to StatusExported
        "Flagged" -> StatusFlagged.copy(alpha = 0.15f) to StatusFlagged
        else -> StatusPending.copy(alpha = 0.15f) to StatusPending
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = status,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
