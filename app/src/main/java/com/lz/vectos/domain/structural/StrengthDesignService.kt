package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment

/**
 * Pure Kotlin service to evaluate structural strength against material capacities.
 */
object StrengthDesignService {

    /**
     * Performs a strength evaluation for bending and shear.
     */
    fun evaluate(
        strengthEnvelope: LimitStateEnvelope,
        capacity: SectionCapacity,
        methodology: DesignMethodology
    ): StrengthDesignResult {
        
        val momentDemand = strengthEnvelope.maxMoment.value
        val momentCapacity = capacity.designMomentCapacity
        
        val shearDemand = strengthEnvelope.maxShear.value
        val shearCapacity = capacity.designShearCapacity

        return StrengthDesignResult(
            momentCheck = StrengthCheckResult(
                demand = momentDemand,
                capacity = momentCapacity,
                utilization = if (momentCapacity.newtonMeters > 0) momentDemand.newtonMeters / momentCapacity.newtonMeters else 0.0,
                governingCombination = strengthEnvelope.maxMoment.combinationName
            ),
            shearCheck = StrengthCheckResult(
                demand = shearDemand,
                capacity = shearCapacity,
                utilization = if (shearCapacity.newtons > 0) shearDemand.newtons / shearCapacity.newtons else 0.0,
                governingCombination = strengthEnvelope.maxShear.combinationName
            ),
            methodology = methodology
        )
    }
}
