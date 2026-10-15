package com.example.model

import android.net.Uri

data class ScannedQrData(
    val rawText: String,
    val merchantOrRecipient: String,
    val amountInSource: Double,
    val currency: AppCurrency,
    val taxRegion: TaxRegion,
    val description: String,
    val invoiceRefId: String? = null
)

object QrCodeParser {
    fun parse(rawText: String): ScannedQrData {
        val trimmed = rawText.trim()

        // 1. UPI QR Code format: upi://pay?pa=merchant@bank&pn=MerchantName&am=1500.00&cu=INR
        if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
            try {
                val uri = Uri.parse(trimmed)
                val payeeName = uri.getQueryParameter("pn")?.replace("%20", " ") ?: "UPI Merchant"
                val amountStr = uri.getQueryParameter("am") ?: "1250"
                val amount = amountStr.toDoubleOrNull() ?: 1250.0
                val currencyCode = uri.getQueryParameter("cu") ?: "INR"
                val currency = AppCurrency.fromCode(currencyCode)
                val refId = uri.getQueryParameter("tr") ?: uri.getQueryParameter("ref")

                return ScannedQrData(
                    rawText = rawText,
                    merchantOrRecipient = payeeName,
                    amountInSource = amount,
                    currency = currency,
                    taxRegion = TaxRegion.INDIA_GST_18,
                    description = "Instant UPI QR Merchant Checkout",
                    invoiceRefId = refId
                )
            } catch (_: Exception) {}
        }

        // 2. JSON Format: {"invoiceId":"INV-89","recipient":"Nordic Tech","amount":1500,"currency":"EUR"}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            val invoiceId = extractJsonString(trimmed, "invoiceId") ?: extractJsonString(trimmed, "id")
            val recipient = extractJsonString(trimmed, "recipient") ?: extractJsonString(trimmed, "merchant") ?: "Global Merchant"
            val amount = extractJsonDouble(trimmed, "amount") ?: 2500.0
            val currCode = extractJsonString(trimmed, "currency") ?: "INR"
            val regionCode = extractJsonString(trimmed, "taxRegion") ?: "IN_18"

            return ScannedQrData(
                rawText = rawText,
                merchantOrRecipient = recipient,
                amountInSource = amount,
                currency = AppCurrency.fromCode(currCode),
                taxRegion = TaxRegion.fromCode(regionCode),
                description = "Scanned A pay Digital Invoice",
                invoiceRefId = invoiceId
            )
        }

        // 3. Fallback / Plain text or URL
        return ScannedQrData(
            rawText = rawText,
            merchantOrRecipient = if (trimmed.length > 24) trimmed.take(24) + "..." else trimmed,
            amountInSource = 3500.0,
            currency = AppCurrency.INR,
            taxRegion = TaxRegion.INDIA_GST_18,
            description = "Quick Scan & Pay Merchant Terminal",
            invoiceRefId = "SCAN-" + (1000 + (Math.random() * 8999).toInt())
        )
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonDouble(json: String, key: String): Double? {
        val pattern = "\"$key\"\\s*:\\s*([0-9.]+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toDoubleOrNull()
    }
}
