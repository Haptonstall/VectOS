package com.lz.domain.repository

import com.lz.domain.calculation.CalculationMetadata
import java.util.UUID

/**
 * Core repository interface managing element-agnostic calculation orchestration.
 * Sits at the foundational core level and remains stable as new modules are added.
 */
interface CalculationRepository {

    /**
     * Retrieves high-level tracking summaries for every design element assigned
     * to a specific project (used to populate the main project tree/dashboard UI).
     */
    suspend fun getCalculationsForProject(projectId: UUID): List<CalculationMetadata>

    /**
     * Deletes any structural calculation record from disk/database by its unique identifier,
     * regardless of its engineering element type.
     */
    suspend fun deleteCalculation(id: UUID)
}
