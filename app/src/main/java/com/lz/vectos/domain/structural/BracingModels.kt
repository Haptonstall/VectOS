package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.inches
import kotlinx.serialization.Serializable

/**
 * Bracing modes for Steel/General materials.
 */
@Serializable
enum class BracingMode(val label: String) {
    CONTINUOUS("Continuous"),
    DISCRETE("Discrete Points"),
    UNBRACED("Unbraced (Ends Only)")
}

/**
 * A single discrete bracing point, used in the interactive table.
 */
@Serializable
data class DiscreteBracePoint(
    val x: Length,
    val isTopBraced: Boolean = true,
    val isBottomBraced: Boolean = false
)

/**
 * Material-specific bracing configurations.
 */
@Serializable
sealed class BracingInput {
    @Serializable
    data class Steel(
        val topMode: BracingMode = BracingMode.CONTINUOUS,
        val bottomMode: BracingMode = BracingMode.UNBRACED,
        val discreteTable: List<DiscreteBracePoint> = emptyList()
    ) : BracingInput()

    @Serializable
    data class Wood(
        val luTop: Length = Length(24.0), // Default 24 inches
        val luBottom: Length = Length(96.0) // Default 8 feet
    ) : BracingInput()
}

/**
 * The "Normalized" output format required by the discrete logic engine.
 */
data class NormalizedBraceState(
    val x: Double, // Position in inches
    val isTopBraced: Boolean,
    val isBotBraced: Boolean
)

/**
 * Normalizes user inputs into a discrete state array for capacity checking.
 */
fun normalizeBracing(
    input: BracingInput,
    analysisPoints: List<Double>,
    memberLength: Length
): List<NormalizedBraceState> {
    val L = memberLength.inches
    val tolerance = 0.001

    return analysisPoints.map { x ->
        val isAtEnds = x < tolerance || x > (L - tolerance)
        
        val (topBraced, botBraced) = when (input) {
            is BracingInput.Steel -> {
                val top = when (input.topMode) {
                    BracingMode.CONTINUOUS -> true
                    BracingMode.UNBRACED -> isAtEnds
                    BracingMode.DISCRETE -> isAtEnds || input.discreteTable.any { 
                        Math.abs(it.x.inches - x) < tolerance && it.isTopBraced 
                    }
                }
                val bot = when (input.bottomMode) {
                    BracingMode.CONTINUOUS -> true
                    BracingMode.UNBRACED -> isAtEnds
                    BracingMode.DISCRETE -> isAtEnds || input.discreteTable.any { 
                        Math.abs(it.x.inches - x) < tolerance && it.isBottomBraced 
                    }
                }
                top to bot
            }
            is BracingInput.Wood -> {
                val top = isAtEnds || (x % input.luTop.inches < tolerance || x % input.luTop.inches > input.luTop.inches - tolerance)
                val bot = isAtEnds || (x % input.luBottom.inches < tolerance || x % input.luBottom.inches > input.luBottom.inches - tolerance)
                top to bot
            }
        }

        NormalizedBraceState(x = x, isTopBraced = topBraced, isBotBraced = botBraced)
    }
}
