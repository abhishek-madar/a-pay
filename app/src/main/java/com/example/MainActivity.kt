package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppCurrency
import com.example.model.UserRole
import com.example.ui.AppTab
import com.example.ui.PaymentViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.repository.isDarkMode.collectAsStateWithLifecycle()
            val currentRole by viewModel.repository.currentRole.collectAsStateWithLifecycle()
            val displayCurrency by viewModel.repository.displayCurrency.collectAsStateWithLifecycle()
            val cloudSyncInfo by viewModel.repository.cloudSyncState.collectAsStateWithLifecycle()
            val encryptionStatus by viewModel.repository.encryptionStatus.collectAsStateWithLifecycle()
            val twoFactorState by viewModel.repository.twoFactorState.collectAsStateWithLifecycle()
            val biometricState by viewModel.repository.biometricState.collectAsStateWithLifecycle()

            val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
            val allTransactions by viewModel.repository.transactions.collectAsStateWithLifecycle()
            val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
            val recurringList by viewModel.repository.recurringBillings.collectAsStateWithLifecycle()
            val invoices by viewModel.repository.invoices.collectAsStateWithLifecycle()
            val budgets by viewModel.repository.budgets.collectAsStateWithLifecycle()
            val bankAccounts by viewModel.repository.bankAccounts.collectAsStateWithLifecycle()
            val notifications by viewModel.repository.notifications.collectAsStateWithLifecycle()

            val totalVolumeInr by viewModel.totalVolumeInr.collectAsStateWithLifecycle()
            val totalTaxInr by viewModel.totalTaxCollectedInr.collectAsStateWithLifecycle()

            // Dialog / Sheet states
            val showCurrencySheet by viewModel.showCurrencySheet.collectAsStateWithLifecycle()
            val showSecuritySheet by viewModel.showSecuritySheet.collectAsStateWithLifecycle()
            val showRoleDialog by viewModel.showRoleDialog.collectAsStateWithLifecycle()
            val showNewPaymentDialog by viewModel.showNewPaymentDialog.collectAsStateWithLifecycle()
            val showNewInvoiceDialog by viewModel.showNewInvoiceDialog.collectAsStateWithLifecycle()
            val showBudgetCalculator by viewModel.showBudgetCalculator.collectAsStateWithLifecycle()
            val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()
            val showQrScanner by viewModel.showQrScanner.collectAsStateWithLifecycle()
            val showNotificationsSheet by viewModel.showNotificationsSheet.collectAsStateWithLifecycle()
            val selectedTxn by viewModel.selectedTransactionDetails.collectAsStateWithLifecycle()

            // Search & Filter
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
            val gatewayFilter by viewModel.gatewayFilter.collectAsStateWithLifecycle()
            val currencyFilter by viewModel.currencyFilter.collectAsStateWithLifecycle()

            val unreadNotifsCount = notifications.count { !it.isRead }

            MyApplicationTheme(darkTheme = isDarkMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopBar(
                            currentCurrency = displayCurrency,
                            currentRole = currentRole,
                            cloudSyncInfo = cloudSyncInfo,
                            unreadNotificationsCount = unreadNotifsCount,
                            isDarkMode = isDarkMode,
                            onCurrencyClick = { viewModel.setShowCurrencySheet(true) },
                            onSecurityClick = { viewModel.setShowSecuritySheet(true) },
                            onScanQrClick = { viewModel.setShowQrScanner(true) },
                            onRoleClick = { viewModel.setShowRoleDialog(true) },
                            onSyncClick = { viewModel.triggerCloudSync() },
                            onNotificationsClick = { viewModel.setShowNotificationsSheet(true) },
                            onToggleDarkMode = { viewModel.toggleDarkMode() }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            AppTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.setTab(tab) },
                                    icon = {
                                        val icon = when (tab) {
                                            AppTab.DASHBOARD -> Icons.Default.Dashboard
                                            AppTab.TRANSACTIONS -> Icons.Default.ReceiptLong
                                            AppTab.RECURRING -> Icons.Default.Autorenew
                                            AppTab.BUDGETS -> Icons.Default.AccountBalanceWallet
                                            AppTab.GATEWAYS -> Icons.Default.AccountBalance
                                        }
                                        Icon(imageVector = icon, contentDescription = tab.title)
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        indicatorColor = EmeraldPrimary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentTab == AppTab.DASHBOARD || currentTab == AppTab.TRANSACTIONS) {
                            ExtendedFloatingActionButton(
                                onClick = { viewModel.setShowNewPaymentDialog(true) },
                                icon = { Icon(Icons.Default.AddCard, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp)) },
                                text = { Text("Process Payment", color = Color.Black, fontWeight = FontWeight.Bold) },
                                containerColor = EmeraldPrimary,
                                modifier = Modifier.testTag("fab_process_payment")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.DASHBOARD -> {
                                DashboardScreen(
                                    displayCurrency = displayCurrency,
                                    totalVolumeInr = totalVolumeInr,
                                    totalTaxInr = totalTaxInr,
                                    transactions = allTransactions,
                                    onSelectTransaction = { viewModel.selectTransaction(it) },
                                    onOpenNewPayment = { viewModel.setShowNewPaymentDialog(true) },
                                    onOpenNewInvoice = { viewModel.setShowNewInvoiceDialog(true) },
                                    onOpenConverter = { viewModel.setShowCurrencySheet(true) },
                                    onOpenExport = { viewModel.setShowExportDialog(true) },
                                    onOpenBudgetCalc = { viewModel.setShowBudgetCalculator(true) },
                                    onOpenScanQr = { viewModel.setShowQrScanner(true) }
                                )
                            }

                            AppTab.TRANSACTIONS -> {
                                TransactionsScreen(
                                    transactions = filteredTransactions,
                                    displayCurrency = displayCurrency,
                                    searchQuery = searchQuery,
                                    statusFilter = statusFilter,
                                    gatewayFilter = gatewayFilter,
                                    currencyFilter = currencyFilter,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onStatusFilterChange = { viewModel.setStatusFilter(it) },
                                    onGatewayFilterChange = { viewModel.setGatewayFilter(it) },
                                    onCurrencyFilterChange = { viewModel.setCurrencyFilter(it) },
                                    onClearFilters = { viewModel.clearFilters() },
                                    onSelectTransaction = { viewModel.selectTransaction(it) },
                                    onOpenNewPayment = { viewModel.setShowNewPaymentDialog(true) },
                                    onOpenExport = { viewModel.setShowExportDialog(true) }
                                )
                            }

                            AppTab.RECURRING -> {
                                RecurringScreen(
                                    recurringList = recurringList,
                                    invoices = invoices,
                                    displayCurrency = displayCurrency,
                                    onOpenNewInvoice = { viewModel.setShowNewInvoiceDialog(true) }
                                )
                            }

                            AppTab.BUDGETS -> {
                                BudgetsScreen(
                                    budgets = budgets,
                                    displayCurrency = displayCurrency,
                                    onOpenCalculator = { viewModel.setShowBudgetCalculator(true) },
                                    onUpdateBudgetLimit = { cat, limit -> viewModel.updateBudgetLimit(cat, limit) }
                                )
                            }

                            AppTab.GATEWAYS -> {
                                BankGatewaysScreen(
                                    bankAccounts = bankAccounts,
                                    displayCurrency = displayCurrency,
                                    cloudSyncInfo = cloudSyncInfo,
                                    onTriggerCloudSync = { viewModel.triggerCloudSync() }
                                )
                            }
                        }
                    }

                    // Dialog & Sheet Overlays
                    if (showCurrencySheet) {
                        CurrencyConverterSheet(
                            currentCurrency = displayCurrency,
                            onSelectDisplayCurrency = { viewModel.setDisplayCurrency(it) },
                            onDismiss = { viewModel.setShowCurrencySheet(false) }
                        )
                    }

                    if (showSecuritySheet) {
                        SecurityDialog(
                            encryptionStatus = encryptionStatus,
                            twoFactorState = twoFactorState,
                            biometricState = biometricState,
                            onToggle2FA = { viewModel.toggle2FA() },
                            onToggleBiometric = { viewModel.toggleBiometric() },
                            onDismiss = { viewModel.setShowSecuritySheet(false) }
                        )
                    }

                    if (showRoleDialog) {
                        RoleSelectorDialog(
                            currentRole = currentRole,
                            onSelectRole = { viewModel.setRole(it) },
                            onDismiss = { viewModel.setShowRoleDialog(false) }
                        )
                    }

                    if (showNewPaymentDialog) {
                        NewPaymentDialog(
                            onProcessPayment = { title, cust, email, amount, curr, gw, cat, taxReg ->
                                viewModel.processPayment(title, cust, email, amount, curr, gw, cat, taxReg)
                            },
                            onDismiss = { viewModel.setShowNewPaymentDialog(false) }
                        )
                    }

                    if (showNewInvoiceDialog) {
                        NewInvoiceDialog(
                            onCreateInvoice = { rec, email, subtotal, curr, taxReg, due, recToggle, notes ->
                                viewModel.createInvoice(rec, email, subtotal, curr, taxReg, due, recToggle, notes)
                            },
                            onDismiss = { viewModel.setShowNewInvoiceDialog(false) }
                        )
                    }

                    if (showBudgetCalculator) {
                        BudgetCalculatorDialog(
                            onApplyBudgetLimits = { limitsMap ->
                                limitsMap.forEach { (cat, limit) ->
                                    viewModel.updateBudgetLimit(cat, limit)
                                }
                            },
                            onDismiss = { viewModel.setShowBudgetCalculator(false) }
                        )
                    }

                    if (showExportDialog) {
                        ExportReportDialog(
                            bookkeepingCsv = viewModel.repository.generateBookkeepingCsv(),
                            taxReportCsv = viewModel.repository.generateTaxReportCsv(),
                            onDismiss = { viewModel.setShowExportDialog(false) }
                        )
                    }

                    if (showQrScanner) {
                        QrCodeScannerSheet(
                            onPayScannedInvoice = { desc, merchant, amount, curr, gw, cat, taxReg ->
                                viewModel.processPayment(
                                    title = desc,
                                    customerName = merchant,
                                    customerEmail = "billing@${merchant.lowercase().replace(" ", "")}.com",
                                    amountInSource = amount,
                                    sourceCurrency = curr,
                                    gateway = gw,
                                    category = cat,
                                    taxRegion = taxReg
                                )
                            },
                            onDismiss = { viewModel.setShowQrScanner(false) }
                        )
                    }

                    if (showNotificationsSheet) {
                        NotificationsSheet(
                            notifications = notifications,
                            onMarkAllRead = { viewModel.repository.markAllNotificationsRead() },
                            onDismiss = { viewModel.setShowNotificationsSheet(false) }
                        )
                    }

                    selectedTxn?.let { txn ->
                        TransactionDetailDialog(
                            transaction = txn,
                            onDismiss = { viewModel.selectTransaction(null) }
                        )
                    }
                }
            }
        }
    }
}
