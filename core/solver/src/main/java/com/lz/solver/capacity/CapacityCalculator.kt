package com.lz.solver.capacity

import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.PointCapacityResult
import com.lz.model.structural.StationDemand
import com.lz.model.structural.StrengthDesignResult
import com.lz.solver.analysis.AnalysisResult

/**
 * Common interface for material-specific capacity calculation engines.
 */
interface CapacityCalculator {
    /**
     * Performs a detailed check at a specific station.
     */
    fun evaluateDetailed(demand: StationDemand, methodology: DesignMethodology): StrengthDesignResult

    /**
     * Performs a raw capacity calculation for a specific demand station.
     */
    fun evaluate(demand: StationDemand): RawCapacityResult

    /**
     * Real, material-specific per-limit-state design factors for the given
     * methodology — used by [com.lz.solver.capacity.CapacityEngine] to turn
     * this calculator's nominal [RawCapacityResult] values into design
     * (factored) capacities. See [DesignFactorSet] for how `value` is
     * interpreted per methodology.
     */
    fun designFactors(methodology: DesignMethodology): DesignFactorSet
}