package com.lz.model.regulatory.nds

import kotlinx.serialization.Serializable

/**
 * Represents the specific major editions of the NDS
 * (National Design Specification for Wood Construction).
 */
@Serializable
enum class NdsEdition(val year: Int, val description: String) {
    NDS_2015(2015, "National Design Specification for Wood Construction (2015)"),
    NDS_2018(2018, "National Design Specification for Wood Construction (2018)"),
    NDS_2024(2024, "National Design Specification for Wood Construction (2024)")
}

/**
 * Wood member end-use service condition.
 * Drives the wet service factor (CM) applied to all reference design values.
 * NDS Table 4A / Table 4B footnotes.
 */
@Serializable
enum class ServiceCondition {
    DRY,    // Moisture content ≤ 19% (typical interior conditions) — CM = 1.0
    WET     // Moisture content > 19% (exposed or treated) — CM factors per NDS Table 4A
}

/**
 * Temperature range classification for structural wood members.
 * Drives the temperature factor (Ct).
 * NDS Section 2.3.3, Table 2.3.3.
 */
@Serializable
enum class TemperatureRange {
    T_100_OR_LESS,      // T ≤ 100°F — Ct = 1.0 for all properties
    T_101_TO_125,       // 100°F < T ≤ 125°F — Ct varies by property
    T_126_TO_150        // 125°F < T ≤ 150°F — Ct further reduced
}

/**
 * Load duration classification per NDS Section 2.3.2.
 * The load duration factor (CD) is one of the most significant adjustments
 * in NDS ASD design. It reflects the time-dependent nature of wood strength.
 *
 * Note: CD does NOT apply to modulus of elasticity (E, Emin),
 * compression perpendicular to grain (Fc⊥), or bearing area factor (Cb).
 */
@Serializable
enum class LoadDurationClass(
    val cd: Double,
    val description: String,
    val typicalLoadType: String
) {
    PERMANENT(         0.90, "Permanent",          "Dead load"),
    TEN_YEARS(         1.00, "Ten years",           "Occupancy live load"),
    TWO_MONTHS(        1.15, "Two months",          "Snow load"),
    SEVEN_DAYS(        1.25, "Seven days",          "Construction load"),
    TEN_MINUTES(       1.60, "Ten minutes",         "Wind / seismic load"),
    IMPACT(            2.00, "Impact",              "Impact load")
}

/**
 * Lumber size classification used to determine applicable adjustment factor tables.
 * NDS Supplement Table reference depends on whether sawn lumber is dimension,
 * beam/stringer, post/timber, or decking.
 */
@Serializable
enum class LumberSizeClass {
    DIMENSION_LUMBER,   // 2-4 in. thick — NDS Supplement Table 4A / 4B
    BEAMS_AND_STRINGERS, // 5 in.+ thick, width > thickness + 2 in. — NDS Supplement Table 4D
    POSTS_AND_TIMBERS,  // 5 in.+ thick, width ≤ thickness + 2 in. — NDS Supplement Table 4D
    DECKING             // 2-4 in. thick, loaded on wide face — NDS Supplement Table 4E
}

/**
 * Wet service factors (CM) for sawn lumber per NDS Supplement Table 4A.
 * Applied when moisture content exceeds 19%.
 * Note: CM = 1.0 for dry service — these factors only deviate from 1.0 for wet conditions.
 */
@Serializable
data class WetServiceFactors(
    val cmBending: Double        = 0.85,  // F'b adjustment
    val cmTension: Double        = 1.00,  // F't adjustment
    val cmShear: Double          = 0.97,  // F'v adjustment
    val cmCompPerp: Double       = 0.67,  // F'c⊥ adjustment
    val cmCompParallel: Double   = 0.80,  // F'c adjustment
    val cmModulus: Double        = 0.90,  // E adjustment
    val cmModulusMin: Double     = 0.90   // Emin adjustment
)

/**
 * Temperature factors (Ct) per NDS Table 2.3.3.
 * Applied when sustained temperatures exceed 100°F.
 */
@Serializable
data class TemperatureFactors(
    val ctBending: Double,          // Ct for Fb, Ft, Fv, Fc
    val ctCompPerp: Double,         // Ct for Fc⊥
    val ctModulus: Double           // Ct for E, Emin
)

/**
 * Edition-specific specification constants for NDS.
 * Captures deflection limits, adjustment factor defaults, and
 * any edition-specific rule changes.
 */
@Serializable
data class NdsSpecificationConstants(
    val edition: NdsEdition,

    // Deflection limits — NDS Section 3.5 / IBC Table 1604.3
    val liveLoadDeflectionLimitSpanRatio: Int = 360,    // L/360
    val totalLoadDeflectionLimitSpanRatio: Int = 240,   // L/240
    val roofLiveLoadDeflectionSpanRatio: Int = 180,     // L/180 (roof members)

    // Repetitive member factor — NDS Section 4.3.9
    // Applies when 3+ members are spaced ≤ 24 in. o.c. and joined by load-distributing elements
    val crRepetitiveMember: Double = 1.15,

    // Flat use factor (Cfu) for dimension lumber used flatwise
    // NDS Supplement Table 4A footnote — applied to Fb only
    val cfuFlatUse: Boolean = true,

    // Format conversion factor (KF) for LRFD — NDS Appendix N
    // Introduced formally in NDS 2015; values consistent across editions
    val kfBending: Double      = 2.54,  // KF for Fb
    val kfTension: Double      = 2.70,  // KF for Ft
    val kfShear: Double        = 2.88,  // KF for Fv
    val kfCompParallel: Double = 2.40,  // KF for Fc
    val kfCompPerp: Double     = 1.67,  // KF for Fc⊥
    val kfModulusMin: Double   = 1.76,  // KF for Emin

    // Resistance factors (phi) for LRFD — NDS Appendix N Table N1
    val phiBending: Double      = 0.85,
    val phiTension: Double      = 0.80,
    val phiShear: Double        = 0.75,
    val phiCompression: Double  = 0.90,
    val phiStability: Double    = 0.85  // Used with Emin for stability calculations
)

/**
 * Registry to resolve edition-specific NDS constants, wet service factors,
 * temperature factors, and load duration factors.
 *
 * Follows the same provider pattern as Asce7VersionRegistry.
 */
object NdsVersionRegistry {

    /**
     * Resolves edition-specific specification constants.
     *
     * NDS 2015 — introduced LRFD format conversion factors (Appendix N)
     *            and updated beam stability provisions.
     * NDS 2018 — clarified cross-laminated timber (CLT) provisions,
     *            updated connection provisions in Chapter 10-12.
     * NDS 2024 — updated species-specific reference design values in
     *            the NDS Supplement; expanded mass timber provisions.
     */
    fun getConstantsFor(edition: NdsEdition): NdsSpecificationConstants {
        return when (edition) {
            NdsEdition.NDS_2015 -> NdsSpecificationConstants(
                edition = NdsEdition.NDS_2015
                // LRFD format conversion factors (KF) formally introduced this edition.
                // All KF and phi values above reflect NDS 2015 Appendix N Table N1.
            )
            NdsEdition.NDS_2018 -> NdsSpecificationConstants(
                edition = NdsEdition.NDS_2018
                // Connection provisions updated in Chapters 10-12.
                // KF and phi values unchanged from NDS 2015.
                // CLT provisions expanded — mass timber chapter added.
            )
            NdsEdition.NDS_2024 -> NdsSpecificationConstants(
                edition = NdsEdition.NDS_2024
                // NDS Supplement reference design values updated for several species.
                // Mass timber provisions further expanded.
                // KF and phi values unchanged from prior editions.
            )
        }
    }

    /**
     * Resolves wet service factors (CM) for the given service condition.
     * Returns 1.0 factors for dry service — no adjustment needed.
     * Returns NDS Supplement Table 4A factors for wet service.
     *
     * Note: These factors are consistent across all three supported editions.
     */
    fun getWetServiceFactors(serviceCondition: ServiceCondition): WetServiceFactors {
        return when (serviceCondition) {
            ServiceCondition.DRY -> WetServiceFactors(
                cmBending      = 1.00,
                cmTension      = 1.00,
                cmShear        = 1.00,
                cmCompPerp     = 1.00,
                cmCompParallel = 1.00,
                cmModulus      = 1.00,
                cmModulusMin   = 1.00
            )
            ServiceCondition.WET -> WetServiceFactors(
                // NDS Supplement Table 4A — Sawn Lumber wet service factors
                cmBending      = 0.85,
                cmTension      = 1.00,
                cmShear        = 0.97,
                cmCompPerp     = 0.67,
                cmCompParallel = 0.80,
                cmModulus      = 0.90,
                cmModulusMin   = 0.90
            )
        }
    }

    /**
     * Resolves temperature factors (Ct) per NDS Table 2.3.3.
     * Consistent across all supported editions.
     */
    fun getTemperatureFactors(temperatureRange: TemperatureRange): TemperatureFactors {
        return when (temperatureRange) {
            TemperatureRange.T_100_OR_LESS -> TemperatureFactors(
                ctBending  = 1.00,
                ctCompPerp = 1.00,
                ctModulus  = 1.00
            )
            TemperatureRange.T_101_TO_125 -> TemperatureFactors(
                // NDS Table 2.3.3 — wet service conditions reduce Ct further,
                // but these values apply to dry service at elevated temperature
                ctBending  = 0.80,
                ctCompPerp = 0.67,
                ctModulus  = 0.90
            )
            TemperatureRange.T_126_TO_150 -> TemperatureFactors(
                ctBending  = 0.70,
                ctCompPerp = 0.50,
                ctModulus  = 0.80
            )
        }
    }

    /**
     * Returns the load duration factor (CD) for a given load duration class.
     * CD is edition-invariant across all supported NDS editions.
     *
     * Note: CD applies to Fb, Ft, Fv, Fc only.
     * CD does NOT apply to: E, Emin, Fc⊥, Cb.
     */
    fun getLoadDurationFactor(loadDurationClass: LoadDurationClass): Double {
        return loadDurationClass.cd
    }

    /**
     * Resolves the governing load duration factor when multiple load types
     * act simultaneously. NDS Section 2.3.2 permits using the shortest
     * duration load in the combination.
     *
     * Example: Dead + Snow → CD = 1.15 (snow governs over permanent)
     */
    fun getGoverningLoadDurationFactor(
        loadDurationClasses: List<LoadDurationClass>
    ): Double {
        return loadDurationClasses.maxOfOrNull { it.cd } ?: LoadDurationClass.PERMANENT.cd
    }
}
