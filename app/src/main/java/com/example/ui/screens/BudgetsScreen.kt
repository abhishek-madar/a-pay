package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.AppCurrency
import com.example.model.BudgetCategory
import com.example.model.CurrencyFormatter
import com.example.model.ExpenseCategory
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@Composable
fun BudgetsScreen(
    budgets: List<BudgetCategory>,
    displayCurrency: AppCurrency,
    onOpenCalculator: () -> Unit,
    onUpdateBudgetLimit: (ExpenseCategory, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLimitInr = budgets.sumOf { it.monthlyLimitInInr }
    val totalSpentInr = budgets.sumOf { it.currentSpentInInr }
    val overallPercentage = if (totalLimitInr > 0) (totalSpentInr / totalLimitInr).toFloat().coerceIn(0f, 1f) else 0f

    var editingCategory by remember { mutableStateOf<BudgetCategory?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Budget Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Budget Limits",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Automated alerts when spending reaches 80% & 100%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenCalculator,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.testTag("budget_screen_calculator_button")
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calculator", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("Total Budget Spent:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.format(totalSpentInr, displayCurrency),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Monthly Cap:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyFormatter.format(totalLimitInr, displayCurrency),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { overallPercentage },
                        color = if (overallPercentage >= 0.85f) CoralError else EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(overallPercentage * 100).toInt()}% of budget utilized",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (overallPercentage >= 0.85f) CoralError else EmeraldPrimary
                        )
                        Text(
                            text = "Remaining: ${CurrencyFormatter.format(totalLimitInr - totalSpentInr, displayCurrency)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Department Spending Caps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Category Budget Items
        items(budgets) { budgetItem ->
            BudgetCategoryCard(
                budgetItem = budgetItem,
                displayCurrency = displayCurrency,
                onEdit = { editingCategory = budgetItem }
            )
        }
    }

    // Edit Limit Dialog
    editingCategory?.let { itemToEdit ->
        EditLimitDialog(
            budgetItem = itemToEdit,
            onDismiss = { editingCategory = null },
            onSave = { newLimit ->
                onUpdateBudgetLimit(itemToEdit.category, newLimit)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun BudgetCategoryCard(
    budgetItem: BudgetCategory,
    displayCurrency: AppCurrency,
    onEdit: () -> Unit
) {
    val progress = budgetItem.percentage.coerceIn(0f, 1f)
    val isWarning = budgetItem.isAlertTriggered
    val isExceeded = budgetItem.isExceeded

    val barColor = when {
        isExceeded -> CoralError
        isWarning -> AmberWarning
        else -> EmeraldPrimary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(budgetItem.category.colorHex))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = budgetItem.category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isExceeded) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CoralError.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("EXCEEDED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoralError)
                    }
                } else if (isWarning) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AmberWarning.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("80% ALERT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CurrencyFormatter.format(budgetItem.currentSpentInInr, displayCurrency)} spent",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cap: ${CurrencyFormatter.format(budgetItem.monthlyLimitInInr, displayCurrency)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(budgetItem.percentage * 100).toInt()}% utilized",
                    fontSize = 11.sp,
                    color = barColor,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Adjust Cap", fontSize = 11.sp, color = CyanAccent)
                }
            }
        }
    }
}

@Composable
private fun EditLimitDialog(
    budgetItem: BudgetCategory,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var limitInput by remember { mutableStateOf(budgetItem.monthlyLimitInInr.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Limit: ${budgetItem.category.displayName}") },
        text = {
            Column {
                Text("Enter monthly spending limit in Rupees (₹):", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Limit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = limitInput.toDoubleOrNull() ?: budgetItem.monthlyLimitInInr
                    onSave(parsed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
