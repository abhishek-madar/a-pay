package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPaymentDialog(
    onProcessPayment: (
        title: String,
        customerName: String,
        customerEmail: String,
        amountInSource: Double,
        sourceCurrency: AppCurrency,
        gateway: PaymentGateway,
        category: ExpenseCategory,
        taxRegion: TaxRegion
    ) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("1500") }
    var selectedCurrency by remember { mutableStateOf(AppCurrency.USD) }
    var selectedGateway by remember { mutableStateOf(PaymentGateway.STRIPE) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.SOFTWARE_CLOUD) }
    var selectedTaxRegion by remember { mutableStateOf(TaxRegion.INDIA_GST_18) }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var gatewayDropdownExpanded by remember { mutableStateOf(false) }
    var taxDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val rawAmount = amountText.toDoubleOrNull() ?: 0.0
    val amountInInr = CurrencyFormatter.convertToInr(rawAmount, selectedCurrency)
    val taxCalc = TaxCalculator.calculate(amountInInr, selectedTaxRegion)

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Process Global Payment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hardware AES-256 encrypted payload",
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

                // Payment Title / Description
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Payment Description / Memo") },
                    placeholder = { Text("e.g. Enterprise Cloud Retainer Q4") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Name & Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Client / Payer") },
                        placeholder = { Text("e.g. Acme Corp") },
                        modifier = Modifier.weight(1f).testTag("customer_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customerEmail,
                        onValueChange = { customerEmail = it },
                        label = { Text("Email") },
                        placeholder = { Text("billing@acme.com") },
                        modifier = Modifier.weight(1f).testTag("customer_email_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount & Currency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (${selectedCurrency.code})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f).testTag("payment_amount_input"),
                        singleLine = true
                    )

                    // Currency Dropdown
                    ExposedDropdownMenuBox(
                        expanded = currencyDropdownExpanded,
                        onExpandedChange = { currencyDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${selectedCurrency.flag} ${selectedCurrency.code}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false }
                        ) {
                            AppCurrency.entries.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text("${curr.flag} ${curr.code} - ${curr.symbol}") },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Rupee Equivalent Box if not INR
                if (selectedCurrency != AppCurrency.INR) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Converted base amount in Rupees:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatInr(amountInInr),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gateway & Category Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gateway Dropdown
                    ExposedDropdownMenuBox(
                        expanded = gatewayDropdownExpanded,
                        onExpandedChange = { gatewayDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedGateway.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gateway") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gatewayDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = gatewayDropdownExpanded,
                            onDismissRequest = { gatewayDropdownExpanded = false }
                        ) {
                            PaymentGateway.entries.forEach { gw ->
                                DropdownMenuItem(
                                    text = { Text(gw.title) },
                                    onClick = {
                                        selectedGateway = gw
                                        gatewayDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            ExpenseCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Regional Tax Selector
                ExposedDropdownMenuBox(
                    expanded = taxDropdownExpanded,
                    onExpandedChange = { taxDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedTaxRegion.regionName} (${selectedTaxRegion.defaultRatePercent}%)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Automated Regional Tax") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taxDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = taxDropdownExpanded,
                        onDismissRequest = { taxDropdownExpanded = false }
                    ) {
                        TaxRegion.entries.forEach { region ->
                            DropdownMenuItem(
                                text = { Text("${region.regionName} - ${region.defaultRatePercent}%") },
                                onClick = {
                                    selectedTaxRegion = region
                                    taxDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tax Calculation Summary Card
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${selectedTaxRegion.taxLabel} (${selectedTaxRegion.defaultRatePercent}%):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.formatInr(taxCalc.taxAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = taxCalc.breakdownText,
                            fontSize = 10.sp,
                            color = CyanAccent
                        )
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Charge (INR):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = CurrencyFormatter.formatInr(taxCalc.totalAmount),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        val finalTitle = title.ifBlank { "Global ${selectedGateway.title} Payment" }
                        val finalCust = customerName.ifBlank { "International Client" }
                        val finalEmail = customerEmail.ifBlank { "client@example.com" }
                        onProcessPayment(
                            finalTitle,
                            finalCust,
                            finalEmail,
                            rawAmount,
                            selectedCurrency,
                            selectedGateway,
                            selectedCategory,
                            selectedTaxRegion
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("process_payment_submit_button")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Authorize & Process Securely",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
