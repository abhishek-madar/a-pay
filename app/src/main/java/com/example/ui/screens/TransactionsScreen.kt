package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    displayCurrency: AppCurrency,
    searchQuery: String,
    statusFilter: TransactionStatus?,
    gatewayFilter: PaymentGateway?,
    currencyFilter: AppCurrency?,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (TransactionStatus?) -> Unit,
    onGatewayFilterChange: (PaymentGateway?) -> Unit,
    onCurrencyFilterChange: (AppCurrency?) -> Unit,
    onClearFilters: () -> Unit,
    onSelectTransaction: (Transaction) -> Unit,
    onOpenNewPayment: () -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar with New Payment & Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by ID, client, memo...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("transactions_search_input")
            )

            // Export Button
            IconButton(
                onClick = onOpenExport,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("transactions_export_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export CSV",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // New Payment Action
            Button(
                onClick = onOpenNewPayment,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("transactions_new_payment_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pay", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // All / Clear Chip
            FilterChip(
                selected = statusFilter == null && gatewayFilter == null && currencyFilter == null,
                onClick = onClearFilters,
                label = { Text("All (${transactions.size})", fontSize = 11.sp) }
            )

            // Status Filter Chips
            TransactionStatus.entries.forEach { status ->
                FilterChip(
                    selected = statusFilter == status,
                    onClick = {
                        onStatusFilterChange(if (statusFilter == status) null else status)
                    },
                    label = { Text(status.label, fontSize = 11.sp) }
                )
            }

            // Gateway Filter Chips
            PaymentGateway.entries.forEach { gw ->
                FilterChip(
                    selected = gatewayFilter == gw,
                    onClick = {
                        onGatewayFilterChange(if (gatewayFilter == gw) null else gw)
                    },
                    label = { Text(gw.title, fontSize = 11.sp) }
                )
            }

            // Currency Filter Chips
            AppCurrency.entries.forEach { curr ->
                FilterChip(
                    selected = currencyFilter == curr,
                    onClick = {
                        onCurrencyFilterChange(if (currencyFilter == curr) null else curr)
                    },
                    label = { Text("${curr.flag} ${curr.code}", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No transactions match your search or filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onClearFilters) {
                        Text("Reset All Filters", color = EmeraldPrimary)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { txn ->
                    TransactionCardItem(
                        transaction = txn,
                        displayCurrency = displayCurrency,
                        onClick = { onSelectTransaction(txn) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionCardItem(
    transaction: Transaction,
    displayCurrency: AppCurrency,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: ID, E2E Lock, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.id,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // E2E badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "E2E", tint = EmeraldPrimary, modifier = Modifier.size(9.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "E2E", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(transaction.status.colorHex).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = transaction.status.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(transaction.status.colorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Row: Title & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.customerName} • ${transaction.gateway.title}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (transaction.type == TransactionType.INCOMING) "+" else "-") +
                                CurrencyFormatter.format(transaction.amountInInr, displayCurrency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (transaction.type == TransactionType.INCOMING) EmeraldPrimary else MaterialTheme.colorScheme.error
                    )
                    if (transaction.originalCurrency != AppCurrency.INR) {
                        Text(
                            text = "${transaction.originalCurrency.symbol}${String.format("%.2f", transaction.originalAmount)} ${transaction.originalCurrency.code}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Tax breakdown & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${transaction.taxRegion.taxLabel}: ${CurrencyFormatter.formatInr(transaction.taxAmountInInr)} (${transaction.taxRegion.defaultRatePercent}%)",
                    fontSize = 10.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = transaction.dateString,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
