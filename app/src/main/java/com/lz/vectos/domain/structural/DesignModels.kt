package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment
import com.lz.vectos.domain.units.Pressure

/**
 * Design methodology used by the building code.
 */
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
 * Pre-calculated capacities for a specific section and material.
 */
data class SectionCapacity(
    val nominalMomentCapacity: Moment,
    val nominalShearCapacity: Force,
    val designMomentCapacity: Moment, // e.g., phi * Mn
    val designShearCapacity: Force    // e.g., phi * Vn
)

/**
 * Result of a single strength check (e.g., Moment or Shear).
 */
data class StrengthCheckResult<T>(
    val demand: T,
    val capacity: T,
    val utilization: Double,
    val governingCombination: String
)

/**
 * Aggregate results for a full strength evaluation.
 */
data class StrengthDesignResult(
    val momentCheck: StrengthCheckResult<Moment>,
    val shearCheck: StrengthCheckResult<Force>,
    val methodology: DesignMethodology
)
