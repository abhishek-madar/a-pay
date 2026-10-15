package com.example.model

enum class UserRole(
    val title: String,
    val description: String,
    val badgeColorHex: Long,
    val permissions: List<String>
) {
    ADMIN(
        "Administrator",
        "Full root control over transactions, keys, team roles, and settings",
        0xFF10B981,
        listOf("Process Payments", "Manage Recurring", "Create Invoices", "Export CSV & PDF", "Gateway API Keys", "Team Roles", "Tax Slabs")
    ),
    FINANCE_MANAGER(
        "Finance Manager",
        "Authorizes transactions, manages billing cycles and regional tax settings",
        0xFF3B82F6,
        listOf("Process Payments", "Manage Recurring", "Create Invoices", "Export CSV & PDF", "Tax Slabs")
    ),
    AUDITOR(
        "Compliance Auditor",
        "Read-only cryptographic verification of financial ledgers and export",
        0xFF8B5CF6,
        listOf("View Transactions", "Verify E2E Hashes", "Audit Logs", "Export CSV & PDF")
    ),
    CASHIER(
        "Billing Cashier",
        "Front-desk point-of-sale checkout and invoice generation",
        0xFFF59E0B,
        listOf("Process Payments", "Generate Receipt", "View Recent Activity")
    );

    fun hasPermission(permission: String): Boolean = permissions.contains(permission)
}

data class EncryptionStatus(
    val cipher: String = "AES-256-GCM (Hardware Backed)",
    val keyDerivation: String = "PBKDF2-HMAC-SHA512 (100,000 rounds)",
    val keyExchange: String = "ECDH Curve25519",
    val integrityCheck: String = "HMAC-SHA256 Payload Signature",
    val tlsVersion: String = "TLS 1.3 Strict Forward Secrecy",
    val pciDssLevel: String = "PCI-DSS Level 1 Validated",
    val keyFingerprint: String = "4F:9A:82:1C:7E:5B:3D:02:88:AC:6E:91"
)

data class TwoFactorState(
    val isEnabled: Boolean = true,
    val method: String = "Time-based OTP (RFC 6238)",
    val issuer: String = "A pay Authenticator",
    val remainingSeconds: Int = 24,
    val currentTotpCode: String = "839 214"
)

data class BiometricState(
    val isAvailable: Boolean = true,
    val isBiometricEnabled: Boolean = true,
    val isUnlocked: Boolean = true,
    val lastAuthenticated: String = "Just now (Biometrics verified)"
)
