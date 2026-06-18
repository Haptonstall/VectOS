package com.lz.solver.material

import com.lz.model.structural.StationDemand
import com.lz.model.units.inLbIn
import com.lz.solver.bracing.StabilityFactorCalculator
import kotlin.math.abs

/**
 * AISC 360-22 Section F1 Cb calculation.
 * Cb = 12.5*Mmax / (2.5*Mmax + 3*Ma + 4*Mb + 3*Mc)
 *
 * Quarter-point moments Ma, Mb, Mc are interpolated from station demands.
 */
object AiscCbCalculator : StabilityFactorCalculator {

    override fun calculate(segmentDemands: List<StationDemand>, isCantilever: Boolean): Double {
        if (isCantilever) return 1.0
        if (segmentDemands.isEmpty()) return 1.0

        val mMax = segmentDemands.maxOf { abs(it.moment.inLbIn) }
        if (mMax < 1e-4) return 1.0

        val xStart = segmentDemands.first().x.inches
        val xEnd   = segmentDemands.last().x.inches
        val l      = xEnd - xStart

        val ma = abs(interpolateMoment(xStart + l * 0.25, segmentDemands))
        val mb = abs(interpolateMoment(xStart + l * 0.50, segmentDemands))
        val mc = abs(interpolateMoment(xStart + l * 0.75, segmentDemands))

        val cb = (12.5 * mMax) / (2.5 * mMax + 3 * ma + 4 * mb + 3 * mc)
        return cb.coerceIn(1.0, 3.0)
    }

    private fun interpolateMoment(x: Double, demands: List<StationDemand>): Double {
        if (demands.isEmpty()) return 0.0
        val next = demands.find { it.x.inches >= x - 1e-4 } ?: return demands.last().moment.inLbIn
        val prev = demands.findLast { it.x.inches <= x + 1e-4 } ?: return demands.first().moment.inLbIn
        if (next == prev) return next.moment.inLbIn
        val dx = next.x.inches - prev.x.inches
        if (abs(dx) < 1e-6) return prev.moment.inLbIn
        val t = (x - prev.x.inches) / dx
        return prev.moment.inLbIn + t * (next.moment.inLbIn - prev.moment.inLbIn)
    }
}