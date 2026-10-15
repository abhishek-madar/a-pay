package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PaymentRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconName: String) {
    DASHBOARD("Dashboard", "Dashboard"),
    TRANSACTIONS("Transactions", "ReceiptLong"),
    RECURRING("Recurring", "Autorenew"),
    BUDGETS("Budgets", "AccountBalanceWallet"),
    GATEWAYS("Gateways", "AccountBalance")
}

class PaymentViewModel(
    val repository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Dialog & Sheet States
    private val _showCurrencySheet = MutableStateFlow(false)
    val showCurrencySheet: StateFlow<Boolean> = _showCurrencySheet.asStateFlow()

    private val _showSecuritySheet = MutableStateFlow(false)
    val showSecuritySheet: StateFlow<Boolean> = _showSecuritySheet.asStateFlow()

    private val _showRoleDialog = MutableStateFlow(false)
    val showRoleDialog: StateFlow<Boolean> = _showRoleDialog.asStateFlow()

    private val _showNewPaymentDialog = MutableStateFlow(false)
    val showNewPaymentDialog: StateFlow<Boolean> = _showNewPaymentDialog.asStateFlow()

    private val _showNewInvoiceDialog = MutableStateFlow(false)
    val showNewInvoiceDialog: StateFlow<Boolean> = _showNewInvoiceDialog.asStateFlow()

    private val _showBudgetCalculator = MutableStateFlow(false)
    val showBudgetCalculator: StateFlow<Boolean> = _showBudgetCalculator.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showQrScanner = MutableStateFlow(false)
    val showQrScanner: StateFlow<Boolean> = _showQrScanner.asStateFlow()

    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    private val _selectedTransactionDetails = MutableStateFlow<Transaction?>(null)
    val selectedTransactionDetails: StateFlow<Transaction?> = _selectedTransactionDetails.asStateFlow()

    // Transaction Search & Filter criteria
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<TransactionStatus?>(null)
    val statusFilter: StateFlow<TransactionStatus?> = _statusFilter.asStateFlow()

    private val _gatewayFilter = MutableStateFlow<PaymentGateway?>(null)
    val gatewayFilter: StateFlow<PaymentGateway?> = _gatewayFilter.asStateFlow()

    private val _currencyFilter = MutableStateFlow<AppCurrency?>(null)
    val currencyFilter: StateFlow<AppCurrency?> = _currencyFilter.asStateFlow()

    // Filtered Transactions Flow
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        repository.transactions,
        _searchQuery,
        _statusFilter,
        _gatewayFilter,
        _currencyFilter
    ) { txns, query, status, gateway, curr ->
        txns.filter { txn ->
            val matchesQuery = query.isBlank() ||
                    txn.title.contains(query, ignoreCase = true) ||
                    txn.customerName.contains(query, ignoreCase = true) ||
                    txn.id.contains(query, ignoreCase = true) ||
                    txn.gatewayRefId.contains(query, ignoreCase = true)

            val matchesStatus = status == null || txn.status == status
            val matchesGateway = gateway == null || txn.gateway == gateway
            val matchesCurr = curr == null || txn.originalCurrency == curr

            matchesQuery && matchesStatus && matchesGateway && matchesCurr
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for Dashboard
    val totalVolumeInr: StateFlow<Double> = repository.transactions.combine(_currentTab) { txns, _ ->
        txns.filter { it.status == TransactionStatus.COMPLETED }.sumOf { it.amountInInr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalTaxCollectedInr: StateFlow<Double> = repository.transactions.combine(_currentTab) { txns, _ ->
        txns.filter { it.status == TransactionStatus.COMPLETED }.sumOf { it.taxAmountInInr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Navigation
    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Filter setters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: TransactionStatus?) {
        _statusFilter.value = status
    }

    fun setGatewayFilter(gateway: PaymentGateway?) {
        _gatewayFilter.value = gateway
    }

    fun setCurrencyFilter(currency: AppCurrency?) {
        _currencyFilter.value = currency
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _statusFilter.value = null
        _gatewayFilter.value = null
        _currencyFilter.value = null
    }

    // Dialog toggles
    fun setShowCurrencySheet(show: Boolean) { _showCurrencySheet.value = show }
    fun setShowSecuritySheet(show: Boolean) { _showSecuritySheet.value = show }
    fun setShowRoleDialog(show: Boolean) { _showRoleDialog.value = show }
    fun setShowNewPaymentDialog(show: Boolean) { _showNewPaymentDialog.value = show }
    fun setShowNewInvoiceDialog(show: Boolean) { _showNewInvoiceDialog.value = show }
    fun setShowBudgetCalculator(show: Boolean) { _showBudgetCalculator.value = show }
    fun setShowExportDialog(show: Boolean) { _showExportDialog.value = show }
    fun setShowQrScanner(show: Boolean) { _showQrScanner.value = show }
    fun setShowNotificationsSheet(show: Boolean) { _showNotificationsSheet.value = show }
    fun selectTransaction(txn: Transaction?) { _selectedTransactionDetails.value = txn }

    // Actions delegation
    fun setDisplayCurrency(currency: AppCurrency) {
        repository.setDisplayCurrency(currency)
    }

    fun setRole(role: UserRole) {
        repository.setRole(role)
    }

    fun toggleDarkMode() {
        repository.toggleDarkMode()
    }

    fun triggerCloudSync() {
        repository.triggerCloudSync()
    }

    fun toggle2FA() {
        repository.toggle2FA()
    }

    fun toggleBiometric() {
        repository.toggleBiometric()
    }

    fun processPayment(
        title: String,
        customerName: String,
        customerEmail: String,
        amountInSource: Double,
        sourceCurrency: AppCurrency,
        gateway: PaymentGateway,
        category: ExpenseCategory,
        taxRegion: TaxRegion
    ) {
        repository.processPayment(
            title, customerName, customerEmail, amountInSource, sourceCurrency, gateway, category, taxRegion
        )
        _showNewPaymentDialog.value = false
    }

    fun createInvoice(
        recipient: String,
        recipientEmail: String,
        subtotalAmount: Double,
        currency: AppCurrency,
        taxRegion: TaxRegion,
        dueDate: String,
        isRecurring: Boolean,
        notes: String
    ) {
        repository.createInvoice(
            recipient, recipientEmail, subtotalAmount, currency, taxRegion, dueDate, isRecurring, notes
        )
        _showNewInvoiceDialog.value = false
    }

    fun addRecurringBilling(
        title: String,
        clientOrVendor: String,
        amount: Double,
        currency: AppCurrency,
        frequency: String,
        category: ExpenseCategory,
        gateway: PaymentGateway,
        autoDebit: Boolean
    ) {
        repository.addRecurringBilling(title, clientOrVendor, amount, currency, frequency, category, gateway, autoDebit)
    }

    fun updateBudgetLimit(category: ExpenseCategory, newLimitInInr: Double) {
        repository.updateBudgetLimit(category, newLimitInInr)
    }
}
