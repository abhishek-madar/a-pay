package com.example.model

enum class TaxRegion(
    val code: String,
    val regionName: String,
    val taxLabel: String,
    val defaultRatePercent: Double,
    val breakdownDescription: String
) {
    INDIA_GST_18("IN_18", "India (GST 18%)", "GST", 18.0, "CGST 9% + SGST 9% (or IGST 18%)"),
    INDIA_GST_12("IN_12", "India (GST 12%)", "GST", 12.0, "CGST 6% + SGST 6%"),
    INDIA_GST_28("IN_28", "India (GST 28%)", "GST", 28.0, "CGST 14% + SGST 14%"),
    US_CALIFORNIA("US_CA", "USA - California", "State Sales Tax", 7.25, "CA State Tax 6% + Local 1.25%"),
    US_NEW_YORK("US_NY", "USA - New York", "State Sales Tax", 8.875, "NY State 4% + NYC Local 4.875%"),
    EU_GERMANY("EU_DE", "European Union (Germany)", "VAT", 19.0, "German Standard Mehrwertsteuer 19%"),
    EU_FRANCE("EU_FR", "European Union (France)", "TVA", 20.0, "French Standard TVA 20%"),
    UK_VAT("UK_20", "United Kingdom", "VAT", 20.0, "HMRC Standard VAT 20%"),
    UAE_VAT("UAE_05", "United Arab Emirates", "VAT", 5.0, "FTA UAE Standard VAT 5%"),
    SINGAPORE_GST("SG_09", "Singapore", "GST", 9.0, "IRAS Standard GST 9%"),
    JAPAN_JCT("JP_10", "Japan", "Consumption Tax", 10.0, "National 7.8% + Local 2.2%"),
    EXEMPT("EXEMPT", "Cross-Border / Export Exempt", "Exempt", 0.0, "0% Cross-border Digital Service Exemption");

    companion object {
        fun fromCode(code: String): TaxRegion {
            return entries.firstOrNull { it.code == code } ?: INDIA_GST_18
        }
    }
}

data class TaxCalculationResult(
    val region: TaxRegion,
    val subtotal: Double,
    val taxRatePercent: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val breakdownText: String
)

object TaxCalculator {
    fun calculate(subtotal: Double, region: TaxRegion): TaxCalculationResult {
        val taxRate = region.defaultRatePercent
        val taxAmount = (subtotal * taxRate) / 100.0
        val total = subtotal + taxAmount

        val breakdown = when (region) {
            TaxRegion.INDIA_GST_18 -> {
                val half = taxAmount / 2.0
                "CGST (9%): ₹${String.format("%.2f", half)} | SGST (9%): ₹${String.format("%.2f", half)}"
            }
            TaxRegion.INDIA_GST_12 -> {
                val half = taxAmount / 2.0
                "CGST (6%): ₹${String.format("%.2f", half)} | SGST (6%): ₹${String.format("%.2f", half)}"
            }
            TaxRegion.INDIA_GST_28 -> {
                val half = taxAmount / 2.0
                "CGST (14%): ₹${String.format("%.2f", half)} | SGST (14%): ₹${String.format("%.2f", half)}"
            }
            TaxRegion.EXEMPT -> "Tax Exempt (0% Rate applied under international export rule)"
            else -> "${region.taxLabel} (${region.defaultRatePercent}%): ${region.breakdownDescription}"
        }

        return TaxCalculationResult(
            region = region,
            subtotal = subtotal,
            taxRatePercent = taxRate,
            taxAmount = taxAmount,
            totalAmount = total,
            breakdownText = breakdown
        )
    }
}
