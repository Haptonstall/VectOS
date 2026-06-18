package com.lz.solver.capacity

import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.SectionCapacity
import com.lz.model.structural.StrengthCheckResult
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.units.Force
import com.lz.model.units.Moment
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import com.lz.solver.analysis.LimitStateEnvelope
import kotlin.math.abs

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
                utilization = if (abs(momentCapacity.inLbIn) > 0) abs(momentDemand.inLbIn) / abs(
                    momentCapacity.inLbIn
                ) else 0.0,
                governingCombination = strengthEnvelope.maxMoment.combinationName
            ),
            shearCheck = StrengthCheckResult(
                demand = shearDemand,
                capacity = shearCapacity,
                utilization = if (abs(shearCapacity.inPoundsForce) > 0) abs(shearDemand.inPoundsForce) / abs(
                    shearCapacity.inPoundsForce
                ) else 0.0,
                governingCombination = strengthEnvelope.maxShear.combinationName
            ),
            axialCheck = StrengthCheckResult(
                demand = Force(0.0),
                capacity = Force(0.0),
                utilization = 0.0,
                governingCombination = "None"
            ),
            torsionCheck = StrengthCheckResult(
                demand = Moment(0.0),
                capacity = Moment(0.0),
                utilization = 0.0,
                governingCombination = "None"
            ),
            methodology = methodology
        )
    }
}