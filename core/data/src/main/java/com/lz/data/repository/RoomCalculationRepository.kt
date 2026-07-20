package com.lz.data.repository

import com.lz.data.persistence.room.dao.CalculationDao
import com.lz.data.persistence.room.mapper.toMetadataDomain
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.repository.CalculationRepository
import java.util.UUID
import javax.inject.Inject

class RoomCalculationRepository @Inject constructor(
    private val calculationDao: CalculationDao
) : CalculationRepository {
    override suspend fun getCalculationsForProject(projectId: UUID): List<CalculationMetadata> {
        return calculationDao.getByProjectId(projectId).map { it.toMetadataDomain() }
    }

    override suspend fun deleteCalculation(id: UUID) {
        calculationDao.deleteById(id)
    }
}
