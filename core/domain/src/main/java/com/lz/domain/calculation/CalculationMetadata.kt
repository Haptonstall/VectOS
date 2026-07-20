package com.lz.domain.calculation

import java.time.LocalDateTime
import java.util.UUID

/**
 * Shared calculation summary used by project-level repositories and feature modules.
 */
data class CalculationMetadata(
    val id: UUID,
    val toolId: String,
    val name: String,
    val createdAt: LocalDateTime
)
