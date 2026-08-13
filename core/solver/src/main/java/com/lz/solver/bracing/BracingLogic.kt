package com.lz.solver.bracing

import com.lz.model.structural.Flange
import com.lz.model.structural.NormalizedBraceState
import com.lz.model.structural.StationDemand
import com.lz.model.structural.StructuralMember
import com.lz.model.units.inches
import com.lz.model.units.inLbIn
import kotlin.math.abs

/**
 * Generic bracing logic that resolves unbraced lengths (Lb) and stability
 * modification factors for every station demand along a structural member.
 *
 * Material-specific stability factor calculation (Cb for steel/aluminum,
 * CL for wood) is delegated to a [StabilityFactorCalculator] provided by
 * the calling feature-layer solver. This keeps BracingLogic material-agnostic.
 *
 * Lb values are sourced from [SpanGeometry.unbracedSegments], which are
 * pre-resolved by [BracingResolver] from user bracing inputs. BracingLogic
 * does not re-derive segment boundaries — it only maps stations to segments
 * and computes the stability factor per segment.
 */
object BracingLogic {

    /**
     * Enriches each [StationDemand] with Lb (top and bottom), compression
     * flange designation, and the stability factor from [stabilityCalculator].
     *
     * @param stationDemands      Stations to enrich.
     * @param braceState          Global member brace points (from BeamViewModel).
     * @param totalLength         Full member length in inches.
     * @param member              StructuralMember supplying [SpanGeometry.unbracedSegments].
     * @param stabilityCalculator Material-specific stability factor (Cb, CL, etc.).
     */
    fun calculateDiscreteLb(
        stationDemands: List<StationDemand>,
        braceState: List<NormalizedBraceState>,
        totalLength: Double,
        member: StructuralMember? = null,
        stabilityCalculator: StabilityFactorCalculator = StabilityFactorCalculator { _, _ -> 1.0 }
    ): List<StationDemand> {
        if (stationDemands.isEmpty()) return stationDemands

        val sorted = stationDemands.sortedBy { it.x.inches }

        // Build segment lists for top and bottom flanges from global brace state
        val topSegments = buildSegments(Flange.TOP, braceState, totalLength, sorted, stabilityCalculator)
        val botSegments = buildSegments(Flange.BOTTOM, braceState, totalLength, sorted, stabilityCalculator)

        return sorted.map { demand ->
            val xInches = demand.x.inches

            // Compression flange: positive moment → top flange in compression
            val compressionFlange = when {
                demand.moment.inLbIn > -1e-6  -> Flange.TOP
                demand.moment.inLbIn < 1e-6 -> Flange.BOTTOM
                else                          -> Flange.NONE
            }

            // Resolve Lb from SpanGeometry.unbracedSegments if member is available,
            // otherwise fall back to the global brace-state segments
            val span = member?.spans?.find { it.id == demand.spanId }

            val lbTop: Double
            val lbBottom: Double

            if (span != null && span.unbracedSegments.isNotEmpty()) {
                val seg = span.getUnbracedSegmentAt(demand.x)
                lbTop    = seg.lbTop.inches
                lbBottom = seg.lbBottom.inches
            } else {
                val tSeg = findSegment(xInches, topSegments)
                val bSeg = findSegment(xInches, botSegments)
                lbTop    = tSeg?.lb ?: totalLength
                lbBottom = bSeg?.lb ?: totalLength
            }

            // Stability factor from the compression flange's segment
            val compressionSegments = if (compressionFlange == Flange.BOTTOM) botSegments else topSegments
            val stabilityFactor = findSegment(xInches, compressionSegments)?.stabilityFactor ?: 1.0

            demand.copy(
                lbTop            = lbTop.inches,
                lbBottom         = lbBottom.inches,
                cb               = stabilityFactor,
                compressionFlange = compressionFlange
            )
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private data class ResolvedSegment(
        val startX: Double,
        val endX: Double,
        val lb: Double,
        val stabilityFactor: Double
    )

    private fun findSegment(xInches: Double, segments: List<ResolvedSegment>): ResolvedSegment? =
        segments.find { xInches >= it.startX - 1e-4 && xInches <= it.endX + 1e-4 }
            ?: segments.minByOrNull { abs(it.startX - xInches) }

    /**
     * Builds [ResolvedSegment] list for one flange from [NormalizedBraceState] positions.
     * Stability factor is computed per segment via [stabilityCalculator].
     */
    private fun buildSegments(
        flange: Flange,
        braces: List<NormalizedBraceState>,
        totalLength: Double,
        demands: List<StationDemand>,
        stabilityCalculator: StabilityFactorCalculator
    ): List<ResolvedSegment> {
        // Collect active brace positions for this flange as raw Double (inches)
        val bracePositions: List<Double> = braces
            .filter { if (flange == Flange.TOP) it.isTopBraced else it.isBotBraced }
            .map { it.x.inches }
            .distinct()
            .sorted()

        // Segment boundaries: member ends plus all brace positions
        val boundaries: List<Double> = (listOf(0.0, totalLength) + bracePositions)
            .distinct()
            .sorted()

        val segments = mutableListOf<ResolvedSegment>()

        for (i in 0 until boundaries.size - 1) {
            val start = boundaries[i]
            val end   = boundaries[i + 1]
            if (abs(end - start) < 1e-6) continue

            val isStartBraced = start == 0.0 || bracePositions.any { abs(it - start) < 1e-4 }
            val isEndBraced   = end == totalLength || bracePositions.any { abs(it - end) < 1e-4 }
            val isCantilever  = !isStartBraced || !isEndBraced

            val segmentDemands = demands.filter { it.x.inches in (start - 1e-4)..(end + 1e-4) }
            val stabilityFactor = stabilityCalculator.calculate(segmentDemands, isCantilever)

            segments.add(ResolvedSegment(start, end, end - start, stabilityFactor))
        }

        if (segments.isEmpty()) {
            segments.add(ResolvedSegment(0.0, totalLength, totalLength, 1.0))
        }

        return segments
    }
}