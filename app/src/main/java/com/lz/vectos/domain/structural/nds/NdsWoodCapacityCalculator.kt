package com.lz.vectos.domain.structural.nds

import com.lz.model.units.inIn2
import com.lz.model.units.inIn3
import com.lz.model.units.inInches
import com.lz.model.units.inPsi
import com.lz.model.units.lbIn
import com.lz.model.units.poundsForce
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.MaterialGrade
import com.lz.vectos.domain.structural.CapacityCalculator
import com.lz.vectos.domain.structural.PointCapacityResult
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.DesignMethodology
import com.lz.vectos.domain.structural.CapacityEngine
import com.lz.vectos.domain.structural.StationDemand
import com.lz.model.structural.StrengthCheckResult
import com.lz.vectos.domain.structural.RawCapacityResult
import com.lz.vectos.domain.structural.analysis.BeamAnalysisResult
import com.lz.model.units.*
import kotlin.math.abs

/**
 * NDS-aligned Wood Capacity Calculator (ASD/LRFD).
 */
class NdsWoodCapacityCalculator(
    private val profile: SectionProfile,
    private val material: MaterialGrade.Wood,
    private val adjustmentFactors: NdsAdjustmentFactors = NdsAdjustmentFactors()
) : CapacityCalculator {

    override fun evaluate(demand: StationDemand): RawCapacityResult {
        val f = adjustmentFactors

        // F'b = Fb * adjustment chain
        val fbAdj = f.adjustedBending(material.referenceBending.inPsi)
        val fvAdj = f.adjustedShear(material.referenceShear.inPsi)
        val fcAdj = f.adjustedCompressionParallel(
            material.referenceCompressionParallel.inPsi
        )

        val nominalMn = fbAdj * profile.propertiesStrongAxis.s.inIn3
        val nominalVn = (2.0 / 3.0) * fvAdj * profile.area.inIn2
        val nominalPn = fcAdj * profile.area.inIn2

        return RawCapacityResult(
            nominalFlexureX    = nominalMn,
            limitStateFlexureX = "Bending (NDS 3.3)",
            nominalShearX      = nominalVn,
            limitStateShearX   = "Shear (NDS 3.4)",
            nominalAxial       = nominalPn,
            limitStateAxial    = "Compression (NDS 3.7)",
            // unchanged fields below
            nominalFlexureY    = 0.0,
            limitStateFlexureY = "N/A",
            nominalShearY      = 0.0,
            limitStateShearY   = "N/A",
            nominalTorsion     = 0.0,
            limitStateTorsion  = "N/A",
            allowableDeflection = demand.allowableDeflection.inInches,
            limitStateDeflection = "Deflection"
        )
    }
}