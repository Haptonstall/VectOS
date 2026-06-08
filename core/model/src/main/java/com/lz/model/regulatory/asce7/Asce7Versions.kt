package com.lz.model.regulatory.asce7

import kotlinx.serialization.Serializable

/**
 * Represents the specific major editions of the ASCE 7 Standard.
 */
@Serializable
enum class Asce7Edition(val year: Int, val description: String) {
    ASCE_7_05(2005, "Minimum Design Loads for Buildings and Other Structures (7-05)"),
    ASCE_7_10(2010, "Minimum Design Loads for Buildings and Other Structures (7-10)"),
    ASCE_7_16(2016, "Minimum Design Loads and Hazards for Buildings and Other Structures (7-16)"),
    ASCE_7_22(2022, "Minimum Design Loads and Hazards for Buildings and Other Structures (7-22)")
}

/**
 * Risk Category determined by the occupancy and use of the structure,
 * per ASCE 7 Chapter 1 / IBC Chapter 16.
 */
@Serializable
enum class RiskCategory(val romanNumeral: String) {
    I("I"),     // Low hazard to human life (e.g., agricultural storage, minor temporary facilities)
    II("II"),   // Standard occupancy (e.g., typical residential, commercial, industrial structures)
    III("III"), // Substantial hazard to human life (e.g., schools, theaters, assembly spaces > 300)
    IV("IV")    // Essential facilities (e.g., hospitals, fire stations, emergency shelters)
}

/**
 * Encapsulates the core Seismic Design Criteria constants required for lateral force
 * calculations (Equivalent Lateral Force procedure under Chapters 11 & 12).
 */
@Serializable
data class SeismicDesignParameters(
    val ss: Double,                  // Short-period MCE ground motion parameter (0.2s)
    val s1: Double,                  // 1-second period MCE ground motion parameter
    val sds: Double,                 // Design spectral response acceleration parameter at short periods
    val sd1: Double,                 // Design spectral response acceleration parameter at 1-s period
    val seismicDesignCategory: String, // Calculated SDC (A through F)
    val importanceFactorIe: Double   // Seismic Importance Factor based on Risk Category
)

/**
 * Holds code-specific coefficients and combination guidelines unique to a specific ASCE 7 edition.
 */
@Serializable
data class Asce7SpecificationConstants(
    val edition: Asce7Edition,

    // Standard basic deflection limits (ASCE 7 Chapter 12 / IBC Chapter 16)
    val liveLoadDeflectionLimitSpanRatio: Int = 360, // L/360
    val totalLoadDeflectionLimitSpanRatio: Int = 240, // L/240

    // Wind Directionality Factors (Kd) - ASCE 7 Chapter 26
    val kdMainWindForceResistingSystem: Double = 0.85,
    val kdComponentsAndCladding: Double = 0.85
)

/**
 * A provider registry to map out risk factor modifiers and structural rulesets
 * across different code generations.
 */
object Asce7VersionRegistry {

    /**
     * Retrieves the Importance Factors for a given Risk Category.
     * Note: ASCE 7-22 shifts away from traditional importance factors for wind in many
     * configurations by using hazard-specific return periods directly in the maps,
     * but seismic still relies heavily on Ie.
     */
    fun getSeismicImportanceFactor(edition: Asce7Edition, riskCategory: RiskCategory): Double {
        return when (riskCategory) {
            RiskCategory.I   -> 1.00
            RiskCategory.II  -> 1.00
            RiskCategory.III -> 1.25
            RiskCategory.IV  -> 1.50
        }
    }

    /**
     * Resolves the version-specific system constraints.
     */
    fun getConstantsFor(edition: Asce7Edition): Asce7SpecificationConstants {
        return when (edition) {
            Asce7Edition.ASCE_7_05 -> Asce7SpecificationConstants(
                edition = Asce7Edition.ASCE_7_05
            )
            Asce7Edition.ASCE_7_10 -> Asce7SpecificationConstants(
                edition = Asce7Edition.ASCE_7_10
            )
            Asce7Edition.ASCE_7_16 -> Asce7SpecificationConstants(
                edition = Asce7Edition.ASCE_7_16
            )
            Asce7Edition.ASCE_7_22 -> Asce7SpecificationConstants(
                edition = Asce7Edition.ASCE_7_22
            )
        }
    }
}