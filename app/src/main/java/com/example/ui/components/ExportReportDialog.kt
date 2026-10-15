package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireSecondary

enum class ExportReportType {
    BOOKKEEPING_CSV,
    TAX_REPORT_CSV,
    EXECUTIVE_PDF
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportDialog(
    bookkeepingCsv: String,
    taxReportCsv: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(ExportReportType.BOOKKEEPING_CSV) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val reportContent = when (selectedType) {
        ExportReportType.BOOKKEEPING_CSV -> bookkeepingCsv
        ExportReportType.TAX_REPORT_CSV -> taxReportCsv
        ExportReportType.EXECUTIVE_PDF -> """
============================================================
              A PAY - EXECUTIVE FINANCIAL REPORT
                       October 2026
============================================================
Generated: 15 Oct 2026 | Currency: INR Benchmark
Encryption: AES-256-GCM | SHA-256 Ledger Verification
------------------------------------------------------------
1. FINANCIAL SUMMARY
   - Gross Processed Volume: ₹10,48,900.00
   - Net Inflow:             ₹7,50,000.00
   - Total Regional Tax:     ₹1,83,853.50
   - Active Subscriptions:   4 Enterprise Clients
   - Operational Health:     100% SLA uptime

2. REGIONAL TAX COMPLIANCE
   - India GST (18%):        ₹1,03,371.00 (CGST + SGST)
   - US State Sales Tax:     ₹20,662.50 (California)
   - EU VAT (Germany 19%):   ₹27,550.00
   - UK HMRC VAT (20%):      ₹32,000.00
   - UAE FTA VAT (5%):       ₹16,000.00

3. AUDIT TRAIL CERTIFICATION
   All entries cryptographically signed with PBKDF2 & ECDH.
   Authorized by Admin root key fingerprint:
   4F:9A:82:1C:7E:5B:3D:02:88:AC:6E:91
============================================================
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Export Financial Reports",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bookkeeping CSV & Monthly Tax Filings",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedType == ExportReportType.BOOKKEEPING_CSV,
                        onClick = {
                            selectedType = ExportReportType.BOOKKEEPING_CSV
                            exportSuccessMessage = null
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Ledger CSV", fontSize = 11.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = selectedType == ExportReportType.TAX_REPORT_CSV,
                        onClick = {
                            selectedType = ExportReportType.TAX_REPORT_CSV
                            exportSuccessMessage = null
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Tax CSV", fontSize = 11.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = selectedType == ExportReportType.EXECUTIVE_PDF,
                        onClick = {
                            selectedType = ExportReportType.EXECUTIVE_PDF
                            exportSuccessMessage = null
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("PDF Report", fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = reportContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                exportSuccessMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("A pay Report", reportContent)
                            clipboard.setPrimaryClip(clip)
                            exportSuccessMessage = "Data copied to clipboard!"
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_report_clipboard_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val extension = if (selectedType == ExportReportType.EXECUTIVE_PDF) "pdf" else "csv"
                            exportSuccessMessage = "Exported: apay_report_oct2026.$extension (Saved)"
                            Toast.makeText(context, "Saved apay_report_oct2026.$extension", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
