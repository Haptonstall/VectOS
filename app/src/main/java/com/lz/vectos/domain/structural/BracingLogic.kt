package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.inches
import com.lz.vectos.domain.units.inLbIn
import kotlin.math.abs

/**
 * Result of the unbraced length calculation at a specific point.
 */
data class DiscreteBracingResult(
    val x: Double,
    val Mx: Double,
    val compressionFlange: Flange,
    val Lb: Double
)

/**
 * Core logic to calculate the effective unbraced length (Lb) and AISC Cb factor
 * based on unbraced segments for the compression flange.
 */
object BracingLogic {

    /**
     * Calculates Lb and Cb for every analysis point based on segment-based logic.
     *
     * @param stationDemands Discrete analysis points (stations) along the member.
     * @param braceState Normalized bracing flags for top/bottom flanges.
     * @param totalLength Total length of the member in inches.
     * @param member The structural member containing span bracing definitions.
     * @return Updated list of StationDemand with unbracedLength and Cb populated.
     */
    fun calculateDiscreteLb(
        stationDemands: List<StationDemand>,
        braceState: List<NormalizedBraceState>,
        totalLength: Double,
        member: StructuralMember? = null
    ): List<StationDemand> {
        if (stationDemands.isEmpty()) return stationDemands

        // 1. Sort demands by position to ensure consistent interpolation and segment matching
        val sortedDemands = stationDemands.sortedBy { it.x.inches }

        // 2. Pre-calculate unbraced segments and their Cb factors for both flanges independently
        val topSegments = calculateSegments(Flange.TOP, braceState, totalLength, sortedDemands)
        val botSegments = calculateSegments(Flange.BOTTOM, braceState, totalLength, sortedDemands)

        return sortedDemands.map { demand ->
            val x = demand.x.inches

            // 3. Determine compression flange (Positive Moment = Top in Compression)
            val compressionFlange = if (demand.moment.inLbIn >= 0) Flange.TOP else Flange.BOTTOM

            // 4. Match the station to its corresponding unbraced segment
            val relevantSegments = if (compressionFlange == Flange.TOP) topSegments else botSegments
            val segment = relevantSegments.find { x >= it.startX - 1e-4 && x <= it.endX + 1e-4 }
                ?: relevantSegments.minByOrNull { abs(it.startX - x) }

            // Determine bracing mode for this specific point based on span and flange
            val span = member?.spans?.find { it.id == demand.spanId }
            val topBracingMode = span?.bracing?.topType ?: BracingMode.UNBRACED
            val botBracingMode = span?.bracing?.bottomType ?: BracingMode.UNBRACED

            val topSegment = topSegments.find { x >= it.startX - 1e-4 && x <= it.endX + 1e-4 }
                ?: topSegments.minByOrNull { abs(it.startX - x) }
            val botSegment = botSegments.find { x >= it.startX - 1e-4 && x <= it.endX + 1e-4 }
                ?: botSegments.minByOrNull { abs(it.startX - x) }

            // Handle Continuous Bracing: Lb = 0.0
            val lbTop = if (topBracingMode == BracingMode.CONTINUOUS) 0.0 else (topSegment?.lb ?: totalLength)
            val lbBottom = if (botBracingMode == BracingMode.CONTINUOUS) 0.0 else (botSegment?.lb ?: totalLength)

            // Cb is associated with the governing (compression) flange segment
            val relevantSegment = if (compressionFlange == Flange.TOP) topSegment else botSegment

            demand.copy(
                lbTop = lbTop.inches,
                lbBottom = lbBottom.inches,
                cb = relevantSegment?.cb ?: 1.0,
                compressionFlange = compressionFlange
            )
        }
    }

    private data class UnbracedSegment(
        val startX: Double,
        val endX: Double,
        val lb: Double,
        val cb: Double
    )

    /**
     * Identifies all unbraced segments for a specific flange and calculates Cb for each.
     * A segment is defined as the distance between two consecutive true bracing flags.
     */
    private fun calculateSegments(
        flange: Flange,
        braces: List<NormalizedBraceState>,
        totalLength: Double,
        demands: List<StationDemand>
    ): List<UnbracedSegment> {
        val activeBracePositions = braces
            .filter { if (flange == Flange.TOP) it.isTopBraced else it.isBotBraced }
            .map { it.x }
            .distinct()
            .sorted()

        // Define segment boundaries: member ends and all brace locations
        val boundaries = (listOf(0.0, totalLength) + activeBracePositions).distinct().sorted()

        val segments = mutableListOf<UnbracedSegment>()
        for (i in 0 until boundaries.size - 1) {
            val start = boundaries[i]
            val end = boundaries[i + 1]
            if (abs(start - end) < 1e-6) continue

            // A segment is considered a "cantilever" or unbraced end if one of its boundaries 
            // is not explicitly braced (e.g., at a member end with no support/brace).
            val isStartBraced = activeBracePositions.any { abs(it - start) < 1e-4 }
            val isEndBraced = activeBracePositions.any { abs(it - end) < 1e-4 }
            val isCantilever = !isStartBraced || !isEndBraced

            val cb = if (isCantilever) {
                1.0 // Conservative default for cantilevers or segments with one free end
            } else {
                calculateCbForSegment(start, end, demands)
            }

            segments.add(UnbracedSegment(start, end, end - start, cb))
        }

        if (segments.isEmpty()) {
            segments.add(UnbracedSegment(0.0, totalLength, totalLength, 1.0))
        }

        return segments
    }

    /**
     * Calculates the AISC 360-22 Cb factor for a specific segment.
     * Cb = 12.5*Mmax / (2.5*Mmax + 3*Ma + 4*Mb + 3*Mc)
     */
    private fun calculateCbForSegment(
        xStart: Double,
        xEnd: Double,
        demands: List<StationDemand>
    ): Double {
        // Find demands within the segment for Mmax
        val segmentDemands = demands.filter { it.x.inches in (xStart - 1e-4)..(xEnd + 1e-4) }
        if (segmentDemands.isEmpty()) return 1.0

        val mMax = segmentDemands.maxOf { abs(it.moment.inLbIn) }
        if (mMax < 1e-4) return 1.0

        // Calculate quarter points
        val l = xEnd - xStart
        val xA = xStart + l * 0.25
        val xB = xStart + l * 0.50
        val xC = xStart + l * 0.75

        // Interpolate absolute moments at quarter points
        val ma = abs(interpolateMoment(xA, demands))
        val mb = abs(interpolateMoment(xB, demands))
        val mc = abs(interpolateMoment(xC, demands))

        // AISC F1-1 formula
        val cb = (12.5 * mMax) / (2.5 * mMax + 3 * ma + 4 * mb + 3 * mc)
        
        // Cb is typically capped at 1.0 minimum and 3.0 maximum for design
        return cb.coerceIn(1.0, 3.0)
    }

    /**
     * Linear interpolation of moment at any point x.
     */
    private fun interpolateMoment(x: Double, demands: List<StationDemand>): Double {
        if (demands.isEmpty()) return 0.0
        
        // Find bounding demands
        val next = demands.find { it.x.inches >= x - 1e-4 } ?: return demands.last().moment.inLbIn
        val prev = demands.findLast { it.x.inches <= x + 1e-4 } ?: return demands.first().moment.inLbIn

        if (next == prev) return next.moment.inLbIn

        val dx = next.x.inches - prev.x.inches
        if (abs(dx) < 1e-6) return prev.moment.inLbIn
        
        val t = (x - prev.x.inches) / dx
        return prev.moment.inLbIn + t * (next.moment.inLbIn - prev.moment.inLbIn)
    }
}

/**
 * Input format for moment data.
 */
data class NormalizedMomentPoint(
    val x: Double,
    val Mx: Double
)
