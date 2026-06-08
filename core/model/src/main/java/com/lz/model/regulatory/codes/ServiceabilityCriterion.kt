package com.lz.model.regulatory.codes

import com.lz.model.regulatory.LoadCategory
import kotlinx.serialization.Serializable

/**
 * Categories of code-defined serviceability checks.
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
 * A single code-required serviceability limit, e.g. L/360 for live load deflection.
 * Sourced from building code tables (IBC Table 1604.3, AISC Design Guide, etc.)
 */
@Serializable
data class ServiceabilityCriterion(
    val limitType: ServiceabilityLimitType,
    val name: String,
    val loadCategory: LoadCategory?,
    val spanDenominator: Double,
    val description: String
)