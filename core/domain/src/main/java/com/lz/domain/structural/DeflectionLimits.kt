package com.lz.domain.structural

/**
 * Pure domain model used for structural calculations.
 */
data class DeflectionLimits(
    val liveLoadDenominator: Double,
    val totalLoadDenominator: Double
)