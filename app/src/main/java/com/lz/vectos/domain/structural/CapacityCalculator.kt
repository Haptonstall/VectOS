package com.lz.vectos.domain.structural

import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.StrengthDesignResult
import com.lz.vectos.domain.structural.analysis.BeamAnalysisResult

/**
 * Common interface for material-specific capacity calculation engines.
 */
interface CapacityCalculator {
    /**
     * Performs a detailed design check for all points along a member.
     */
    fun evaluateAll(analysisResult: BeamAnalysisResult, methodology: DesignMethodology): List<PointCapacityResult>
    
    /**
     * Performs a detailed check at a specific station.
     */
    fun evaluateDetailed(demand: StationDemand, methodology: DesignMethodology): StrengthDesignResult

    /**
     * Performs a raw capacity calculation for a specific demand station.
     */
    fun evaluate(demand: StationDemand): RawCapacityResult
}
