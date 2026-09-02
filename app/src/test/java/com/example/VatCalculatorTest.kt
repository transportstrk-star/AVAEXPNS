package com.example

import com.example.data.model.VatCalculator
import com.example.domain.DualOcrEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VatCalculatorTest {

    @Test
    fun testUaeVatCalculationFromGross() {
        // 105 AED gross at 5% VAT => 100 AED net, 5 AED VAT
        val calc = VatCalculator.calculateFromGross(105.00, 5.0)
        assertEquals(100.00, calc.netAed, 0.01)
        assertEquals(5.00, calc.vatAed, 0.01)
        assertEquals(105.00, calc.totalAed, 0.01)
    }

    @Test
    fun testUaeVatCalculationFromNet() {
        // 200 AED net at 5% VAT => 210 AED gross, 10 AED VAT
        val calc = VatCalculator.calculateFromNet(200.00, 5.0)
        assertEquals(200.00, calc.netAed, 0.01)
        assertEquals(10.00, calc.vatAed, 0.01)
        assertEquals(210.00, calc.totalAed, 0.01)
    }

    @Test
    fun testTrnValidation() {
        val validTrn = "100249581700003"
        val result = VatCalculator.validateUaeTrn(validTrn)
        assertTrue(result.isValid)

        val invalidTrn = "12345"
        val invalidResult = VatCalculator.validateUaeTrn(invalidTrn)
        assertTrue(!invalidResult.isValid)
    }

    @Test
    fun testDualOcrLocalParser() {
        val ocrEngine = DualOcrEngine()
        val text = """
            ADNOC DISTRIBUTION PJSC
            TAX INVOICE
            TRN: 100249581700003
            TOTAL: 162.25 AED
        """.trimIndent()

        val parsed = ocrEngine.parseLocalText(text)
        assertEquals("ADNOC Distribution", parsed.supplier)
        assertEquals("100249581700003", parsed.trn)
        assertEquals(162.25, parsed.totalAed, 0.01)
    }
}
