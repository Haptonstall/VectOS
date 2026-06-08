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
    fun applyToNominal(nominal: Double, factor: DesignFactor): Double =
        when (methodology) {
            DesignMethodology.LRFD -> nominal * factor.value
            DesignMethodology.ASD  -> nominal / factor.value
        }
}