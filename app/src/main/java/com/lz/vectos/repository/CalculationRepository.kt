package com.lz.vectos.repository

import com.lz.vectos.domain.beam.BeamCalculation
import com.lz.vectos.domain.calculation.CalculationMetadata
import java.util.UUID

/**
 * Repository interface for Calculation operations.
 * Exposes only pure domain models.
 */
interface CalculationRepository {
    suspend fun getBeamCalculation(id: UUID): BeamCalculation?
    suspend fun getCalculationsForProject(projectId: UUID): List<CalculationMetadata>
    suspend fun saveBeamCalculation(calculation: BeamCalculation)
    suspend fun deleteCalculation(id: UUID)
}
