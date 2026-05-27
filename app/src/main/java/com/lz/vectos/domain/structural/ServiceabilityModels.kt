package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length
import kotlinx.serialization.Serializable

/**
 * Types of serviceability limits.
 */
@Serializable
enum class ServiceabilityLimitType {
    TOTAL_LOAD_DEFLECTION,
    LIVE_LOAD_DEFLECTION,
    PONDING,
    VIBRATION,
    LATERAL_DRIFT
}

/**
 * Defines a code-required serviceability limit (e.g., L/360).
 */
@Serializable
data class ServiceabilityCriterion(
    val limitType: ServiceabilityLimitType,
    val name: String,
    val loadCategory: LoadCategory?, // Link to category instead of String loadCaseId
    val spanDenominator: Double, // e.g., 360 for L/360
    val description: String
)

/**
 * Holds the numerical result of a serviceability evaluation.
 */
@Serializable
data class ServiceabilityResult(
    val actualDeflection: Length,
    val allowableDeflection: Length,
    val utilization: Double,
    val criterion: ServiceabilityCriterion
)
