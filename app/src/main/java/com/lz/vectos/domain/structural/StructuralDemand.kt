package com.lz.vectos.domain.structural

/**
 * Represents the governing demand (factored or service-level) for a structural member.
 * Strictly bound to a specific design methodology and load combination.
 */
data class StructuralDemand(
    val moment: Double,
    val shear: Double,
    val axial: Double? = null,
    val torsion: Double? = null,
    val methodology: DesignMethodology,
    val governingCombination: LoadCombination,
    val trace: DesignEquationTrace
)
