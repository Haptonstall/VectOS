package com.lz.vectos.domain.calculation

import com.lz.vectos.domain.beam.BeamCalculation
import java.util.UUID

/**
 * Domain repository for Calculation operations.
 * Exposes only pure domain models.
 */
interface CalculationRepository {
    suspend fun getBeamCalculation(id: UUID): BeamCalculation?
    suspend fun getCalculationsForProject(projectId: UUID): List<CalculationMetadata>
    suspend fun saveBeamCalculation(calculation: BeamCalculation)
    suspend fun deleteCalculation(id: UUID)
}
