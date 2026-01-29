package com.muatrenthenang.resfood.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyHelper {
    /**
     * Formats the given amount into a currency string based on the current locale.
     *
     * Rules:
     * - Vietnamese (vi): 10.000.000đ (dot as thousands separator)
     * - English/Others: 10,000,000 VND (comma as thousands separator)
     */
    val currencySymbol: String
        get() = if (Locale.getDefault().language == "vi") "đ" else "VND"

    fun format(amount: Double): String {
        val locale = Locale.getDefault()
        return if (locale.language == "vi") {
            // Vietnamese: use dot for thousands, comma for decimals (if any, though usually integer for VND)
            // NumberFormat.getNumberInstance for vi_VN handles the dot/comma correctly.
            val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            "${numberFormat.format(amount)}$currencySymbol"
        } else {
            // English/Other: use comma for thousands, dot for decimals
            // Using US locale as standard for "international" formatting with comma separators
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)
            "${numberFormat.format(amount)} $currencySymbol"
        }
    }

    // Overload for Int
    fun format(amount: Int): String {
        return format(amount.toDouble())
    }
    
    // Overload for Long
    fun format(amount: Long): String {
        return format(amount.toDouble())
    }
}

// Extension functions for convenience
fun Double.toCurrency(): String = CurrencyHelper.format(this)
fun Int.toCurrency(): String = CurrencyHelper.format(this)
fun Long.toCurrency(): String = CurrencyHelper.format(this)
