package com.example.data.model

import java.math.BigDecimal
import java.math.RoundingMode

object VatCalculator {
    const val UAE_DEFAULT_VAT_RATE = 5.0

    /**
     * Given Gross/Total (inclusive of VAT), calculates Net and VAT amount.
     * Net = Total / (1 + rate/100)
     * VAT = Total - Net
     */
    fun calculateFromGross(grossAmount: Double, vatRate: Double = UAE_DEFAULT_VAT_RATE): VatBreakdown {
        if (grossAmount <= 0.0) return VatBreakdown(0.0, 0.0, 0.0, vatRate)
        val gross = BigDecimal(grossAmount.toString())
        val divisor = BigDecimal.ONE.add(BigDecimal(vatRate.toString()).divide(BigDecimal("100"), 6, RoundingMode.HALF_UP))
        
        val net = gross.divide(divisor, 2, RoundingMode.HALF_UP)
        val vat = gross.subtract(net).setScale(2, RoundingMode.HALF_UP)

        return VatBreakdown(
            totalAed = gross.setScale(2, RoundingMode.HALF_UP).toDouble(),
            vatAed = vat.toDouble(),
            netAed = net.toDouble(),
            vatRate = vatRate
        )
    }

    /**
     * Given Net (exclusive of VAT), calculates Total and VAT amount.
     * VAT = Net * (rate/100)
     * Total = Net + VAT
     */
    fun calculateFromNet(netAmount: Double, vatRate: Double = UAE_DEFAULT_VAT_RATE): VatBreakdown {
        if (netAmount <= 0.0) return VatBreakdown(0.0, 0.0, 0.0, vatRate)
        val net = BigDecimal(netAmount.toString())
        val multiplier = BigDecimal(vatRate.toString()).divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)
        
        val vat = net.multiply(multiplier).setScale(2, RoundingMode.HALF_UP)
        val total = net.add(vat).setScale(2, RoundingMode.HALF_UP)

        return VatBreakdown(
            totalAed = total.toDouble(),
            vatAed = vat.toDouble(),
            netAed = net.setScale(2, RoundingMode.HALF_UP).toDouble(),
            vatRate = vatRate
        )
    }

    /**
     * Validates UAE Tax Registration Number (TRN):
     * Should be exactly 15 digits, starting with 100.
     */
    fun validateUaeTrn(trn: String): TrnValidationResult {
        val clean = trn.replace(Regex("[^0-9]"), "")
        return when {
            clean.isEmpty() -> TrnValidationResult.EMPTY
            clean.length != 15 -> TrnValidationResult.INVALID_LENGTH
            !clean.startsWith("100") -> TrnValidationResult.INVALID_PREFIX
            else -> TrnValidationResult.VALID
        }
    }
}

data class VatBreakdown(
    val totalAed: Double,
    val vatAed: Double,
    val netAed: Double,
    val vatRate: Double
)

enum class TrnValidationResult(val message: String, val isValid: Boolean) {
    VALID("Valid UAE 15-Digit TRN (FTA Compliant)", true),
    INVALID_LENGTH("TRN must be exactly 15 digits", false),
    INVALID_PREFIX("UAE TRNs typically start with 100", false),
    EMPTY("TRN is missing", false)
}
