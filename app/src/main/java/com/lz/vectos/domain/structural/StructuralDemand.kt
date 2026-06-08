package com.lz.vectos.domain.structural

import com.lz.model.structural.DesignEquationTrace
import com.lz.model.structural.DesignMethodology
import com.lz.model.units.Force
import com.lz.model.units.Moment

/**
 * Represents the governing demand (factored or service-level) for a structural member.
 * Strictly bound to a specific design methodology and load combination.
 */
data class StructuralDemand(
    val moment: Moment,
    val shear: Force,
    val axial: Force? = null,
    val torsion: Moment? = null,
    val methodology: DesignMethodology,
    val governingCombination: LoadCombination,
    val trace: DesignEquationTrace
)
