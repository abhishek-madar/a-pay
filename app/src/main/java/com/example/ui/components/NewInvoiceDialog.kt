package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInvoiceDialog(
    onCreateInvoice: (
        recipient: String,
        recipientEmail: String,
        subtotalAmount: Double,
        currency: AppCurrency,
        taxRegion: TaxRegion,
        dueDate: String,
        isRecurring: Boolean,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var recipient by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("50000") }
    var currency by remember { mutableStateOf(AppCurrency.INR) }
    var taxRegion by remember { mutableStateOf(TaxRegion.INDIA_GST_18) }
    var dueDate by remember { mutableStateOf("30 Oct 2026") }
    var isRecurring by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("Automated vendor net-15 payment terms.") }

    var currencyDropdown by remember { mutableStateOf(false) }
    var taxDropdown by remember { mutableStateOf(false) }

    val rawAmount = amountText.toDoubleOrNull() ?: 0.0
    val amountInInr = CurrencyFormatter.convertToInr(rawAmount, currency)
    val taxCalc = TaxCalculator.calculate(amountInInr, taxRegion)

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
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Generate Invoice",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Automated tax & recurring schedule",
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

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Vendor / Client Name") },
                    modifier = Modifier.fillMaxWidth().testTag("invoice_recipient_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Client Email") },
                    modifier = Modifier.fillMaxWidth().testTag("invoice_email_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Subtotal (${currency.code})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f).testTag("invoice_amount_input"),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = currencyDropdown,
                        onExpandedChange = { currencyDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${currency.flag} ${currency.code}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdown) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = currencyDropdown,
                            onDismissRequest = { currencyDropdown = false }
                        ) {
                            AppCurrency.entries.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text("${curr.flag} ${curr.code}") },
                                    onClick = {
                                        currency = curr
                                        currencyDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = taxDropdown,
                    onExpandedChange = { taxDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${taxRegion.regionName} (${taxRegion.defaultRatePercent}%)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tax Region") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taxDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = taxDropdown,
                        onDismissRequest = { taxDropdown = false }
                    ) {
                        TaxRegion.entries.forEach { region ->
                            DropdownMenuItem(
                                text = { Text("${region.regionName} - ${region.defaultRatePercent}%") },
                                onClick = {
                                    taxRegion = region
                                    taxDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Recurring schedule toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Set as Recurring Monthly Invoice",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Auto-dispatches reminders before due date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        modifier = Modifier.testTag("invoice_recurring_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Calculation Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatInr(amountInInr), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Automated Tax (${taxRegion.defaultRatePercent}%):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatInr(taxCalc.taxAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Invoice Total (INR):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(CurrencyFormatter.formatInr(taxCalc.totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val finalRec = recipient.ifBlank { "Client Business Partner" }
                        val finalEmail = email.ifBlank { "billing@clientcorp.com" }
                        onCreateInvoice(finalRec, finalEmail, rawAmount, currency, taxRegion, dueDate, isRecurring, notes)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("create_invoice_submit_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Issue Invoice & Send Reminder", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
