package com.lz.vectos.domain.structural

/**
 * Represents a linear combination of load cases.
 * Total Load = Factor1 * Case1 + Factor2 * Case2 + ...
 */
data class LoadCombination(
    val name: String,
    val factors: Map<String, Double> // Map of LoadCase ID to scaling factor
)
