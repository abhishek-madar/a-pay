package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.model.CurrencyFormatter
import com.example.model.ExpenseCategory
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCalculatorDialog(
    onApplyBudgetLimits: (Map<ExpenseCategory, Double>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var projectedRevenueText by remember { mutableStateOf("1000000") } // 10 Lakhs INR
    var targetProfitMarginText by remember { mutableStateOf("25") } // 25%

    val revenue = projectedRevenueText.toDoubleOrNull() ?: 1000000.0
    val margin = targetProfitMarginText.toDoubleOrNull() ?: 25.0
    val totalSpendablePool = revenue * (1.0 - (margin / 100.0).coerceIn(0.0, 0.90))

    // Recommended allocations based on spendable pool
    val softwareLimit = totalSpendablePool * 0.16
    val payrollLimit = totalSpendablePool * 0.44
    val vendorLimit = totalSpendablePool * 0.15
    val taxReserveLimit = totalSpendablePool * 0.15
    val marketingLimit = totalSpendablePool * 0.08
    val gatewayFeesLimit = totalSpendablePool * 0.02

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Budget Calculator",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Automated financial planning limits",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = projectedRevenueText,
                        onValueChange = { projectedRevenueText = it },
                        label = { Text("Monthly Revenue (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f).testTag("calculator_revenue_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetProfitMarginText,
                        onValueChange = { targetProfitMarginText = it },
                        label = { Text("Margin %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f).testTag("calculator_margin_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Pool Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Spendable Expense Cap:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.formatInr(totalSpendablePool),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Target Retained Profit:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.formatInr(revenue - totalSpendablePool),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Recommended Category Allocations:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                CategoryAllocRow("Payroll & Contractors (44%)", payrollLimit)
                CategoryAllocRow("Software & Cloud Infrastructure (16%)", softwareLimit)
                CategoryAllocRow("Vendor Invoices & Supplies (15%)", vendorLimit)
                CategoryAllocRow("Regional Tax Reserves (15%)", taxReserveLimit)
                CategoryAllocRow("Marketing & Campaigns (8%)", marketingLimit)
                CategoryAllocRow("Gateway Processing Fees (2%)", gatewayFeesLimit)

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val limitsMap = mapOf(
                            ExpenseCategory.SALARIES_CONTRACTORS to payrollLimit,
                            ExpenseCategory.SOFTWARE_CLOUD to softwareLimit,
                            ExpenseCategory.VENDOR_PAYMENT to vendorLimit,
                            ExpenseCategory.TAX_DISBURSEMENT to taxReserveLimit,
                            ExpenseCategory.MARKETING_ADS to marketingLimit,
                            ExpenseCategory.PAYMENT_FEES to gatewayFeesLimit
                        )
                        onApplyBudgetLimits(limitsMap)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("apply_budget_limits_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Limits to Active Budgets", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryAllocRow(categoryName: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(categoryName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(CurrencyFormatter.formatInr(amount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
