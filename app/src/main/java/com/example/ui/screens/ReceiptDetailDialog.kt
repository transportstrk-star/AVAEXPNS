package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ReceiptEntity
import com.example.data.model.UaeSuppliersRegistry
import com.example.data.model.VatCalculator
import com.example.ui.theme.StatusFlagged
import com.example.ui.theme.StatusVerified
import java.util.Locale

@Composable
fun ReceiptDetailDialog(
    receipt: ReceiptEntity,
    onDismiss: () -> Unit,
    onSave: (ReceiptEntity) -> Unit,
    onDelete: (ReceiptEntity) -> Unit
) {
    var supplier by remember { mutableStateOf(receipt.supplier) }
    var trn by remember { mutableStateOf(receipt.trn) }
    var invoiceNumber by remember { mutableStateOf(receipt.invoiceNumber) }
    var date by remember { mutableStateOf(receipt.date) }
    var description by remember { mutableStateOf(receipt.description) }
    var totalAedText by remember { mutableStateOf(String.format(Locale.US, "%.2f", receipt.totalAed)) }
    var status by remember { mutableStateOf(receipt.status) }
    var category by remember { mutableStateOf(receipt.category) }
    var imageUrl by remember { mutableStateOf(receipt.imageUrl) }
    var notes by remember { mutableStateOf(receipt.notes) }

    val currentGross = totalAedText.toDoubleOrNull() ?: 0.0
    val vatCalc = remember(currentGross) {
        VatCalculator.calculateFromGross(currentGross, receipt.vatRate)
    }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Receipt Record",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Row ID #${receipt.id} • ${receipt.formattedTimestamp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Supplier & TRN
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name (Col D)") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_supplier"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = trn,
                    onValueChange = { trn = it },
                    label = { Text("TRN / VAT # (15 Digits) (Col E)") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_trn"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Invoice # & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Invoice # (Col B)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (Col C)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Col F)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Amounts
                OutlinedTextField(
                    value = totalAedText,
                    onValueChange = { totalAedText = it },
                    label = { Text("Total (AED) [Incl. 5% VAT] (Col G)") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_total"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Auto Calc Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Net (Col I):", fontSize = 11.sp)
                            Text(String.format(Locale.US, "AED %.2f", vatCalc.netAed), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("5% VAT (Col H):", fontSize = 11.sp)
                            Text(String.format(Locale.US, "AED %.2f", vatCalc.vatAed), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Image URL
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image / Drive URL (Col K)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("URL", imageUrl))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", modifier = Modifier.size(16.dp))
                        }
                    }
                )

                // Status Selector
                Text("Status (Col M)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Verified", "Pending Review", "Exported", "Flagged").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Audit Memo") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDelete(receipt)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusFlagged),
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val updated = receipt.copy(
                                supplier = supplier,
                                trn = trn,
                                invoiceNumber = invoiceNumber,
                                date = date,
                                description = description,
                                totalAed = vatCalc.totalAed,
                                vatAed = vatCalc.vatAed,
                                netAed = vatCalc.netAed,
                                status = status,
                                category = category,
                                imageUrl = imageUrl,
                                notes = notes
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.4f).height(46.dp).testTag("save_edit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
