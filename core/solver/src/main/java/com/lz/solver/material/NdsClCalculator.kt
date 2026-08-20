package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.StationDemand
import com.lz.model.units.inches
import com.lz.solver.bracing.StabilityFactorCalculator
import kotlin.math.abs

/**
 * NDS Section 3.3.3 beam stability factor CL, exposed as a
 * [StabilityFactorCalculator] for [com.lz.solver.bracing.BracingLogic]
 * (populates [StationDemand.cb] for wood spans).
 *
 * Note: this is a *reporting* path. The wood bending capacity check itself
 * ([NdsWoodCapacityCalculator]) computes CL independently, per-station, from
 * the station's own Lb — it does not read [StationDemand.cb]. This class
 * exists so the segment-level factor reported alongside steel/aluminum Cb
 * is a real NDS 3.3.3 value instead of the previous CL = 1.0 stub, and so
 * both paths share one formula ([computeNdsCL]) rather than risk drifting
 * apart.
 *
 * @param isGlulam Whether the member's species is a glulam species —
 *   changes the NDS 3.3.3 curve-fit constant c (0.90 glulam, 0.85 sawn).
 */
class NdsClCalculator(
    private val profile: SectionProfile,
    private val material: MaterialGrade.Wood,
    private val adjustmentFactors: NdsAdjustmentFactors = NdsAdjustmentFactors(),
    private val isGlulam: Boolean = material.species.isGlulam
) : StabilityFactorCalculator {

    override fun calculate(segmentDemands: List<StationDemand>, isCantilever: Boolean): Double {
        if (segmentDemands.isEmpty()) return 1.0

        // Unbraced length for this segment — span of the station demands
        // BracingLogic passed in. Matches how AiscCbCalculator derives its
        // segment length from the same list.
        val xStart = segmentDemands.first().x.inches
        val xEnd = segmentDemands.last().x.inches
        val lb = abs(xEnd - xStart)

        return computeNdsCL(lb, profile, material, adjustmentFactors, isGlulam)
    }
}
