package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PaymentRepository {

    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _displayCurrency = MutableStateFlow(AppCurrency.INR)
    val displayCurrency: StateFlow<AppCurrency> = _displayCurrency.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _cloudSyncState = MutableStateFlow(CloudSyncInfo(isSynced = true, lastSyncText = "Just now", pendingItems = 0))
    val cloudSyncState: StateFlow<CloudSyncInfo> = _cloudSyncState.asStateFlow()

    private val _twoFactorState = MutableStateFlow(TwoFactorState())
    val twoFactorState: StateFlow<TwoFactorState> = _twoFactorState.asStateFlow()

    private val _biometricState = MutableStateFlow(BiometricState())
    val biometricState: StateFlow<BiometricState> = _biometricState.asStateFlow()

    private val _encryptionStatus = MutableStateFlow(EncryptionStatus())
    val encryptionStatus: StateFlow<EncryptionStatus> = _encryptionStatus.asStateFlow()

    // Financial Data Flows
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _recurringBillings = MutableStateFlow<List<RecurringBilling>>(emptyList())
    val recurringBillings: StateFlow<List<RecurringBilling>> = _recurringBillings.asStateFlow()

    private val _invoices = MutableStateFlow<List<VendorInvoice>>(emptyList())
    val invoices: StateFlow<List<VendorInvoice>> = _invoices.asStateFlow()

    private val _budgets = MutableStateFlow<List<BudgetCategory>>(emptyList())
    val budgets: StateFlow<List<BudgetCategory>> = _budgets.asStateFlow()

    private val _bankAccounts = MutableStateFlow<List<ConnectedBankAccount>>(emptyList())
    val bankAccounts: StateFlow<List<ConnectedBankAccount>> = _bankAccounts.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)

        val sampleTransactions = listOf(
            Transaction(
                id = "TXN-84920",
                title = "Stripe Global Payout - SaaS Enterprise",
                customerName = "Apex Cloud Systems Inc.",
                customerEmail = "billing@apexcloud.io",
                category = ExpenseCategory.SOFTWARE_CLOUD,
                type = TransactionType.INCOMING,
                amountInInr = 285000.0,
                originalCurrency = AppCurrency.USD,
                originalAmount = 3372.78,
                gateway = PaymentGateway.STRIPE,
                gatewayRefId = "pi_3MtwL2EZepomrf1g0x",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 1800000,
                dateString = sdf.format(Date(now - 1800000)),
                taxRegion = TaxRegion.US_CALIFORNIA,
                taxAmountInInr = 20662.5,
                e2ePayloadHash = generateHash("pi_3MtwL2EZepomrf1g0x:285000:USD")
            ),
            Transaction(
                id = "TXN-84919",
                title = "PayPal Checkout - EU License Retainer",
                customerName = "Nordic FinTech GmbH",
                customerEmail = "accounts@nordictech.de",
                category = ExpenseCategory.VENDOR_PAYMENT,
                type = TransactionType.INCOMING,
                amountInInr = 145000.0,
                originalCurrency = AppCurrency.EUR,
                originalAmount = 1589.91,
                gateway = PaymentGateway.PAYPAL,
                gatewayRefId = "PAYID-MQ5V33A9876",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 7200000,
                dateString = sdf.format(Date(now - 7200000)),
                taxRegion = TaxRegion.EU_GERMANY,
                taxAmountInInr = 27550.0,
                e2ePayloadHash = generateHash("PAYID-MQ5V33A9876:145000:EUR")
            ),
            Transaction(
                id = "TXN-84918",
                title = "AWS Cloud Infrastructure Billing",
                customerName = "Amazon Web Services",
                customerEmail = "aws-invoices@amazon.com",
                category = ExpenseCategory.SOFTWARE_CLOUD,
                type = TransactionType.OUTGOING,
                amountInInr = 48500.0,
                originalCurrency = AppCurrency.INR,
                originalAmount = 48500.0,
                gateway = PaymentGateway.BANK_WIRE,
                gatewayRefId = "SWIFT-HDFC-99210",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 18000000,
                dateString = sdf.format(Date(now - 18000000)),
                taxRegion = TaxRegion.INDIA_GST_18,
                taxAmountInInr = 8730.0,
                e2ePayloadHash = generateHash("SWIFT-HDFC-99210:48500:INR")
            ),
            Transaction(
                id = "TXN-84917",
                title = "Dubai Consulting Retainer Payment",
                customerName = "Emirates Digital Ventures",
                customerEmail = "finance@emiratesdv.ae",
                category = ExpenseCategory.VENDOR_PAYMENT,
                type = TransactionType.INCOMING,
                amountInInr = 320000.0,
                originalCurrency = AppCurrency.AED,
                originalAmount = 13913.04,
                gateway = PaymentGateway.STRIPE,
                gatewayRefId = "pi_3N939A098LKa2",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 36000000,
                dateString = sdf.format(Date(now - 36000000)),
                taxRegion = TaxRegion.UAE_VAT,
                taxAmountInInr = 16000.0,
                e2ePayloadHash = generateHash("pi_3N939A098LKa2:320000:AED")
            ),
            Transaction(
                id = "TXN-84916",
                title = "Quarterly Advance Corporate GST Filing",
                customerName = "Government of India GST Portal",
                customerEmail = "gst-portal@gov.in",
                category = ExpenseCategory.TAX_DISBURSEMENT,
                type = TransactionType.OUTGOING,
                amountInInr = 92400.0,
                originalCurrency = AppCurrency.INR,
                originalAmount = 92400.0,
                gateway = PaymentGateway.UPI_INSTANT,
                gatewayRefId = "UPI-2026-GSTN-8812",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 86400000,
                dateString = sdf.format(Date(now - 86400000)),
                taxRegion = TaxRegion.INDIA_GST_18,
                taxAmountInInr = 92400.0,
                e2ePayloadHash = generateHash("UPI-2026-GSTN-8812:92400:INR")
            ),
            Transaction(
                id = "TXN-84915",
                title = "Stripe Processing Gateway Fee Batch",
                customerName = "Stripe Inc. Processing",
                customerEmail = "receipts@stripe.com",
                category = ExpenseCategory.PAYMENT_FEES,
                type = TransactionType.OUTGOING,
                amountInInr = 12450.0,
                originalCurrency = AppCurrency.INR,
                originalAmount = 12450.0,
                gateway = PaymentGateway.STRIPE,
                gatewayRefId = "fee_90241A8",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 172800000,
                dateString = sdf.format(Date(now - 172800000)),
                taxRegion = TaxRegion.INDIA_GST_18,
                taxAmountInInr = 2241.0,
                e2ePayloadHash = generateHash("fee_90241A8:12450:INR")
            ),
            Transaction(
                id = "TXN-84914",
                title = "Contractor Retainer - Senior Engineer",
                customerName = "DevForge Studios London",
                customerEmail = "alex@devforge.co.uk",
                category = ExpenseCategory.SALARIES_CONTRACTORS,
                type = TransactionType.OUTGOING,
                amountInInr = 160000.0,
                originalCurrency = AppCurrency.GBP,
                originalAmount = 1473.30,
                gateway = PaymentGateway.BANK_WIRE,
                gatewayRefId = "BARC-WIRE-11029",
                status = TransactionStatus.COMPLETED,
                timestamp = now - 259200000,
                dateString = sdf.format(Date(now - 259200000)),
                taxRegion = TaxRegion.UK_VAT,
                taxAmountInInr = 32000.0,
                e2ePayloadHash = generateHash("BARC-WIRE-11029:160000:GBP")
            )
        )
        _transactions.value = sampleTransactions

        _recurringBillings.value = listOf(
            RecurringBilling(
                id = "REC-101",
                title = "Enterprise API Gateway Tier 1",
                clientOrVendorName = "Apex Cloud Systems Inc.",
                category = ExpenseCategory.SOFTWARE_CLOUD,
                frequency = "Monthly",
                amountInInr = 285000.0,
                currency = AppCurrency.USD,
                nextBillingDate = "1st Nov 2026",
                autoDebit = true,
                gateway = PaymentGateway.STRIPE,
                status = "Active"
            ),
            RecurringBilling(
                id = "REC-102",
                title = "EU Security Compliance Audit Retainer",
                clientOrVendorName = "Nordic FinTech GmbH",
                category = ExpenseCategory.VENDOR_PAYMENT,
                frequency = "Monthly",
                amountInInr = 145000.0,
                currency = AppCurrency.EUR,
                nextBillingDate = "5th Nov 2026",
                autoDebit = true,
                gateway = PaymentGateway.PAYPAL,
                status = "Active"
            ),
            RecurringBilling(
                id = "REC-103",
                title = "AWS Multi-Region Dedicated Clusters",
                clientOrVendorName = "Amazon Web Services",
                category = ExpenseCategory.SOFTWARE_CLOUD,
                frequency = "Monthly",
                amountInInr = 48500.0,
                currency = AppCurrency.INR,
                nextBillingDate = "28th Oct 2026",
                autoDebit = true,
                gateway = PaymentGateway.BANK_WIRE,
                status = "Active"
            ),
            RecurringBilling(
                id = "REC-104",
                title = "Google Workspace & AI Cloud Cluster",
                clientOrVendorName = "Google Cloud EMEA",
                category = ExpenseCategory.SOFTWARE_CLOUD,
                frequency = "Monthly",
                amountInInr = 34000.0,
                currency = AppCurrency.INR,
                nextBillingDate = "2nd Nov 2026",
                autoDebit = true,
                gateway = PaymentGateway.STRIPE,
                status = "Active"
            )
        )

        _invoices.value = listOf(
            VendorInvoice(
                id = "INV-2026-001",
                invoiceNumber = "INV-2026-089",
                recipient = "Apex Cloud Systems Inc.",
                recipientEmail = "billing@apexcloud.io",
                issueDate = "14 Oct 2026",
                dueDate = "28 Oct 2026",
                subtotalInInr = 285000.0,
                taxRegion = TaxRegion.US_CALIFORNIA,
                taxAmountInInr = 20662.5,
                totalInInr = 305662.5,
                currency = AppCurrency.USD,
                status = "Paid",
                isRecurring = true,
                notes = "Auto-debited via Stripe tokenized recurring billing."
            ),
            VendorInvoice(
                id = "INV-2026-002",
                invoiceNumber = "INV-2026-090",
                recipient = "Emirates Digital Ventures",
                recipientEmail = "finance@emiratesdv.ae",
                issueDate = "10 Oct 2026",
                dueDate = "24 Oct 2026",
                subtotalInInr = 320000.0,
                taxRegion = TaxRegion.UAE_VAT,
                taxAmountInInr = 16000.0,
                totalInInr = 336000.0,
                currency = AppCurrency.AED,
                status = "Pending",
                isRecurring = false,
                notes = "Urgent: Payment deadline approaching in 10 days."
            ),
            VendorInvoice(
                id = "INV-2026-003",
                invoiceNumber = "INV-2026-091",
                recipient = "Global Data Corp Tokyo",
                recipientEmail = "ap@globaldata.co.jp",
                issueDate = "01 Oct 2026",
                dueDate = "15 Oct 2026",
                subtotalInInr = 180000.0,
                taxRegion = TaxRegion.JAPAN_JCT,
                taxAmountInInr = 18000.0,
                totalInInr = 198000.0,
                currency = AppCurrency.JPY,
                status = "Overdue",
                isRecurring = false,
                notes = "Automated reminder dispatched with 10% late notice."
            )
        )

        _budgets.value = listOf(
            BudgetCategory(ExpenseCategory.SOFTWARE_CLOUD, monthlyLimitInInr = 120000.0, currentSpentInInr = 82500.0),
            BudgetCategory(ExpenseCategory.SALARIES_CONTRACTORS, monthlyLimitInInr = 350000.0, currentSpentInInr = 310000.0),
            BudgetCategory(ExpenseCategory.TAX_DISBURSEMENT, monthlyLimitInInr = 150000.0, currentSpentInInr = 92400.0),
            BudgetCategory(ExpenseCategory.VENDOR_PAYMENT, monthlyLimitInInr = 100000.0, currentSpentInInr = 91000.0),
            BudgetCategory(ExpenseCategory.MARKETING_ADS, monthlyLimitInInr = 80000.0, currentSpentInInr = 45000.0),
            BudgetCategory(ExpenseCategory.PAYMENT_FEES, monthlyLimitInInr = 25000.0, currentSpentInInr = 12450.0)
        )

        _bankAccounts.value = listOf(
            ConnectedBankAccount(
                id = "BANK-01",
                bankName = "HDFC Corporate Bank",
                accountType = "Current Account (INR Master)",
                maskedNumber = "•••• 8921",
                balanceInInr = 1845200.0,
                currency = AppCurrency.INR,
                isAutoSyncEnabled = true,
                lastSyncTime = "Live sync active"
            ),
            ConnectedBankAccount(
                id = "BANK-02",
                bankName = "Chase Commercial Banking",
                accountType = "USD Multi-Currency Treasury",
                maskedNumber = "•••• 4410",
                balanceInInr = 1267500.0, // ~$15,000 USD
                currency = AppCurrency.USD,
                isAutoSyncEnabled = true,
                lastSyncTime = "Synced 5m ago"
            ),
            ConnectedBankAccount(
                id = "BANK-03",
                bankName = "Barclays International",
                accountType = "GBP Settlement Account",
                maskedNumber = "•••• 9102",
                balanceInInr = 760200.0, // ~£7,000 GBP
                currency = AppCurrency.GBP,
                isAutoSyncEnabled = true,
                lastSyncTime = "Synced 1h ago"
            )
        )

        _notifications.value = listOf(
            NotificationItem(
                id = "N1",
                title = "Payment Received: ₹2,85,000",
                message = "Apex Cloud Systems settled invoice #INV-2026-089 via Stripe Connect. End-to-end encrypted verification confirmed.",
                type = "PAYMENT",
                timestamp = "30m ago",
                isUrgent = false
            ),
            NotificationItem(
                id = "N2",
                title = "Urgent: Invoice Overdue Warning",
                message = "Invoice #INV-2026-091 to Global Data Corp (₹1,98,000) was due on 15 Oct. Automated reminder sent.",
                type = "DEADLINE",
                timestamp = "2h ago",
                isUrgent = true
            ),
            NotificationItem(
                id = "N3",
                title = "Budget Threshold Alert (91%)",
                message = "Vendor Invoices spending has reached ₹91,000 of your ₹1,00,000 monthly limit. Review upcoming debits.",
                type = "BUDGET",
                timestamp = "5h ago",
                isUrgent = true
            ),
            NotificationItem(
                id = "N4",
                title = "Upcoming Recurring Debit: AWS",
                message = "Automated vendor debit of ₹48,500 scheduled for 28th Oct via Direct Bank Wire.",
                type = "PAYMENT",
                timestamp = "1d ago",
                isUrgent = false
            )
        )
    }

    // Role Management
    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    // Currency Switcher
    fun setDisplayCurrency(currency: AppCurrency) {
        _displayCurrency.value = currency
    }

    // Theme Switcher
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Cloud Sync Simulator
    fun triggerCloudSync() {
        _cloudSyncState.value = CloudSyncInfo(isSynced = true, lastSyncText = "Just now", pendingItems = 0)
    }

    // 2FA & Biometric Toggles
    fun toggle2FA() {
        _twoFactorState.value = _twoFactorState.value.copy(isEnabled = !_twoFactorState.value.isEnabled)
    }

    fun toggleBiometric() {
        _biometricState.value = _biometricState.value.copy(
            isBiometricEnabled = !_biometricState.value.isBiometricEnabled
        )
    }

    // Process New Payment
    fun processPayment(
        title: String,
        customerName: String,
        customerEmail: String,
        amountInSource: Double,
        sourceCurrency: AppCurrency,
        gateway: PaymentGateway,
        category: ExpenseCategory,
        taxRegion: TaxRegion
    ): Transaction {
        val amountInInr = CurrencyFormatter.convertToInr(amountInSource, sourceCurrency)
        val taxCalc = TaxCalculator.calculate(amountInInr, taxRegion)
        val totalInr = taxCalc.totalAmount

        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
        val refId = when (gateway) {
            PaymentGateway.STRIPE -> "pi_3M" + UUID.randomUUID().toString().take(12)
            PaymentGateway.PAYPAL -> "PAYID-" + UUID.randomUUID().toString().take(10).uppercase()
            PaymentGateway.RAZORPAY -> "pay_" + UUID.randomUUID().toString().take(14)
            PaymentGateway.BANK_WIRE -> "SWIFT-REF-" + UUID.randomUUID().toString().take(8).uppercase()
            PaymentGateway.UPI_INSTANT -> "UPI-" + UUID.randomUUID().toString().take(8).uppercase()
        }

        val newTxn = Transaction(
            id = "TXN-" + (10000 + (Math.random() * 89999).toInt()),
            title = title,
            customerName = customerName,
            customerEmail = customerEmail,
            category = category,
            type = TransactionType.INCOMING,
            amountInInr = totalInr,
            originalCurrency = sourceCurrency,
            originalAmount = amountInSource + (amountInSource * taxRegion.defaultRatePercent / 100.0),
            gateway = gateway,
            gatewayRefId = refId,
            status = TransactionStatus.COMPLETED,
            timestamp = now,
            dateString = sdf.format(Date(now)),
            taxRegion = taxRegion,
            taxAmountInInr = taxCalc.taxAmount,
            e2ePayloadHash = generateHash("$refId:$totalInr:${sourceCurrency.code}")
        )

        _transactions.value = listOf(newTxn) + _transactions.value

        // Post notification
        val notif = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = "Instant Payment: ${CurrencyFormatter.formatInr(totalInr)}",
            message = "Received from $customerName via ${gateway.title}. Encrypted hash verified.",
            type = "PAYMENT",
            timestamp = "Just now",
            isUrgent = false
        )
        _notifications.value = listOf(notif) + _notifications.value

        return newTxn
    }

    // Add Recurring Schedule
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
        val amountInInr = CurrencyFormatter.convertToInr(amount, currency)
        val newBilling = RecurringBilling(
            id = "REC-" + (200 + _recurringBillings.value.size),
            title = title,
            clientOrVendorName = clientOrVendor,
            category = category,
            frequency = frequency,
            amountInInr = amountInInr,
            currency = currency,
            nextBillingDate = "15th Nov 2026",
            autoDebit = autoDebit,
            gateway = gateway,
            status = "Active"
        )
        _recurringBillings.value = listOf(newBilling) + _recurringBillings.value
    }

    // Add Invoice
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
        val subtotalInInr = CurrencyFormatter.convertToInr(subtotalAmount, currency)
        val taxCalc = TaxCalculator.calculate(subtotalInInr, taxRegion)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        val newInvoice = VendorInvoice(
            id = "INV-" + UUID.randomUUID().toString().take(6).uppercase(),
            invoiceNumber = "INV-2026-" + (100 + _invoices.value.size),
            recipient = recipient,
            recipientEmail = recipientEmail,
            issueDate = sdf.format(Date()),
            dueDate = dueDate,
            subtotalInInr = subtotalInInr,
            taxRegion = taxRegion,
            taxAmountInInr = taxCalc.taxAmount,
            totalInInr = taxCalc.totalAmount,
            currency = currency,
            status = "Pending",
            isRecurring = isRecurring,
            notes = notes
        )
        _invoices.value = listOf(newInvoice) + _invoices.value
    }

    // Update Budget Limit
    fun updateBudgetLimit(category: ExpenseCategory, newLimitInInr: Double) {
        _budgets.value = _budgets.value.map {
            if (it.category == category) it.copy(monthlyLimitInInr = newLimitInInr) else it
        }
    }

    // Mark notifications read
    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    // Export Bookkeeping CSV
    fun generateBookkeepingCsv(): String {
        val sb = StringBuilder()
        sb.append("Transaction ID,Date,Title,Customer,Category,Type,Amount (INR),Original Currency,Original Amount,Gateway,Reference ID,Status,Tax Region,Tax (INR),E2E SHA-256 Hash\n")
        _transactions.value.forEach { t ->
            sb.append("\"${t.id}\",")
            sb.append("\"${t.dateString}\",")
            sb.append("\"${t.title}\",")
            sb.append("\"${t.customerName}\",")
            sb.append("\"${t.category.displayName}\",")
            sb.append("\"${t.type.name}\",")
            sb.append("${String.format(Locale.ENGLISH, "%.2f", t.amountInInr)},")
            sb.append("\"${t.originalCurrency.code}\",")
            sb.append("${String.format(Locale.ENGLISH, "%.2f", t.originalAmount)},")
            sb.append("\"${t.gateway.title}\",")
            sb.append("\"${t.gatewayRefId}\",")
            sb.append("\"${t.status.name}\",")
            sb.append("\"${t.taxRegion.regionName}\",")
            sb.append("${String.format(Locale.ENGLISH, "%.2f", t.taxAmountInInr)},")
            sb.append("\"${t.e2ePayloadHash}\"\n")
        }
        return sb.toString()
    }

    // Export Tax Report CSV
    fun generateTaxReportCsv(): String {
        val sb = StringBuilder()
        sb.append("Tax Region,Tax Rate (%),Total Transactions,Total Volume (INR),Total Tax Collected (INR)\n")
        TaxRegion.entries.forEach { region ->
            val regionTxns = _transactions.value.filter { it.taxRegion == region }
            val count = regionTxns.size
            val volume = regionTxns.sumOf { it.amountInInr }
            val taxSum = regionTxns.sumOf { it.taxAmountInInr }
            sb.append("\"${region.regionName}\",")
            sb.append("${region.defaultRatePercent}%,")
            sb.append("$count,")
            sb.append("${String.format(Locale.ENGLISH, "%.2f", volume)},")
            sb.append("${String.format(Locale.ENGLISH, "%.2f", taxSum)}\n")
        }
        return sb.toString()
    }

    private fun generateHash(content: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(content.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        }
    }
}

data class CloudSyncInfo(
    val isSynced: Boolean,
    val lastSyncText: String,
    val pendingItems: Int
)
