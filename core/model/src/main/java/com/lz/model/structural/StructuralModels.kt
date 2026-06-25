package com.lz.model.structural

import com.lz.model.util.UUIDSerializer
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inches
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single physical span within a multi-span structural member.
 * Contains geometric boundaries and pre-resolved stability segment zones.
 */
@Serializable
data class SpanGeometry(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val length: Length,
    @Serializable(with = UUIDSerializer::class)
    val startNodeId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val endNodeId: UUID,
    val unbracedSegments: List<UnbracedSegment> = emptyList()
) {
    /**
     * Returns the unbraced segment enclosing coordinate x, or a fully-unbraced
     * fallback if none was defined.
     */
    fun getUnbracedSegmentAt(x: Length): UnbracedSegment =
        unbracedSegments.find { it.contains(x) }
            ?: UnbracedSegment(
                startX = 0.0.inches,
                endX = length,
                lbTop = length,
                lbBottom = length
            )
}

/**
 * A generalized multi-span structural member.
 * Boundary conditions are owned by [nodes]; spans reference nodes by UUID.
 */
@Serializable
data class StructuralMember(
    val nodes: List<StructuralNode> = emptyList(),
    val spans: List<SpanGeometry>,
    val sectionProfileId: String? = null
) {
    companion object {
         /**
         * Creates a simple single-span member with the two most common preset
         * boundary conditions. Preset-to-DOF resolution happens here so callers
         * in tests and BeamViewModel do not need to know about NodeBoundaryCondition
         * internals.
         *
         * Default: simply-supported (pinned + roller).
         */
        fun createSimple(
            length: Length,
            startCondition: NodeBoundaryCondition = NodeBoundaryCondition.pinned(),
            endCondition: NodeBoundaryCondition = NodeBoundaryCondition.roller()
       ): StructuralMember {
            val startId = UUID.randomUUID()
            val endId = UUID.randomUUID()
            return StructuralMember(
                nodes = listOf(
                    StructuralNode(id = startId, boundaryCondition = startCondition),
                     StructuralNode(id = endId, boundaryCondition = endCondition)
                ),
                spans = listOf(
                    SpanGeometry(
                        id = UUID.randomUUID(),
                        length = length,
                        startNodeId = startId,
                        endNodeId = endId
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