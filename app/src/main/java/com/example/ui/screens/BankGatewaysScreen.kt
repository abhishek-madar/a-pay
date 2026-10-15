package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.CloudSyncInfo
import com.example.model.AppCurrency
import com.example.model.ConnectedBankAccount
import com.example.model.CurrencyFormatter
import com.example.model.PaymentGateway
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireSecondary

@Composable
fun BankGatewaysScreen(
    bankAccounts: List<ConnectedBankAccount>,
    displayCurrency: AppCurrency,
    cloudSyncInfo: CloudSyncInfo,
    onTriggerCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddBankDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cloud Sync & Cross-Device Backup Banner
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SapphireSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = SapphireSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Secure Cloud Sync & Backup",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Multi-device encrypted ledger with offline caching",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onTriggerCloudSync()
                                Toast.makeText(context, "Ledger synchronized across cloud devices", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary),
                            modifier = Modifier.testTag("sync_now_button")
                        ) {
                            Text("Sync Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: Encrypted Cloud Active (${cloudSyncInfo.lastSyncText})",
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Offline Access: Enabled",
                            fontSize = 11.sp,
                            color = CyanAccent
                        )
                    }
                }
            }
        }

        // Section Title: Payment Gateways
        item {
            Text(
                text = "Integrated Payment Gateways",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Gateways Cards
        item {
            GatewayConfigCard(
                title = "Stripe Connect",
                subtitle = "Credit Cards, Apple Pay, SEPA, ACH Payments",
                fee = "2.9% + 30¢",
                apiKeyMasked = "pk_live_••••••••••••••••9421",
                isSandbox = false,
                color = Color(0xFF635BFF)
            )
        }

        item {
            GatewayConfigCard(
                title = "PayPal Express",
                subtitle = "Global Wallets, Venmo, Pay Later International",
                fee = "3.4% + flat",
                apiKeyMasked = "client_id_••••••••••••33A9",
                isSandbox = false,
                color = Color(0xFF003087)
            )
        }

        item {
            GatewayConfigCard(
                title = "Razorpay International",
                subtitle = "Domestic UPI, RuPay, NetBanking & Cards",
                fee = "2.0% standard",
                apiKeyMasked = "rzp_live_••••••••••••8812",
                isSandbox = false,
                color = Color(0xFF0C2340)
            )
        }

        item {
            GatewayConfigCard(
                title = "Direct Bank Wire / SWIFT",
                subtitle = "Automated RTGS, NEFT, SWIFT cross-border clearing",
                fee = "0.5% flat wire",
                apiKeyMasked = "SWIFT: HDFCINBB440",
                isSandbox = false,
                color = EmeraldPrimary
            )
        }

        // Connected Bank Accounts Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Connected Bank Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time automated transaction importing",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = { showAddBankDialog = true },
                    modifier = Modifier.testTag("link_bank_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Link Bank", fontSize = 12.sp, color = EmeraldPrimary)
                }
            }
        }

        items(bankAccounts) { account ->
            BankAccountCard(account = account, displayCurrency = displayCurrency)
        }
    }

    if (showAddBankDialog) {
        AlertDialog(
            onDismissRequest = { showAddBankDialog = false },
            title = { Text("Link Bank Account") },
            text = {
                Text("Select your financial institution (HDFC, SBI, ICICI, Chase, Barclays) to authorize real-time transaction ingestion via encrypted Open Banking API.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Open Banking API authenticated. Account linked.", Toast.LENGTH_SHORT).show()
                        showAddBankDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Authenticate & Link", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBankDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GatewayConfigCard(
    title: String,
    subtitle: String,
    fee: String,
    apiKeyMasked: String,
    isSandbox: Boolean,
    color: Color
) {
    var isLive by remember { mutableStateOf(!isSandbox) }

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
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isLive) "LIVE" else "SANDBOX",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLive) EmeraldPrimary else CyanAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isLive,
                        onCheckedChange = { isLive = it },
                        modifier = Modifier.size(width = 38.dp, height = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = apiKeyMasked,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Processing Fee: $fee",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BankAccountCard(
    account: ConnectedBankAccount,
    displayCurrency: AppCurrency
) {
    var isAutoSync by remember { mutableStateOf(account.isAutoSyncEnabled) }

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
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = account.bankName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${account.accountType} • ${account.maskedNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = CurrencyFormatter.format(account.balanceInInr, displayCurrency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last synced: ${account.lastSyncTime}",
                    fontSize = 11.sp,
                    color = CyanAccent
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAutoSync) "Auto-Import ON" else "Paused",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isAutoSync,
                        onCheckedChange = { isAutoSync = it },
                        modifier = Modifier.size(width = 38.dp, height = 24.dp)
                    )
                }
            }
        }
    }
}
