package com.marketplace.app.util

import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun formatMinor_formatsEurWithTwoDecimals() {
        val formatted = Money.formatMinor(1999, "EUR")
        assertTrue(formatted.contains("19.99") || formatted.contains("19,99"))
    }

    @Test
    fun formatMinor_fallsBackForUnknownCurrency() {
        val formatted = Money.formatMinor(500, "NOTAVALID")
        assertTrue(formatted.startsWith("NOTAVALID"))
        assertTrue(formatted.contains("5.00"))
    }
}
