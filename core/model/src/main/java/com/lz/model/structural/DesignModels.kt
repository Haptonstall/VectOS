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
    val designParameters: Map<String, String> = emptyMap()
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