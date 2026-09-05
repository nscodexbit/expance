package com.yourname.expensetracker.util

import java.util.Locale

object CurrencyHelper {

    data class CurrencyInfo(
        val code: String,
        val symbol: String,
        val name: String,
        val rateFromInr: Double
    )

    val ALL_CURRENCIES = listOf(
        CurrencyInfo("INR", "₹", "Indian Rupee (INR - ₹)", 1.0),
        CurrencyInfo("USD", "$", "US Dollar (USD - $)", 0.012),
        CurrencyInfo("EUR", "€", "Euro (EUR - €)", 0.011),
        CurrencyInfo("GBP", "£", "British Pound (GBP - £)", 0.0094),
        CurrencyInfo("JPY", "¥", "Japanese Yen (JPY - ¥)", 1.82),
        CurrencyInfo("AUD", "A$", "Australian Dollar (AUD - A$)", 0.018),
        CurrencyInfo("CAD", "C$", "Canadian Dollar (CAD - C$)", 0.016),
        CurrencyInfo("AED", "AED", "UAE Dirham (AED)", 0.044)
    )

    fun getCurrencyInfo(symbolOrCode: String): CurrencyInfo {
        return ALL_CURRENCIES.firstOrNull { 
            it.symbol == symbolOrCode || it.code.equals(symbolOrCode, ignoreCase = true) 
        } ?: ALL_CURRENCIES[0]
    }

    fun getRate(symbolOrCode: String): Double {
        return getCurrencyInfo(symbolOrCode).rateFromInr
    }

    fun getSymbol(symbolOrCode: String): String {
        return getCurrencyInfo(symbolOrCode).symbol
    }

    fun convert(baseAmount: Double, symbolOrCode: String): Double {
        return baseAmount * getRate(symbolOrCode)
    }

    fun format(baseAmount: Double, symbolOrCode: String): String {
        val info = getCurrencyInfo(symbolOrCode)
        val converted = baseAmount * info.rateFromInr
        val formattedNum = if (info.symbol == "¥") {
            String.format(Locale.US, "%,.0f", converted)
        } else {
            String.format(Locale.US, "%,.2f", converted)
        }
        return if (info.symbol == "AED") "${info.symbol} $formattedNum" else "${info.symbol}$formattedNum"
    }
}
