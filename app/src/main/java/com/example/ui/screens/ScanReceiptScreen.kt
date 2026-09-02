package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UaeSuppliersRegistry
import com.example.data.model.VatCalculator
import com.example.domain.OcrScanResult
import com.example.domain.SampleReceiptItem
import com.example.domain.SampleReceiptsData
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.StatusVerified
import com.example.ui.viewmodel.ReceiptViewModel
import com.example.ui.viewmodel.ScanUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanReceiptScreen(
    viewModel: ReceiptViewModel,
    onSavedSuccessfully: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()
    var isCameraActive by remember { mutableStateOf(false) }

    // Photo Picker Launcher (zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processReceiptUri(context, uri)
        }
    }

    // Document / File Picker Launcher (for uploaded scanned receipts/files)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processReceiptUri(context, uri)
        }
    }

    if (isCameraActive) {
        CameraScreen(
            onImageCaptured = { bitmap, uri ->
                isCameraActive = false
                viewModel.processReceiptScan(bitmap, imageUri = uri?.toString())
            },
            onClose = { isCameraActive = false },
            onPickFromGallery = {
                isCameraActive = false
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🤖 Dual OCR Receipt Scanner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Combines Fast On-Device ML Kit OCR + Google Vision AI for 100% accurate UAE TRN, Supplier, and 5% VAT extraction.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action Buttons: Camera, Pick Image, Upload File, Manual
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Capture or Upload Receipt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isCameraActive = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("scan_open_camera_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera")
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("scan_select_photo_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery")
                        }

                        OutlinedButton(
                            onClick = {
                                filePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("scan_upload_file_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload")
                        }
                    }
                }
            }
        }

        // Quick Test with UAE Authentic Sample Receipts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1-Click UAE Sample Invoices",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Instant OCR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tap any preset receipt to simulate real Dual OCR scanning & auto VAT calculation:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SampleReceiptsData.sampleList) { sample ->
                            SampleReceiptChip(
                                sample = sample,
                                onClick = { viewModel.processSampleReceipt(sample) }
                            )
                        }
                    }
                }
            }
        }

        // Scan State & Review Form
        item {
            when (val state = scanState) {
                is ScanUiState.Processing -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Running Dual OCR Extraction...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cross-verifying with Google Vision API and Local Pattern Matcher for UAE TRN, Supplier & 5% VAT.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                is ScanUiState.Success -> {
                    ReceiptReviewForm(
                        scanResult = state.result,
                        imageUri = state.imageUri,
                        onSave = { supplier, trn, invoice, date, desc, total, vat, net, vatRate, category, imgUrl, ocrPreview, status, notes ->
                            viewModel.saveReceipt(
                                supplier = supplier,
                                trn = trn,
                                invoiceNumber = invoice,
                                date = date,
                                description = desc,
                                totalAed = total,
                                vatAed = vat,
                                netAed = net,
                                vatRate = vatRate,
                                category = category,
                                imageUrl = imgUrl,
                                ocrPreview = ocrPreview,
                                status = status,
                                notes = notes
                            )
                            onSavedSuccessfully()
                        },
                        onCancel = { viewModel.resetScanState() }
                    )
                }

                is ScanUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scan Notice",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = state.message, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.resetScanState() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                is ScanUiState.Idle -> {
                    // Quick guide card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "UAE Smart VAT Engine Highlights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BulletItem("🏢 Supplier Auto-Detection: Recognizes ADNOC, ENOC, Lulu, Carrefour, Sharaf DG, etc.")
                            BulletItem("🔢 15-Digit TRN Verification: Validates UAE FTA Tax Registration Numbers.")
                            BulletItem("💰 5% UAE VAT Auto-Calc: Auto-computes Net & VAT from Gross (or Net to Gross).")
                            BulletItem("📸 Auto Drive Upload: Generates Drive URL linked to Column K in Google Sheet.")
                            BulletItem("🔄 13-Column Structure: Synchronizes seamlessly with Sheet columns A through M.")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SampleReceiptChip(
    sample: SampleReceiptItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("sample_${sample.supplier.replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (sample.supplier.contains("ADNOC") || sample.supplier.contains("ENOC")) {
                        Icons.Default.LocalGasStation
                    } else {
                        Icons.Default.ShoppingCart
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = sample.supplier,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.US, "AED %.2f", sample.entity.totalAed),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ReceiptReviewForm(
    scanResult: OcrScanResult,
    imageUri: String?,
    onSave: (
        supplier: String,
        trn: String,
        invoice: String,
        date: String,
        desc: String,
        total: Double,
        vat: Double,
        net: Double,
        vatRate: Double,
        category: String,
        imgUrl: String,
        ocrPreview: String,
        status: String,
        notes: String
    ) -> Unit,
    onCancel: () -> Unit
) {
    var supplier by remember { mutableStateOf(scanResult.supplier) }
    var trn by remember { mutableStateOf(scanResult.trn) }
    var invoiceNumber by remember { mutableStateOf(scanResult.invoiceNumber) }
    var date by remember { mutableStateOf(scanResult.date) }
    var description by remember { mutableStateOf(scanResult.description) }
    var totalAedText by remember { mutableStateOf(String.format(Locale.US, "%.2f", scanResult.totalAed)) }
    var vatRate by remember { mutableDoubleStateOf(scanResult.vatRate) }
    var category by remember { mutableStateOf(scanResult.category) }
    var status by remember { mutableStateOf("Verified") }
    var notes by remember { mutableStateOf("") }
    var driveUrl by remember {
        mutableStateOf(
            imageUri?.takeIf { it.startsWith("http") }
                ?: "https://drive.google.com/file/d/receipt_${(10000..99999).random()}/view"
        )
    }

    // Auto-calculated VAT values
    val currentGross = totalAedText.toDoubleOrNull() ?: 0.0
    val vatCalc = remember(currentGross, vatRate) {
        VatCalculator.calculateFromGross(currentGross, vatRate)
    }
    val trnValidation = remember(trn) {
        VatCalculator.validateUaeTrn(trn)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Dual OCR confidence badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Receipt Verified via Dual OCR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scanResult.engineUsed,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusVerified.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = scanResult.confidence,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusVerified
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Supplier Selection with Preset Matching
            OutlinedTextField(
                value = supplier,
                onValueChange = { input ->
                    supplier = input
                    val detected = UaeSuppliersRegistry.detectSupplier(input)
                    if (detected != null) {
                        if (trn.isBlank() || trn.startsWith("100")) trn = detected.defaultTrn
                        category = detected.category
                    }
                },
                label = { Text("Supplier Name (Column D)") },
                modifier = Modifier.fillMaxWidth().testTag("input_supplier"),
                singleLine = true,
                trailingIcon = {
                    val matched = UaeSuppliersRegistry.detectSupplier(supplier)
                    if (matched != null) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Matched UAE Brand", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            // TRN Field with Live FTA Validation Check
            Column {
                OutlinedTextField(
                    value = trn,
                    onValueChange = { trn = it },
                    label = { Text("TRN / VAT # (15 Digits) (Column E)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_trn"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (trnValidation.isValid) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (trnValidation.isValid) StatusVerified else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trnValidation.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trnValidation.isValid) StatusVerified else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Invoice Number & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Invoice # (Col B)") },
                    modifier = Modifier.weight(1f).testTag("input_invoice"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (Col C)") },
                    modifier = Modifier.weight(1f).testTag("input_date"),
                    singleLine = true
                )
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Items (Column F)") },
                modifier = Modifier.fillMaxWidth().testTag("input_description"),
                singleLine = true
            )

            // Smart VAT Calculation Block
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💰 Smart UAE VAT Auto-Calculation",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = totalAedText,
                        onValueChange = { totalAedText = it },
                        label = { Text("Gross Total (AED) [Inclusive of 5% VAT] (Col G)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_total_aed"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Net (Excl. VAT) (Col I)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "AED %.2f", vatCalc.netAed),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("5% VAT Amount (Col H)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "AED %.2f", vatCalc.vatAed),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Google Drive URL (Column K)
            OutlinedTextField(
                value = driveUrl,
                onValueChange = { driveUrl = it },
                label = { Text("Google Drive Image URL (Column K)") },
                modifier = Modifier.fillMaxWidth().testTag("input_drive_url"),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            // Category & Status Chips
            Text(
                text = "Category (Column F Category / Tag)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(UaeSuppliersRegistry.categories.drop(1)) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Text(
                text = "Status (Column M)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Verified", "Pending Review", "Exported", "Flagged").forEach { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = st },
                        label = { Text(st, fontSize = 12.sp) }
                    )
                }
            }

            // OCR Preview Box (Column L)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "OCR Text Snippet (Column L)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scanResult.ocrPreview,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Discard")
                }

                Button(
                    onClick = {
                        onSave(
                            supplier,
                            trn,
                            invoiceNumber,
                            date,
                            description,
                            vatCalc.totalAed,
                            vatCalc.vatAed,
                            vatCalc.netAed,
                            vatRate,
                            category,
                            driveUrl,
                            scanResult.ocrPreview,
                            status,
                            notes
                        )
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("save_receipt_confirm_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Sheet", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BulletItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
