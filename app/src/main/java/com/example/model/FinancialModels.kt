package com.example.model

enum class PaymentGateway(
    val title: String,
    val iconKey: String,
    val feePercent: Double,
    val primaryColorHex: Long
) {
    STRIPE("Stripe Connect", "stripe", 2.9, 0xFF635BFF),
    PAYPAL("PayPal Express", "paypal", 3.4, 0xFF003087),
    RAZORPAY("Razorpay International", "razorpay", 2.0, 0xFF0C2340),
    BANK_WIRE("Direct Bank Wire / SWIFT", "bank", 0.5, 0xFF10B981),
    UPI_INSTANT("Instant UPI / RuPay", "upi", 0.0, 0xFF00897B);

    companion object {
        fun fromKey(key: String): PaymentGateway {
            return entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: STRIPE
        }
    }
}

enum class TransactionStatus(val label: String, val colorHex: Long) {
    COMPLETED("Completed", 0xFF10B981),
    PENDING("Processing", 0xFFF59E0B),
    FAILED("Failed", 0xFFEF4444),
    REFUNDED("Refunded", 0xFF8B5CF6)
}

enum class TransactionType {
    INCOMING,
    OUTGOING
}

enum class ExpenseCategory(val displayName: String, val iconName: String, val colorHex: Long) {
    SOFTWARE_CLOUD("Software & Cloud", "Cloud", 0xFF3B82F6),
    VENDOR_PAYMENT("Vendor Invoices", "Receipt", 0xFF8B5CF6),
    TAX_DISBURSEMENT("Regional Taxes", "AccountBalance", 0xFFEC4899),
    SALARIES_CONTRACTORS("Payroll & Team", "People", 0xFF10B981),
    OFFICE_OPERATIONS("Operations & Logistics", "Business", 0xFFF59E0B),
    MARKETING_ADS("Global Marketing", "Campaign", 0xFF06B6D4),
    PAYMENT_FEES("Gateway Processing Fees", "Payment", 0xFF64748B)
}

data class Transaction(
    val id: String,
    val title: String,
    val customerName: String,
    val customerEmail: String,
    val category: ExpenseCategory,
    val type: TransactionType,
    val amountInInr: Double,
    val originalCurrency: AppCurrency,
    val originalAmount: Double,
    val gateway: PaymentGateway,
    val gatewayRefId: String,
    val status: TransactionStatus,
    val timestamp: Long,
    val dateString: String,
    val taxRegion: TaxRegion,
    val taxAmountInInr: Double,
    val e2ePayloadHash: String,
    val isSyncedToCloud: Boolean = true
)

data class RecurringBilling(
    val id: String,
    val title: String,
    val clientOrVendorName: String,
    val category: ExpenseCategory,
    val frequency: String, // "Weekly", "Monthly", "Quarterly", "Annually"
    val amountInInr: Double,
    val currency: AppCurrency,
    val nextBillingDate: String,
    val autoDebit: Boolean,
    val gateway: PaymentGateway,
    val status: String // "Active", "Paused", "Attention"
)

data class VendorInvoice(
    val id: String,
    val invoiceNumber: String,
    val recipient: String,
    val recipientEmail: String,
    val issueDate: String,
    val dueDate: String,
    val subtotalInInr: Double,
    val taxRegion: TaxRegion,
    val taxAmountInInr: Double,
    val totalInInr: Double,
    val currency: AppCurrency,
    val status: String, // "Paid", "Pending", "Overdue"
    val isRecurring: Boolean = false,
    val notes: String = ""
)

data class BudgetCategory(
    val category: ExpenseCategory,
    val monthlyLimitInInr: Double,
    val currentSpentInInr: Double
) {
    val percentage: Float get() = if (monthlyLimitInInr > 0) ((currentSpentInInr / monthlyLimitInInr).toFloat()).coerceIn(0f, 1.5f) else 0f
    val isAlertTriggered: Boolean get() = percentage >= 0.80f
    val isExceeded: Boolean get() = percentage >= 1.0f
}

data class ConnectedBankAccount(
    val id: String,
    val bankName: String,
    val accountType: String,
    val maskedNumber: String,
    val balanceInInr: Double,
    val currency: AppCurrency = AppCurrency.INR,
    val isAutoSyncEnabled: Boolean = true,
    val lastSyncTime: String = "Live (2 min ago)"
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "PAYMENT", "DEADLINE", "BUDGET", "SECURITY"
    val timestamp: String,
    val isUrgent: Boolean = false,
    val isRead: Boolean = false
)
