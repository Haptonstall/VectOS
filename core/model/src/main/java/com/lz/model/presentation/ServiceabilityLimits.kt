package com.lz.model.presentation

/**
 * UI-centric model for serviceability limit selection.
 */
data class ServiceabilityLimits(
    val category: ServiceabilityCategory = ServiceabilityCategory.FLOOR,
    val customLiveDenominator: Double? = null,
    val customTotalDenominator: Double? = null
)

/**
 * Standard IBC serviceability categories for UI selection.
 */
enum class ServiceabilityCategory {
    FLOOR,
    ROOF_SUPPORTING_PLASTER,
    ROOF_SUPPORTING_NONPLASTER,
    ROOF_NO_CEILING,
    EXTERIOR_WALL_BRITTLE,
    EXTERIOR_WALL_FLEXIBLE,
    INTERIOR_PARTITION,
    CUSTOM
}