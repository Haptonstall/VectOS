package com.lz.model.structural

import kotlinx.serialization.Serializable
import com.lz.model.units.Pressure
import com.lz.model.units.Moment
import com.lz.model.units.Force

/**
 * Design methodology used by the building code.
 */
@Serializable
enum class DesignMethodology {
    LRFD, // Load and Resistance Factor Design (Strength)
    ASD   // Allowable Stress Design (Service)
}

/**
 * Material-specific strength properties.
 */
data class MaterialStrengthProperties(
    val yieldStrength: Pressure,
    val ultimateStrength: Pressure,
    val modulusOfElasticity: Pressure
)

/**
 * Result of a single strength check (e.g., Moment or Shear).
 */
data class StrengthCheckResult<T>(
    val demand: T,
    val capacity: T,
    val utilization: Double,
    val governingCombination: String,
    val governingMode: String? = null,
    val limitStateBreakdown: Map<String, Double> = emptyMap(),
    val traces: List<DesignEquationTrace> = emptyList()
)

/**
 * Aggregate results for a full strength evaluation.
 */
data class StrengthDesignResult(
    val momentCheck: StrengthCheckResult<Moment>,
    val shearCheck: StrengthCheckResult<Force>,
    val axialCheck: StrengthCheckResult<Force>,
    val torsionCheck: StrengthCheckResult<Moment>,
    val methodology: DesignMethodology,
    val designParameters: Map<String, String> = emptyMap(),
    /**
     * Flexure about the member's *other* principal axis, present only when
     * the analysis produced a nonzero moment about it ([momentCheck] is
     * always the axis the member is oriented to bend about). Null for
     * calculators that don't check it.
     */
    val secondaryMomentCheck: StrengthCheckResult<Moment>? = null,
    /**
     * Combined-stress interaction checks (e.g. NDS 3.9.1 bending + axial
     * tension, NDS 3.9.2 bending + axial compression / biaxial bending).
     * Empty when no combination applies or the calculator doesn't produce
     * any.
     */
    val combinedChecks: List<CombinedStressCheck> = emptyList(),
    /**
     * The material, section, adjustment factors and unbraced / effective
     * lengths this evaluation actually used, grouped for display — so a
     * reader can check a calculation's inputs without reverse-engineering
     * them from the traces. Empty for calculators that don't report it.
     */
    val selectedProperties: List<DesignPropertyGroup> = emptyList(),
    /**
     * What this evaluation does NOT have verified data or implementation for,
     * under the project's selected code / standard edition. Empty when
     * everything it used is sourced for that edition.
     */
    val codeDataNotices: List<CodeDataNotice> = emptyList(),
    /** e.g. "NDS 2024" — the standard edition the evaluation was run against. */
    val standardLabel: String? = null
)

/** How well a body of code data / rules is backed for the selected edition. */
enum class CodeDataStatus {
    /** Sourced from the edition's own published tables / text. */
    VERIFIED,
    /** Implemented, but not yet checked against the edition's own pages. */
    UNVERIFIED,
    /** Not available for the selected edition; values from another edition are standing in. */
    NOT_SOURCED,
    /** The rule exists in the code but this calculator does not apply it. */
    NOT_IMPLEMENTED
}

/** One item of [CodeDataStatus] that isn't plainly VERIFIED, with what it means for the result. */
data class CodeDataNotice(
    val dataset: String,
    val status: CodeDataStatus,
    val message: String
)

/** One labelled input value, e.g. symbol "Fb", description "Reference bending", value "875 psi". */
data class DesignPropertyRow(
    val symbol: String,
    val description: String,
    val value: String
)

/** A titled block of [DesignPropertyRow]s (material, section, factors, lengths ...). */
data class DesignPropertyGroup(
    val title: String,
    val rows: List<DesignPropertyRow>
)

/**
 * One equation of a combined-stress interaction check, e.g. NDS Eq. 3.9-1.
 * [value] is the left-hand side, which must not exceed [limit] (or must be
 * strictly below it when [isStrictLimit], as for NDS Eq. 3.9-4).
 * Infinite means the equation can't be satisfied — typically because a
 * buckling precondition (fc < FcE) fails.
 */
data class InteractionEquationResult(
    val equation: String,
    val value: Double,
    val limit: Double = 1.0,
    val isStrictLimit: Boolean = false
) {
    val passes: Boolean get() = if (isStrictLimit) value < limit else value <= limit
}

/**
 * A combined bending-and-axial (or biaxial bending) unity check with its
 * full development. [utilization] is the governing (largest) of
 * [equations]; [notes] carries any caveat the reader should see next to
 * the number (failed preconditions, simplifications, missing data).
 */
data class CombinedStressCheck(
    val label: String,
    val clause: String,
    val utilization: Double,
    val governingEquation: String,
    val equations: List<InteractionEquationResult>,
    val traces: List<DesignEquationTrace> = emptyList(),
    val notes: List<String> = emptyList()
)

/**
 * Qualitative status of a design check based on utilization.
 */
enum class DesignUtilizationStatus {
    LOW_UTILIZATION,
    MODERATE,
    HIGH,
    EXCEEDS_CAPACITY
}

/**
 * Descriptive interpretation of a design check.
 */
data class DesignInterpretation(
    val status: DesignUtilizationStatus,
    val explanation: String,
    val advisoryNote: String,
    val ratio: Double
)

/**
 * Formal record of an engineering decision.
 */
data class DesignDecision(
    val mechanism: String,
    val ratio: Double,
    val status: DesignUtilizationStatus,
    val engineerNote: String? = null
)