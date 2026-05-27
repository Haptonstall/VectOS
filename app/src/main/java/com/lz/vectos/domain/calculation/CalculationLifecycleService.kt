package com.lz.vectos.domain.calculation

import com.lz.vectos.domain.versioning.CalculationVersion
import com.lz.vectos.domain.versioning.CalculationVersioningService
import com.lz.vectos.domain.provenance.CalculationProvenance
import java.time.LocalDateTime
import java.util.UUID

/**
 * Service to manage the lifecycle of engineering calculations.
 * Ensures data integrity and consistent versioning within a project.
 */
class CalculationLifecycleService(
    private val registry: ProjectCalculationRegistry
) {

    /**
     * Creates a new calculation and registers it in the project.
     */
    fun startCalculation(
        projectId: UUID,
        toolId: String,
        name: String,
        initialProvenance: CalculationProvenance
    ): EngineeringCalculation {
        val now = LocalDateTime.now()
        val initialVersion = CalculationVersioningService.createVersion(
            number = 1,
            note = "Initial Calculation",
            current = initialProvenance,
            previous = null
        )

        val calculation = EngineeringCalculation(
            id = UUID.randomUUID(),
            projectId = projectId,
            toolId = toolId,
            name = name,
            latestVersion = initialVersion,
            versionHistory = listOf(initialVersion),
            createdAt = now,
            updatedAt = now
        )

        registry.updateCalculation(calculation)
        return calculation
    }

    /**
     * Records a new revision for an existing calculation.
     */
    fun recordRevision(
        calculation: EngineeringCalculation,
        note: String,
        newProvenance: CalculationProvenance
    ): EngineeringCalculation {
        val nextVersionNumber = calculation.latestVersion.versionNumber + 1
        val newVersion = CalculationVersioningService.createVersion(
            number = nextVersionNumber,
            note = note,
            current = newProvenance,
            previous = calculation.latestVersion.provenance
        )

        val updatedCalculation = calculation.copy(
            latestVersion = newVersion,
            versionHistory = calculation.versionHistory + newVersion,
            updatedAt = LocalDateTime.now()
        )

        registry.updateCalculation(updatedCalculation)
        return updatedCalculation
    }

    /**
     * Safely removes a calculation from the project.
     */
    fun archiveCalculation(id: UUID) {
        registry.removeCalculation(id)
    }
}
