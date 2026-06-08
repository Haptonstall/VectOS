package com.lz.model.regulatory.aci318

import kotlinx.serialization.Serializable

/**
 * Represents the specific major editions of ACI 318
 * (Building Code Requirements for Structural Concrete).
 */
@Serializable
enum class Aci318Edition(val year: Int, val description: String) {
    ACI_318_14(2014, "Building Code Requirements for Structural Concrete (ACI 318-14)"),
    ACI_318_19(2019, "Building Code Requirements for Structural Concrete (ACI 318-19)"),
    ACI_318_25(2025, "Building Code Requirements for Structural Concrete (ACI 318-25)")
}

/**
 * Concrete exposure categories per ACI 318 Table 19.3.1.
 * Determines minimum f'c, maximum w/cm ratio, and cover requirements.
 *
 * F = Freezing and thawing
 * S = Sulfate exposure
 * W = In contact with water
 * C = Corrosion protection of reinforcement
 */
@Serializable
enum class ExposureCategory {
    F0,     // Not exposed to freezing/thawing
    F1,     // Moderate exposure to freezing/thawing
    F2,     // Severe exposure to freezing/thawing
    F3,     // Very severe — deicing chemicals
    S0,     // Negligible sulfate exposure
    S1,     // Moderate sulfate exposure
    S2,     // Severe sulfate exposure
    S3,     // Very severe sulfate exposure
    W0,     // Concrete not in contact with water
    W1,     // In contact with water, low permeability not required
    W2,     // In contact with water, low permeability required
    C0,     // Low corrosion risk
    C1,     // Moderate corrosion risk
    C2      // High corrosion risk — chloride exposure
}

/**
 * Reinforcement type classification.
 * Drives applicable strength reduction factors for tension-controlled members
 * and development length calculations.
 * ACI 318-19 Section 20.2.1.
 */
@Serializable
enum class ReinforcementType {
    GRADE_40,       // fy = 40,000 psi — legacy, limited modern use
    GRADE_60,       // fy = 60,000 psi — most common US reinforcement
    GRADE_80,       // fy = 80,000 psi — high-strength, ACI 318-19+ provisions
    GRADE_100,      // fy = 100,000 psi — introduced ACI 318-19 Section 26.4
    STAINLESS,      // Corrosion-resistant — ACI 318-14 Section 20.2.2
    EPOXY_COATED    // ACI 318 Section 25.8.1 — development length modification
}

/**
 * Lightweight concrete classification per ACI 318 Section 19.2.4.
 * Drives the lambda (λ) modification factor applied to tensile strength
 * calculations including shear and development lengths.
 */
@Serializable
enum class LightweightConcreteClass(
    val lambda: Double,
    val description: String
) {
    NORMAL_WEIGHT(  1.00, "Normal-weight concrete — no λ reduction"),
    SAND_LIGHTWEIGHT(0.85, "Sand-lightweight concrete — ACI 318 Table 19.2.4.2"),
    ALL_LIGHTWEIGHT( 0.75, "All-lightweight concrete — ACI 318 Table 19.2.4.2")
}

/**
 * Strength reduction factors (φ) per ACI 318.
 * Values are organized by action type and tension/compression control classification.
 *
 * ACI 318-14: Table 21.2.1
 * ACI 318-19: Table 21.2.1 (added high-strength steel provisions)
 * ACI 318-25: Table 21.2.1 (updated seismic provisions)
 */
@Serializable
data class Aci318StrengthReductionFactors(
    val edition: Aci318Edition,

    // Flexure and axial — ACI 318 Section 21.2.1
    val phiTensionControlled: Double    = 0.90,  // Net tensile strain ≥ 0.005
    val phiCompressionControlled: Double = 0.65, // Spiral ties — net tensile strain ≤ εy
    val phiCompressionTiedColumn: Double = 0.65, // Tied columns

    // Shear and torsion — ACI 318 Section 21.2.1(c)
    val phiShear: Double                = 0.75,

    // Bearing — ACI 318 Section 21.2.1(e)
    val phiBearing: Double              = 0.65,

    // Post-installed anchors — ACI 318 Chapter 17
    val phiAnchorTension: Double        = 0.75,  // Ductile steel element
    val phiAnchorShear: Double          = 0.65,  // Concrete controlled

    // Strut-and-tie models — ACI 318 Section 21.2.1(h)
    val phiStrutAndTie: Double          = 0.75
)

/**
 * Minimum concrete compressive strength requirements by exposure category.
 * ACI 318 Table 19.3.3.1.
 * Values in psi.
 */
@Serializable
data class ConcreteStrengthRequirements(
    val minFcPsi: Double,
    val maxWaterCementRatio: Double?,   // null = not specified for this category
    val notes: String
)

/**
 * Edition-specific specification constants for ACI 318.
 * Covers concrete cover, development length multipliers, and
 * edition-specific rule changes.
 */
@Serializable
data class Aci318SpecificationConstants(
    val edition: Aci318Edition,

    // Concrete cover — ACI 318 Table 20.6.1.3 (cast-in-place, non-prestressed)
    val coverBarsSmallerThan6InchConcreteCastInForms: Double = 1.50,   // in.
    val coverBarsLargerThan5InchConcreteAgainstGround: Double = 3.00,  // in.
    val coverConcreteExposedToWeather: Double = 2.00,                  // in.

    // Modulus of elasticity — ACI 318 Section 19.2.2
    // Ec = 33 * wc^1.5 * sqrt(f'c) for normal weight concrete
    val ecMultiplierNormalWeight: Double = 57000.0,  // Ec = 57,000 * sqrt(f'c) simplified

    // Minimum reinforcement ratio for flexural members
    // ACI 318 Section 9.6.1.2
    val rhoMinFlexureFactor1: Double = 3.0,   // 3*sqrt(f'c) / fy
    val rhoMinFlexureFactor2: Double = 200.0, // 200 / fy (governs for low f'c)

    // Maximum reinforcement ratio for columns
    // ACI 318 Section 10.6.1.1
    val rhoMaxColumn: Double = 0.08,   // 8% gross area
    val rhoMinColumn: Double = 0.01,   // 1% gross area

    // Shear provisions — ACI 318 Section 22.5
    val vcMultiplier: Double = 2.0,    // Vc = 2*lambda*sqrt(f'c)*bw*d (simplified)

    // Development length — ACI 318 Section 25.5
    // ld = (3/40) * (fy / lambda*sqrt(f'c)) * (ψt*ψe*ψs*ψg / (cb+Ktr)/db) * db
    val ldPsiTTopBar: Double      = 1.30,  // Top bar factor (>12 in. fresh concrete below)
    val ldPsiTOtherBar: Double    = 1.00,
    val ldPsiEEpoxyCoated: Double = 1.50,  // Epoxy-coated bar factor
    val ldPsiEUncoated: Double    = 1.00,

    // Lap splice length multiplier — ACI 318 Section 25.5.2
    val lapSpliceClass1Multiplier: Double = 1.00,  // Class A
    val lapSpliceClass2Multiplier: Double = 1.30   // Class B
)

/**
 * Registry to resolve edition-specific ACI 318 constants, strength reduction factors,
 * and concrete strength requirements.
 *
 * Follows the same provider pattern as Asce7VersionRegistry and NdsVersionRegistry.
 */
object Aci318VersionRegistry {

    /**
     * Resolves edition-specific specification constants.
     *
     * ACI 318-14 — unified format reorganization (chapters renumbered from -11).
     *              Introduced seismic provisions update for special moment frames.
     * ACI 318-19 — added high-strength reinforcement provisions (Grade 80/100).
     *              Updated anchor design provisions (Chapter 17).
     *              Expanded strut-and-tie model provisions.
     * ACI 318-25 — updated seismic detailing provisions.
     *              Expanded mass concrete provisions.
     *              Revised durability requirements for exposure categories.
     */
    fun getConstantsFor(edition: Aci318Edition): Aci318SpecificationConstants {
        return when (edition) {
            Aci318Edition.ACI_318_14 -> Aci318SpecificationConstants(
                edition = Aci318Edition.ACI_318_14
                // Baseline constants reflect ACI 318-14 provisions.
                // Chapter numbering reorganized from ACI 318-11.
            )
            Aci318Edition.ACI_318_19 -> Aci318SpecificationConstants(
                edition = Aci318Edition.ACI_318_19
                // High-strength reinforcement (Grade 80/100) provisions added.
                // Anchor design Chapter 17 substantially updated.
                // Constants otherwise consistent with ACI 318-14.
            )
            Aci318Edition.ACI_318_25 -> Aci318SpecificationConstants(
                edition = Aci318Edition.ACI_318_25
                // Seismic detailing updated for ASCE 7-22 alignment.
                // Durability requirements revised for exposure categories F and S.
                // Cover and development length constants unchanged from ACI 318-19.
            )
        }
    }

    /**
     * Resolves strength reduction factors (φ) for a given edition.
     *
     * Note: ACI 318-19 introduced a transitional zone provision for φ between
     * tension-controlled and compression-controlled sections. The factors below
     * represent the governing boundary values — intermediate φ requires linear
     * interpolation based on net tensile strain (εt).
     */
    fun getStrengthReductionFactors(edition: Aci318Edition): Aci318StrengthReductionFactors {
        return when (edition) {
            Aci318Edition.ACI_318_14 -> Aci318StrengthReductionFactors(
                edition = Aci318Edition.ACI_318_14
                // All factors per ACI 318-14 Table 21.2.1
            )
            Aci318Edition.ACI_318_19 -> Aci318StrengthReductionFactors(
                edition = Aci318Edition.ACI_318_19
                // φ values unchanged from ACI 318-14 for standard provisions.
                // High-strength steel (Grade 80/100): tension-controlled φ = 0.90 maintained.
            )
            Aci318Edition.ACI_318_25 -> Aci318StrengthReductionFactors(
                edition = Aci318Edition.ACI_318_25
                // φ values consistent with ACI 318-19.
                // Seismic special systems: verify ACI 318-25 Chapter 18 for any overrides.
            )
        }
    }

    /**
     * Resolves the lambda (λ) modification factor for lightweight concrete.
     * Applied to all tensile strength expressions: shear (Vc), development length,
     * and splitting tensile strength.
     * ACI 318 Table 19.2.4.2.
     *
     * Consistent across all three supported editions.
     */
    fun getLambdaFactor(lightweightClass: LightweightConcreteClass): Double {
        return lightweightClass.lambda
    }

    /**
     * Resolves minimum concrete compressive strength requirements
     * for common exposure categories.
     * ACI 318 Table 19.3.3.1.
     *
     * Note: These requirements are consistent across ACI 318-14, -19, and -25
     * for the categories listed. ACI 318-25 introduced updated sulfate provisions
     * for S2/S3 categories — flag here if project involves sulfate exposure.
     */
    fun getStrengthRequirements(
        exposureCategory: ExposureCategory
    ): ConcreteStrengthRequirements {
        return when (exposureCategory) {
            ExposureCategory.F0 -> ConcreteStrengthRequirements(
                minFcPsi = 2500.0,
                maxWaterCementRatio = null,
                notes = "No freezing/thawing exposure — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.F1 -> ConcreteStrengthRequirements(
                minFcPsi = 3500.0,
                maxWaterCementRatio = 0.55,
                notes = "Moderate F/T exposure — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.F2 -> ConcreteStrengthRequirements(
                minFcPsi = 4500.0,
                maxWaterCementRatio = 0.45,
                notes = "Severe F/T exposure — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.F3 -> ConcreteStrengthRequirements(
                minFcPsi = 4500.0,
                maxWaterCementRatio = 0.40,
                notes = "F/T with deicing chemicals — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.S0 -> ConcreteStrengthRequirements(
                minFcPsi = 2500.0,
                maxWaterCementRatio = null,
                notes = "Negligible sulfate — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.S1 -> ConcreteStrengthRequirements(
                minFcPsi = 3000.0,
                maxWaterCementRatio = 0.50,
                notes = "Moderate sulfate — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.S2 -> ConcreteStrengthRequirements(
                minFcPsi = 4000.0,
                maxWaterCementRatio = 0.45,
                notes = "Severe sulfate — ACI 318 Table 19.3.3.1; Type V cement required"
            )
            ExposureCategory.S3 -> ConcreteStrengthRequirements(
                minFcPsi = 4000.0,
                maxWaterCementRatio = 0.40,
                notes = "Very severe sulfate — ACI 318 Table 19.3.3.1; Type V + pozzolan required"
            )
            ExposureCategory.W0 -> ConcreteStrengthRequirements(
                minFcPsi = 2500.0,
                maxWaterCementRatio = null,
                notes = "Not in contact with water — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.W1 -> ConcreteStrengthRequirements(
                minFcPsi = 2500.0,
                maxWaterCementRatio = null,
                notes = "In contact with water, low permeability not required"
            )
            ExposureCategory.W2 -> ConcreteStrengthRequirements(
                minFcPsi = 4000.0,
                maxWaterCementRatio = 0.50,
                notes = "In contact with water, low permeability required — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.C0 -> ConcreteStrengthRequirements(
                minFcPsi = 2500.0,
                maxWaterCementRatio = null,
                notes = "Low corrosion risk — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.C1 -> ConcreteStrengthRequirements(
                minFcPsi = 3000.0,
                maxWaterCementRatio = null,
                notes = "Moderate corrosion risk — ACI 318 Table 19.3.3.1"
            )
            ExposureCategory.C2 -> ConcreteStrengthRequirements(
                minFcPsi = 5000.0,
                maxWaterCementRatio = 0.40,
                notes = "High corrosion risk, chloride exposure — ACI 318 Table 19.3.3.1"
            )
        }
    }
}
