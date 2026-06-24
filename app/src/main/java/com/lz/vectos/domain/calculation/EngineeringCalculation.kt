package com.lz.vectos.domain.calculation

import com.lz.vectos.domain.versioning.CalculationVersion
import java.util.UUID
import java.time.LocalDateTime

/**
 * A polymorphic project-level wrapper for any engineering calculation.
 * This is the primary asset for engineering work within a project.
 */
data class EngineeringCalculation(
    val id: UUID,
    val projectId: UUID,
    val toolId: String,
    val name: String,
    val latestVersion: CalculationVersion,
    val versionHistory: List<CalculationVersion>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
