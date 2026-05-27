package com.lz.vectos.domain.structural

/**
 * Represents a code-authoritative load combination.
 */
data class LoadCombination(
    val id: String = "",
    val name: String,
    val methodology: DesignMethodology,
    val equation: String,
    val factors: Map<LoadCategory, Double>,
    val codeReference: String,
    val limitState: LimitState = if (methodology == DesignMethodology.LRFD) LimitState.STRENGTH else LimitState.SERVICEABILITY
)

/**
 * Represents a logical grouping of load combinations (e.g., "ASCE 7-16 LRFD Combinations").
 */
data class LoadCombinationSet(
    val id: String,
    val sourceId: String,
    val sourceName: String,
    val methodology: DesignMethodology,
    val description: String,
    val combinations: List<LoadCombination>
)
