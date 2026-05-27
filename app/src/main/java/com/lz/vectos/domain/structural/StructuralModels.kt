package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length

/**
 * Supported boundary conditions for structural members.
 */
enum class SupportCondition {
    PINNED,
    FIXED,
    ROLLER,
    FREE
}

/**
 * A generalized structural member (e.g., beam or column segment).
 * Defined purely by geometry and boundary conditions in base units.
 */
data class StructuralMember(
    val length: Length,
    val sectionProfileId: String?,
    val startSupport: SupportCondition,
    val endSupport: SupportCondition
)
