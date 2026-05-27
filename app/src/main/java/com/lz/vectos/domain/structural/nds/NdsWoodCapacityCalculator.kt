package com.lz.vectos.domain.structural.nds

import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.structural.MaterialGrade
import com.lz.vectos.domain.structural.CapacityCalculator
import com.lz.vectos.domain.structural.PointCapacityResult
import com.lz.vectos.domain.structural.StrengthDesignResult
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.structural.CapacityEngine
import com.lz.vectos.domain.structural.StationDemand
import com.lz.vectos.domain.structural.StrengthCheckResult
import com.lz.vectos.domain.structural.RawCapacityResult
import com.lz.vectos.domain.structural.analysis.BeamAnalysisResult
import com.lz.vectos.domain.units.*
import kotlin.math.abs

/**
 * NDS-aligned Wood Capacity Calculator (ASD/LRFD).
 */
class NdsWoodCapacityCalculator(
    private val profile: SectionProfile,
    private val material: MaterialGrade.Wood
) : CapacityCalculator {

    override fun evaluateAll(
        analysisResult: BeamAnalysisResult,
        methodology: DesignMethodology
    ): List<PointCapacityResult> {
        return CapacityEngine.evaluate(
            demands = analysisResult.spanResults.flatMap { it.stationDemands },
            section = profile,
            methodology = methodology,
            capacityCalculator = { evaluate(it) }
        )
    }

    override fun evaluate(demand: StationDemand): RawCapacityResult {
        // NDS Placeholder: F'b = Fb * Cd * Cm * Ct * Cl * Cf * Cfu * Ci * Cr
        // For now, assume unadjusted reference properties
        val nominalMn = material.referenceBending.inPsi * profile.propertiesStrongAxis.s.inIn3
        val nominalVn = (2.0/3.0) * material.referenceShear.inPsi * profile.area.inIn2
        
        return RawCapacityResult(
            nominalFlexureX = nominalMn,
            limitStateFlexureX = "Bending (Placeholder)",
            nominalFlexureY = 0.0,
            limitStateFlexureY = "N/A",
            nominalShearX = nominalVn,
            limitStateShearX = "Shear (Placeholder)",
            nominalShearY = 0.0,
            limitStateShearY = "N/A",
            nominalAxial = material.referenceCompressionParallel.inPsi * profile.area.inIn2,
            limitStateAxial = "Compression (Placeholder)",
            nominalTorsion = 0.0,
            limitStateTorsion = "N/A",
            allowableDeflection = demand.allowableDeflection.inInches,
            limitStateDeflection = "Deflection"
        )
    }

    override fun evaluateDetailed(demand: StationDemand, methodology: DesignMethodology): StrengthDesignResult {
        val raw = evaluate(demand)
        
        // Simplified NDS logic: Design Capacity = Nominal (assuming ASD for now)
        val designMn = raw.nominalFlexureX
        val designVn = raw.nominalShearX
        val designPn = raw.nominalAxial
        
        return StrengthDesignResult(
            momentCheck = StrengthCheckResult(
                demand = demand.moment,
                capacity = designMn.lbIn,
                utilization = if (designMn > 0) abs(demand.moment.lbIn) / designMn else 0.0,
                governingCombination = "Current",
                governingMode = raw.limitStateFlexureX
            ),
            shearCheck = StrengthCheckResult(
                demand = demand.shear,
                capacity = designVn.poundsForce,
                utilization = if (designVn > 0) abs(demand.shear.pounds) / designVn else 0.0,
                governingCombination = "Current",
                governingMode = raw.limitStateShearX
            ),
            axialCheck = StrengthCheckResult(
                demand = demand.axial,
                capacity = designPn.poundsForce,
                utilization = if (designPn > 0) abs(demand.axial.pounds) / designPn else 0.0,
                governingCombination = "Current",
                governingMode = raw.limitStateAxial
            ),
            torsionCheck = StrengthCheckResult(
                demand = demand.torque,
                capacity = 0.0.lbIn,
                utilization = 0.0,
                governingCombination = "N/A"
            ),
            methodology = methodology
        )
    }
}
