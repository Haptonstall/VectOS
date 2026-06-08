package com.lz.model.structural

import com.lz.model.units.Length
import com.lz.model.units.inches
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Defines a discrete, continuous region along a physical span where the
 * compression flanges have a constant unbraced length (Lb).
 * * This is the core entity used by the solver to map changing stability
 * zones and plot the utilization heat map.
 */
@Serializable
data class UnbracedSegment(
    val startX: Length,  // The starting coordinate relative to the span origin
    val endX: Length,    // The ending coordinate relative to the span origin
    val lbTop: Length,   // The active unbraced length governing the top flange in this zone
    val lbBottom: Length // The active unbraced length governing the bottom flange in this zone
) {
    /**
     * Calculates the total physical length of this specific bracing zone.
     */
    val segmentLength: Length get() = endX - startX

    /**
     * Helper to check if a specific analytical station coordinate falls within this zone.
     */
    fun contains(x: Length): Boolean {
        return x >= startX && x <= endX
    }
}

/**
 * Bracing macro modes for structural elements.
 */
@Serializable
enum class BracingMode(val label: String) {
    CONTINUOUS("Continuous Attachment"),
    DISCRETE("Discrete Points Table"),
    REPETITIVE_SPACING("On-Center Spacing"),
    UNBRACED("Ends Only")
}

/**
 * A single explicit coordinate entry where a structural member is restrained laterally.
 */
@Serializable
data class DiscreteBracePoint(
    val x: Length,
    val isTopBraced: Boolean = true,
    val isBottomBraced: Boolean = false
)

/**
 * Clean, material-specific parameter inputs captured from UI forms.
 */
@Serializable
sealed class BracingInput {

    @Serializable
    data class Steel(
        val topMode: BracingMode = BracingMode.UNBRACED,
        val bottomMode: BracingMode = BracingMode.UNBRACED,
        val discreteTable: List<DiscreteBracePoint> = emptyList()
    ) : BracingInput()

    @Serializable
    data class Wood(
        val topMode: BracingMode = BracingMode.CONTINUOUS,
        val bottomMode: BracingMode = BracingMode.UNBRACED,
        val luTopSpacing: Length = 24.0.inches,    // e.g., 24" o.c. bridging
        val luBottomSpacing: Length = 96.0.inches, // e.g., 8ft o.c. blocking
        val discreteTable: List<DiscreteBracePoint> = emptyList()
    ) : BracingInput()
}

/**
 * Clean engine utility to compile distinct user layouts into physical interval zones.
 */
object BracingResolver {

    /**
     * Translates material-specific inputs into a unified list of dimensional unbraced segments.
     * This acts as the clean foundational link for your downstream capacity solvers and heat map.
     */
    fun resolveSegments(input: BracingInput, spanLength: Length): List<UnbracedSegment> {
        val lengthInches = spanLength.inches

        // 1. Collect all coordinate bounds where a physical brace boundary changes state
        val criticalPoints = mutableSetOf(0.0, lengthInches)

        when (input) {
            is BracingInput.Steel -> {
                input.discreteTable.forEach { criticalPoints.add(it.x.inches) }
            }
            is BracingInput.Wood -> {
                if (input.topMode == BracingMode.REPETITIVE_SPACING && input.luTopSpacing.inches > 0.0) {
                    var current = input.luTopSpacing.inches
                    while (current < lengthInches) {
                        criticalPoints.add(current)
                        current += input.luTopSpacing.inches
                    }
                }
                if (input.bottomMode == BracingMode.REPETITIVE_SPACING && input.luBottomSpacing.inches > 0.0) {
                    var current = input.luBottomSpacing.inches
                    while (current < lengthInches) {
                        criticalPoints.add(current)
                        current += input.luBottomSpacing.inches
                    }
                }
                input.discreteTable.forEach { criticalPoints.add(it.x.inches) }
            }
        }

        // Sort coordinates chronologically down the length of the span
        val sortedCoordinates = criticalPoints.toList().sorted()
        val segments = mutableListOf<UnbracedSegment>()

        // 2. Step through intervals to map the physical unbraced length (Lb) values active in each zone
        for (i in 0 until sortedCoordinates.size - 1) {
            val startX = sortedCoordinates[i]
            val endX = sortedCoordinates[i + 1]
            val midPoint = startX + (endX - startX) / 2.0

            val lbTop = calculateActiveLb(midPoint, startX, endX, lengthInches, true, input)
            val lbBottom = calculateActiveLb(midPoint, startX, endX, lengthInches, false, input)

            segments.add(
                UnbracedSegment(
                    startX = startX.inches,
                    endX = endX.inches,
                    lbTop = lbTop.inches,
                    lbBottom = lbBottom.inches
                )
            )
        }

        return segments
    }

    private fun calculateActiveLb(
        midPoint: Double,
        startX: Double,
        endX: Double,
        spanLength: Double,
        isTopFlange: Boolean,
        input: BracingInput
    ): Double {
        return when (input) {
            is BracingInput.Steel -> {
                val mode = if (isTopFlange) input.topMode else input.bottomMode
                when (mode) {
                    BracingMode.CONTINUOUS -> 0.0
                    BracingMode.UNBRACED -> spanLength
                    BracingMode.DISCRETE, BracingMode.REPETITIVE_SPACING -> {
                        // Unbraced length is the distance between the discrete endpoints bounding this segment
                        val hasLeftBrace = startX == 0.0 || hasDiscreteBraceAt(startX, isTopFlange, input.discreteTable)
                        val hasRightBrace = endX == spanLength || hasDiscreteBraceAt(endX, isTopFlange, input.discreteTable)

                        if (hasLeftBrace && hasRightBrace) endX - startX else spanLength
                    }
                }
            }
            is BracingInput.Wood -> {
                val mode = if (isTopFlange) input.topMode else input.bottomMode
                when (mode) {
                    BracingMode.CONTINUOUS -> 0.0
                    BracingMode.UNBRACED -> spanLength
                    BracingMode.REPETITIVE_SPACING -> {
                        if (isTopFlange) input.luTopSpacing.inches else input.luBottomSpacing.inches
                    }
                    BracingMode.DISCRETE -> {
                        val hasLeftBrace = startX == 0.0 || hasDiscreteBraceAt(startX, isTopFlange, input.discreteTable)
                        val hasRightBrace = endX == spanLength || hasDiscreteBraceAt(endX, isTopFlange, input.discreteTable)

                        if (hasLeftBrace && hasRightBrace) endX - startX else spanLength
                    }
                }
            }
        }
    }

    private fun hasDiscreteBraceAt(x: Double, isTopFlange: Boolean, table: List<DiscreteBracePoint>): Boolean {
        val tolerance = 0.001
        return table.any {
            abs(it.x.inches - x) < tolerance && (if (isTopFlange) it.isTopBraced else it.isBottomBraced)
        }
    }
}