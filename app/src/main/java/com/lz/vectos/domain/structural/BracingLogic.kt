package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.inches

/**
 * Result of the unbraced length calculation at a specific point.
 */
data class DiscreteBracingResult(
    val x: Double,
    val Mx: Double,
    val compressionFlange: Flange,
    val Lb: Double
)

enum class Flange { TOP, BOTTOM }

/**
 * Core logic to calculate the effective unbraced length (Lb) for the compression flange 
 * at every discrete point along the beam.
 */
object BracingLogic {

    /**
     * Calculates Lb for every analysis point based on moment and bracing state.
     * 
     * @param momentData Discrete moment points (x in inches, Mx in lb-in).
     * @param braceState Normalized bracing flags for top/bottom flanges.
     * @param totalLength Total length of the member in inches.
     */
    fun calculateDiscreteLb(
        momentData: List<NormalizedMomentPoint>,
        braceState: List<NormalizedBraceState>,
        totalLength: Double
    ): List<DiscreteBracingResult> {
        // Ensure data is sorted by x
        val sortedMoments = momentData.sortedBy { it.x }
        val sortedBraces = braceState.sortedBy { it.x }

        return sortedMoments.map { point ->
            // 1. Determine which flange is in compression
            // Positive Moment = Top in Compression (Standard convention)
            val compressionFlange = if (point.Mx >= 0) Flange.TOP else Flange.BOTTOM
            
            // 2. Find the unbraced segment bounding this point for the active flange
            val Lb = findUnbracedLength(point.x, compressionFlange, sortedBraces, totalLength)

            DiscreteBracingResult(
                x = point.x,
                Mx = point.Mx,
                compressionFlange = compressionFlange,
                Lb = Lb
            )
        }
    }

    private fun findUnbracedLength(
        x: Double,
        flange: Flange,
        braces: List<NormalizedBraceState>,
        totalLength: Double
    ): Double {
        // Filter braces for the specific flange
        val activeBraces = braces.filter { 
            if (flange == Flange.TOP) it.isTopBraced else it.isBotBraced 
        }

        // Find the nearest brace to the left (<= x)
        val leftBrace = activeBraces.filter { it.x <= x + 1e-4 }.maxByOrNull { it.x }
        val leftX = leftBrace?.x ?: 0.0 // Default to start of beam if no brace found (Cantilever/Start)

        // Find the nearest brace to the right (>= x)
        val rightBrace = activeBraces.filter { it.x >= x - 1e-4 }.minByOrNull { it.x }
        val rightX = rightBrace?.x ?: totalLength // Default to end of beam if no brace found (Cantilever/End)

        return rightX - leftX
    }
}

/**
 * Input format for moment data.
 */
data class NormalizedMomentPoint(
    val x: Double,
    val Mx: Double
)
