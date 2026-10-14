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
    val combinedChecks: List<CombinedStressCheck> = emptyList()
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