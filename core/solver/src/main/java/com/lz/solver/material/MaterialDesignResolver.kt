package com.lz.solver.material

import com.lz.model.regulatory.AiscEdition
import com.lz.model.regulatory.aisc.AiscDesignFactorRegistry
import com.lz.model.regulatory.aisc.AiscDesignFactors
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.structural.DesignMethodology

/**
 * Registry-style resolver for material design strategies.
 */
// solver/material/MaterialDesignResolver.kt
object MaterialDesignResolver {

    fun resolveAiscFactors(
        edition: AiscEdition,
        methodology: DesignMethodology
    ): AiscDesignFactors = AiscDesignFactorRegistry.get(edition, methodology)

    fun resolveNdsFactors(
        // Future: takes project conditions (moisture, temperature, duration)
        // For now returns defaults
    ): NdsAdjustmentFactors = NdsAdjustmentFactors()
}
