package com.lz.model.regulatory

import com.lz.model.structural.DesignMethodology
import kotlinx.serialization.Serializable

/**
 * Represents a code-authoritative load combination.
 * Fully unified to support mathematical parsing and structural report generation.
 */
@Serializable
data class LoadCombination(
    val id: String,
    val name: String, // Short identifier, e.g., "722_LRFD_2"
    val methodology: DesignMethodology,
    val equationText: String, // Readable string for calculation reports/UIs, e.g., "1.2D + 1.6L"
    val factors: Map<LoadCategory, Double>,
    val codeReference: String, // Legal source trail, e.g., "ASCE 7-22 Section 2.3.1, Eq. 2"
    val isLateralReversible: Boolean = false // Flags if the solver must test both direction vectors (+ and -)
)

/**
 * Represents a specific code ruleset or source generation profile.
 */
@Serializable
data class CombinationSource(
    val id: String, // e.g., "asce_7_22"
    val name: String, // e.g., "ASCE 7-22"
    val description: String
)

/**
 * Represents a logical, production-ready grouping of load combinations
 * explicitly tied to a standard version and methodology.
 */
@Serializable
data class LoadCombinationSet(
    val id: String,
    val source: CombinationSource,
    val methodology: DesignMethodology,
    val description: String,
    val combinations: List<LoadCombination>
)