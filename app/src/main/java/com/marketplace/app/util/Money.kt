package com.marketplace.app.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Money {

    fun formatMinor(minor: Int, currencyCode: String): String {
        val major = minor / 100.0
        return try {
            val fmt = NumberFormat.getCurrencyInstance(Locale.getDefault())
            fmt.currency = Currency.getInstance(currencyCode)
            fmt.format(major)
        } catch (_: Exception) {
            "$currencyCode %.2f".format(major)
        }
    }
}
