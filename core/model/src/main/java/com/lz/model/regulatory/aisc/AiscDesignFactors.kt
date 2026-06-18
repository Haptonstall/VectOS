package com.lz.model.regulatory.aisc

import com.lz.model.regulatory.AiscEdition
import com.lz.model.structural.DesignMethodology
import kotlinx.serialization.Serializable

/**
 * A single design factor with its authoritative code citation.
 * The citation string is used directly in calculation reports.
 */
@Serializable
data class DesignFactor(
    val value: Double,
    val citation: String
)

/**
 * Complete set of AISC 360 resistance (φ) and safety (Ω) factors
 * for a specific edition and methodology.
 *
 * Use [applyToNominal] to apply the correct factor for the methodology
 * rather than branching on LRFD vs ASD at the call site.
 */
@Serializable
data class AiscDesignFactors(
    val edition: AiscEdition,
    val methodology: DesignMethodology,
    val flexure: DesignFactor,
    val shear: DesignFactor,
    val compression: DesignFactor,
    val tensionYield: DesignFactor,
    val tensionRupture: DesignFactor,
    val torsion: DesignFactor
) {
    /**
     * Applies the correct phi (LRFD) or omega (ASD) factor to a nominal capacity.
     * LRFD: design = phi * nominal
     * ASD:  design = nominal / omega
     */
    fun applyToNominal(nominal: Double, factor: DesignFactor): Double =
        when (methodology) {
            DesignMethodology.LRFD -> nominal * factor.value
            DesignMethodology.ASD  -> nominal / factor.value
        }

    companion object {
        /**
         * Convenience accessor when edition is known.
         * Delegates to [AiscDesignFactorRegistry].
         */
        fun forEditionAndMethodology(
            edition: AiscEdition,
            methodology: DesignMethodology
        ): AiscDesignFactors = AiscDesignFactorRegistry.get(edition, methodology)

        /**
         * Convenience accessor defaulting to AISC 360-22 when no edition is specified.
         * Used by the capacity calculator when the material grade does not carry
         * an explicit code edition.
         */
        fun forMethodology(methodology: DesignMethodology): AiscDesignFactors =
            AiscDesignFactorRegistry.get(AiscEdition.AISC_360_22, methodology)
    }
}