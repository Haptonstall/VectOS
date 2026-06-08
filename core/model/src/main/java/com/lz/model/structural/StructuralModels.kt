package com.lz.model.structural

import com.lz.model.UUIDSerializer
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inches
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Supported physical boundary conditions for structural member span nodes.
 */
@Serializable
enum class SupportCondition {
    PINNED,
    FIXED,
    ROLLER,
    FREE,
    CUSTOM
}

/**
 * Represents a single physical span within a multi-span structural member.
 * Contains geometric boundaries and pre-resolved stability segment zones.
 */
@Serializable
data class SpanGeometry(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val length: Length,
    val startSupport: SupportCondition,
    val endSupport: SupportCondition,

    // Crucial for the Heat Map Engine: The material-agnostic interval zones
    // calculated by the BracingResolver from your user inputs.
    val unbracedSegments: List<UnbracedSegment> = emptyList()
) {
    /**
     * Finds the active unbraced lengths (Lb) governing a specific calculation station coordinate.
     * Maps the analytical station straight to its corresponding spatial zone.
     */
    fun getUnbracedSegmentAt(x: Length): UnbracedSegment {
        // Find the precise zone enclosing coordinate X
        val matchingSegment = unbracedSegments.find { it.contains(x) }

        // Fallback: If no segments were generated, treat the entire span as completely unbraced
        return matchingSegment ?: UnbracedSegment(
            startX = 0.0.inches,
            endX = length,
            lbTop = length,
            lbBottom = length
        )
    }
}

/**
 * A generalized multi-span structural member segment (e.g., a continuous beam or column line).
 */
@Serializable
data class StructuralMember(
    val spans: List<SpanGeometry>,
    val sectionProfileId: String? = null
) {
    companion object {
        /**
         * Factory utility to quickly instantiate a standard single-span member.
         */
        fun createSimple(
            length: Length,
            startSupport: SupportCondition = SupportCondition.PINNED,
            endSupport: SupportCondition = SupportCondition.ROLLER
        ): StructuralMember {
            return StructuralMember(
                spans = listOf(
                    SpanGeometry(
                        id = UUID.randomUUID(),
                        length = length,
                        startSupport = startSupport,
                        endSupport = endSupport,
                        unbracedSegments = emptyList() // Defaults to fully unbraced over its length
                    )
                )
            )
        }
    }
}

/**
 * Encapsulates the multi-criteria capacities calculated for a cross-section profile
 * at a specific station point.
 */
@Serializable
data class SectionCapacity(
    val nominalMomentCapacity: Moment,
    val nominalShearCapacity: Force,
    val designMomentCapacity: Moment,
    val designShearCapacity: Force,
    val governingMode: String = "Yielding",
    val evaluationSummary: Map<String, CapacityEvaluationStatus> = emptyMap()
)

/**
 * Tracing metrics evaluating cross-sectional code compliance checks.
 */
@Serializable
sealed class CapacityEvaluationStatus {
    @Serializable
    data object NotApplicable : CapacityEvaluationStatus()

    @Serializable
    data class NotEvaluated(val reason: String) : CapacityEvaluationStatus()

    @Serializable
    data class Evaluated(val value: Double, val reference: String) : CapacityEvaluationStatus()
}