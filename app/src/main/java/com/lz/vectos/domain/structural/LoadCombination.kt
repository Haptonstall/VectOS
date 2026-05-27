package com.lz.vectos.domain.structural

/**
 * Represents a code-authoritative load combination per ASCE 7.
 */
data class LoadCombination(
    val name: String,
    val methodology: DesignMethodology,
    val equation: String,
    val factors: Map<LoadCategory, Double>,
    val codeReference: String,
    val limitState: LimitState = if (methodology == DesignMethodology.LRFD) LimitState.STRENGTH else LimitState.SERVICEABILITY
)
