package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.Moment
import com.lz.vectos.domain.units.Force
import com.lz.vectos.util.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Supported boundary conditions for structural members.
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
 * A single span within a structural member.
 */
@Serializable
data class SpanGeometry(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val length: Length,
    val startSupport: SupportCondition,
    val endSupport: SupportCondition,
    val bracing: SpanBracing = SpanBracing()
)

/**
 * A generalized structural member (e.g., beam or column segment).
 * Defined by one or more spans in base units.
 */
@Serializable
data class StructuralMember(
    val spans: List<SpanGeometry>,
    val sectionProfileId: String? = null
) {
    companion object {
        fun createSimple(
            length: Length,
            startSupport: SupportCondition = SupportCondition.PINNED,
            endSupport: SupportCondition = SupportCondition.ROLLER
        ): StructuralMember {
            return StructuralMember(
                spans = listOf(
                    SpanGeometry(
                        id = java.util.UUID.randomUUID(),
                        length = length,
                        startSupport = startSupport,
                        endSupport = endSupport
                    )
                )
            )
        }
    }
}

/**
 * Encapsulates the calculated capacities of a structural section.
 */
data class SectionCapacity(
    val nominalMomentCapacity: Moment,
    val nominalShearCapacity: Force,
    val designMomentCapacity: Moment,
    val designShearCapacity: Force,
    val governingMode: String = "Yielding",
    val evaluationSummary: Map<String, CapacityEvaluationStatus> = emptyMap()
)

sealed class CapacityEvaluationStatus {
    object NotApplicable : CapacityEvaluationStatus()
    data class NotEvaluated(val reason: String) : CapacityEvaluationStatus()
    data class Evaluated(val value: Double, val reference: String) : CapacityEvaluationStatus()
}
