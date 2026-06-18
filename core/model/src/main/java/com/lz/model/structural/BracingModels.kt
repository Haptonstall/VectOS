package com.lz.model.structural

import com.lz.model.units.Length
import com.lz.model.units.inches
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Defines a discrete, continuous region along a physical span where the
 * compression flange or face has a constant unbraced length (Lb).
 *
 * This is the core entity consumed by the solver to map stability zones
 * and plot the utilization heat map.
 */
@Serializable
data class UnbracedSegment(
    val startX: Length,     // Starting coordinate relative to the span origin
    val endX: Length,       // Ending coordinate relative to the span origin
    val lbTop: Length,      // Active unbraced length governing the top flange/face in this zone
    val lbBottom: Length    // Active unbraced length governing the bottom flange/face in this zone
) {
    val segmentLength: Length get() = endX - startX

    fun contains(x: Length): Boolean = x >= startX && x <= endX
}

/**
 * Bracing macro modes applicable to structural members.
 *
 * REPETITIVE_SPACING is wood-specific (bridging/blocking at regular o.c. spacing).
 * All other modes apply across steel, aluminum, masonry, and wood.
 */
@Serializable
enum class BracingMode(val label: String) {
    CONTINUOUS(         "Continuous Attachment"),
    DISCRETE(           "Discrete Points Table"),
    REPETITIVE_SPACING( "On-Center Spacing"),      // Wood only
    UNBRACED(           "Ends Only")
}

/**
 * A single explicit coordinate where a structural member is restrained laterally.
 *
 * For steel and aluminum: isTopBraced = top flange, isBottomBraced = bottom flange.
 * For masonry: isTopBraced = compression face, isBottomBraced = tension face.
 * For wood: isTopBraced = top (compression) flange, isBottomBraced = bottom flange.
 */
@Serializable
data class DiscreteBracePoint(
    val x: Length,
    val isTopBraced: Boolean = true,
    val isBottomBraced: Boolean = false
)

/**
 * A lateral brace point expressed in global member coordinates (measured from
 * the start of the first span). Produced by resolving per-span [BracingInput]
 * into a single member-level brace list before passing to the solver.
 *
 * Contrast with [DiscreteBracePoint], which is span-local and UI-facing.
 * [NormalizedBraceState] is solver-facing and member-global.
 */
@Serializable
data class NormalizedBraceState(
    val x: Length,
    val isTopBraced: Boolean = true,
    val isBotBraced: Boolean = false
)

/**
 * Material-specific bracing input captured from UI forms.
 * Each variant carries the parameters relevant to that material's code provisions.
 *
 * Sealed class ensures the solver always receives a typed, unambiguous input —
 * no string matching or runtime casting needed to dispatch to the right engine.
 */
@Serializable
sealed class BracingInput {

    /**
     * AISC 360 steel bracing configuration.
     * Modes: CONTINUOUS, DISCRETE, UNBRACED.
     * Reference: AISC 360 Appendix 6 — stability bracing for beams.
     */
    @Serializable
    data class Steel(
        val topMode: BracingMode = BracingMode.UNBRACED,
        val bottomMode: BracingMode = BracingMode.UNBRACED,
        val discreteTable: List<DiscreteBracePoint> = emptyList()
    ) : BracingInput()

    /**
     * Aluminum Design Manual (ADM) bracing configuration.
     * Lateral-torsional buckling provisions are structurally analogous to AISC 360
     * but apply ADM Part I Section F slenderness limits and alloy-specific factors.
     * Modes: CONTINUOUS, DISCRETE, UNBRACED.
     * Reference: ADM Part I Section F.
     */
    @Serializable
    data class Aluminum(
        val topMode: BracingMode = BracingMode.UNBRACED,
        val bottomMode: BracingMode = BracingMode.UNBRACED,
        val discreteTable: List<DiscreteBracePoint> = emptyList()
    ) : BracingInput()

    /**
     * TMS 402 masonry bracing configuration.
     *
     * Masonry uses "compression face" and "tension face" rather than flanges.
     * Unreinforced masonry is more sensitive to lateral instability than reinforced —
     * TMS 402 Section 8.3 imposes stricter bracing requirements for unreinforced members.
     *
     * isReinforced drives which TMS 402 chapter provisions the solver applies:
     *   true  → Chapter 9 (reinforced masonry beam provisions)
     *   false → Chapter 8 (unreinforced masonry beam provisions — stricter Lb limits)
     *
     * In DiscreteBracePoint:
     *   isTopBraced    = compression face restraint
     *   isBottomBraced = tension face restraint
     *
     * Reference: TMS 402 Sections 8.3 and 9.3.
     */
    @Serializable
    data class Masonry(
        val compressionFaceMode: BracingMode = BracingMode.UNBRACED,
        val tensionFaceMode: BracingMode = BracingMode.UNBRACED,
        val isReinforced: Boolean = true,
        val discreteTable: List<DiscreteBracePoint> = emptyList()
    ) : BracingInput()

    /**
     * NDS wood bracing configuration.
     * Modes: CONTINUOUS, REPETITIVE_SPACING, DISCRETE, UNBRACED.
     *
     * REPETITIVE_SPACING encodes regular bridging or blocking (e.g. 24" o.c.).
     * luTopSpacing and luBottomSpacing are only active when their respective
     * mode is REPETITIVE_SPACING — BracingResolver ignores them otherwise.
     *
     * Reference: NDS Section 4.4 — lateral support requirements.
     */
    @Serializable
    data class Wood(
        val topMode: BracingMode = BracingMode.CONTINUOUS,
        val bottomMode: BracingMode = BracingMode.UNBRACED,
        val luTopSpacing: Length = 24.0.inches,     // e.g. 24" o.c. bridging
        val luBottomSpacing: Length = 96.0.inches,  // e.g. 8 ft o.c. blocking
        val discreteTable: List<DiscreteBracePoint> = emptyList()
    ) : BracingInput()
}

/**
 * Translates material-specific BracingInput into UnbracedSegment zones
 * consumed by BracingLogic and the capacity solver.
 */
object BracingResolver {

    fun resolveSegments(input: BracingInput, spanLength: Length): List<UnbracedSegment> {
        val lengthInches = spanLength.inches
        val criticalPoints = mutableSetOf(0.0, lengthInches)

        when (input) {
            is BracingInput.Steel, is BracingInput.Aluminum -> {
                val table = when (input) {
                    is BracingInput.Steel    -> input.discreteTable
                    is BracingInput.Aluminum -> input.discreteTable
                    else                     -> emptyList()
                }
                table.forEach { criticalPoints.add(it.x.inches) }
            }

            is BracingInput.Masonry -> {
                input.discreteTable.forEach { criticalPoints.add(it.x.inches) }
            }

            is BracingInput.Wood -> {
                if (input.topMode == BracingMode.REPETITIVE_SPACING
                    && input.luTopSpacing.inches > 0.0) {
                    var current = input.luTopSpacing.inches
                    while (current < lengthInches) {
                        criticalPoints.add(current)
                        current += input.luTopSpacing.inches
                    }
                }
                if (input.bottomMode == BracingMode.REPETITIVE_SPACING
                    && input.luBottomSpacing.inches > 0.0) {
                    var current = input.luBottomSpacing.inches
                    while (current < lengthInches) {
                        criticalPoints.add(current)
                        current += input.luBottomSpacing.inches
                    }
                }
                input.discreteTable.forEach { criticalPoints.add(it.x.inches) }
            }
        }

        val sorted = criticalPoints.toList().sorted()
        val segments = mutableListOf<UnbracedSegment>()

        for (i in 0 until sorted.size - 1) {
            val startX   = sorted[i]
            val endX     = sorted[i + 1]
            val midPoint = startX + (endX - startX) / 2.0

            val lbTop    = calculateActiveLb(midPoint, startX, endX, lengthInches, true, input)
            val lbBottom = calculateActiveLb(midPoint, startX, endX, lengthInches, false, input)

            segments.add(
                UnbracedSegment(
                    startX   = startX.inches,
                    endX     = endX.inches,
                    lbTop    = lbTop.inches,
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
        isTop: Boolean,       // true = top flange/compression face, false = bottom/tension face
        input: BracingInput
    ): Double {
        return when (input) {
            is BracingInput.Steel -> {
                val mode = if (isTop) input.topMode else input.bottomMode
                resolveLbForMode(mode, startX, endX, spanLength, isTop, input.discreteTable)
            }
            is BracingInput.Aluminum -> {
                val mode = if (isTop) input.topMode else input.bottomMode
                resolveLbForMode(mode, startX, endX, spanLength, isTop, input.discreteTable)
            }
            is BracingInput.Masonry -> {
                val mode = if (isTop) input.compressionFaceMode else input.tensionFaceMode
                resolveLbForMode(mode, startX, endX, spanLength, isTop, input.discreteTable)
            }
            is BracingInput.Wood -> {
                val mode = if (isTop) input.topMode else input.bottomMode
                when (mode) {
                    BracingMode.CONTINUOUS         -> 0.0
                    BracingMode.UNBRACED           -> spanLength
                    BracingMode.REPETITIVE_SPACING -> {
                        if (isTop) input.luTopSpacing.inches
                        else input.luBottomSpacing.inches
                    }
                    BracingMode.DISCRETE           ->
                        resolveLbForMode(
                            mode, startX, endX, spanLength, isTop, input.discreteTable
                        )
                }
            }
        }
    }

    private fun resolveLbForMode(
        mode: BracingMode,
        startX: Double,
        endX: Double,
        spanLength: Double,
        isTop: Boolean,
        discreteTable: List<DiscreteBracePoint>
    ): Double {
        return when (mode) {
            BracingMode.CONTINUOUS         -> 0.0
            BracingMode.UNBRACED           -> spanLength
            BracingMode.REPETITIVE_SPACING -> spanLength // Handled upstream for wood; fallback
            BracingMode.DISCRETE           -> {
                val leftBraced  = startX == 0.0 ||
                        hasDiscreteBraceAt(startX, isTop, discreteTable)
                val rightBraced = endX == spanLength ||
                        hasDiscreteBraceAt(endX, isTop, discreteTable)
                if (leftBraced && rightBraced) endX - startX else spanLength
            }
        }
    }

    private fun hasDiscreteBraceAt(
        x: Double,
        isTop: Boolean,
        table: List<DiscreteBracePoint>
    ): Boolean {
        val tolerance = 0.001
        return table.any {
            abs(it.x.inches - x) < tolerance &&
                    (if (isTop) it.isTopBraced else it.isBottomBraced)
        }
    }
}