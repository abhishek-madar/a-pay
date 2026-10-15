package com.example.model

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val flag: String,
    val inrRate: Double // How many INR is 1 unit of this currency
) {
    INR("INR", "₹", "Indian Rupee", "🇮🇳", 1.0),
    USD("USD", "$", "US Dollar", "🇺🇸", 84.50),
    EUR("EUR", "€", "Euro", "🇪🇺", 91.20),
    GBP("GBP", "£", "British Pound", "🇬🇧", 108.60),
    AED("AED", "د.إ", "UAE Dirham", "🇦🇪", 23.00),
    JPY("JPY", "¥", "Japanese Yen", "🇯🇵", 0.56),
    SGD("SGD", "S$", "Singapore Dollar", "🇸🇬", 63.80),
    CAD("CAD", "C$", "Canadian Dollar", "🇨🇦", 61.30);

    companion object {
        fun fromCode(code: String): AppCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: INR
        }
    }
}

object CurrencyFormatter {
    private val inrDecimalFormat = DecimalFormat("##,##,##0.00")
    private val intInrDecimalFormat = DecimalFormat("##,##,##0")
    private val standardFormat = DecimalFormat("#,##0.00")

    fun formatInr(amountInInr: Double, includeDecimals: Boolean = true): String {
        val formatted = if (includeDecimals) inrDecimalFormat.format(amountInInr) else intInrDecimalFormat.format(amountInInr)
        return "₹$formatted"
    }

    fun format(amountInInr: Double, currency: AppCurrency, includeDecimals: Boolean = true): String {
        if (currency == AppCurrency.INR) {
            return formatInr(amountInInr, includeDecimals)
        }
        val converted = amountInInr / currency.inrRate
        val formatted = if (includeDecimals) standardFormat.format(converted) else DecimalFormat("#,##0").format(converted)
        return "${currency.symbol}$formatted"
    }

    fun convertFromInr(amountInInr: Double, targetCurrency: AppCurrency): Double {
        return amountInInr / targetCurrency.inrRate
    }

    fun convertToInr(amountInSource: Double, sourceCurrency: AppCurrency): Double {
        return amountInSource * sourceCurrency.inrRate
    }
}
