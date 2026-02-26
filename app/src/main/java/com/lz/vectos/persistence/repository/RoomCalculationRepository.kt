package com.lz.vectos.persistence.repository

import com.lz.vectos.application.repository.CalculationRepository
import com.lz.vectos.application.repository.ProjectRepository
import com.lz.vectos.domain.beam.BeamCalculation
import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.persistence.mapper.RoomPersistenceMapper
import com.lz.vectos.persistence.room.dao.BeamCalculationDao
import com.lz.vectos.persistence.room.dao.CalculationDao
import java.util.UUID

/**
 * Room-backed implementation of [CalculationRepository].
 * Coordinates generic calculation metadata and specific beam data.
 */
class RoomCalculationRepository(
    private val calculationDao: CalculationDao,
    private val beamCalculationDao: BeamCalculationDao,
    private val projectRepository: ProjectRepository
) : CalculationRepository {

    override suspend fun getBeamCalculation(id: UUID): BeamCalculation? {
        val metadataEntity = calculationDao.getById(id) ?: return null
        val beamEntity = beamCalculationDao.getByCalculationId(id) ?: return null
        
        val project = projectRepository.getProject(metadataEntity.projectId) ?: return null
        val metadata = RoomPersistenceMapper.toDomain(metadataEntity)
        
        return RoomPersistenceMapper.toDomain(beamEntity, metadata, project)
    }

    override suspend fun getCalculationsForProject(projectId: UUID): List<CalculationMetadata> {
        return calculationDao.getByProjectId(projectId).map { RoomPersistenceMapper.toDomain(it) }
    }

    override suspend fun saveBeamCalculation(calculation: BeamCalculation) {
        val metadata = calculation.metadata
        val project = calculation.inputs.project
        
        val metadataEntity = RoomPersistenceMapper.toRoomEntity(metadata, project.id, "BEAM_SIMPLY_SUPPORTED")
        val beamEntity = RoomPersistenceMapper.toRoomEntity(calculation)
        
        // Transactional save ensures metadata and payload are saved together
        calculationDao.insertCalculationWithPayload(metadataEntity, beamEntity, beamCalculationDao)
    }

    override suspend fun deleteCalculation(id: UUID) {
        // CalculationRoomEntity delete is cascading to BeamCalculationRoomEntity
        calculationDao.deleteById(id)
    }
}
