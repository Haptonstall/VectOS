package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length

/**
 * Defines a code-required serviceability limit (e.g., L/360).
 */
data class ServiceabilityCriterion(
    val id: String,
    val name: String,
    val loadCaseId: String?, // ID of the load case to check, null means Total Load
    val spanDenominator: Double, // e.g., 360 for L/360
    val description: String
)

/**
 * Holds the numerical result of a serviceability evaluation.
 */
data class ServiceabilityResult(
    val actualDeflection: Length,
    val allowableDeflection: Length,
    val utilization: Double,
    val criterion: ServiceabilityCriterion
)
