package com.lz.solver.material

import com.lz.model.structural.StationDemand
import com.lz.solver.bracing.StabilityFactorCalculator

/**
 * NDS Section 3.3.3 beam stability factor CL.
 * CL is section-property dependent (RB slenderness ratio) and requires
 * Emin and Fb — those are passed in at construction from NdsWoodCapacityCalculator.
 *
 * Placeholder returns conservative 1.0 until full NDS property chain is wired.
 */
class NdsClCalculator : StabilityFactorCalculator {
    override fun calculate(segmentDemands: List<StationDemand>, isCantilever: Boolean): Double {
        // TODO: implement NDS 3.3.3 CL using RB = sqrt(le*d/b²), Emin, Fb*
        return 1.0
    }
}