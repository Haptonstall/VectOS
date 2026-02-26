package com.lz.vectos.persistence.repository

import com.lz.vectos.domain.beam.BeamCalculation
import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.application.repository.CalculationRepository
import com.lz.vectos.application.repository.ProjectRepository
import com.lz.vectos.persistence.entity.BeamCalculationEntity
import com.lz.vectos.persistence.entity.CalculationEntity
import com.lz.vectos.persistence.mapper.PersistenceMapper
import java.util.UUID

class InMemoryCalculationRepository(
    private val projectRepository: ProjectRepository
) : CalculationRepository {

    private val metadataStorage = mutableMapOf<UUID, CalculationEntity>()
    private val beamStorage = mutableMapOf<UUID, BeamCalculationEntity>()

    override suspend fun getBeamCalculation(id: UUID): BeamCalculation? {
        val metadataEntity = metadataStorage[id] ?: return null
        val beamEntity = beamStorage[id] ?: return null
        
        val project = projectRepository.getProject(metadataEntity.projectId) ?: return null
        val metadata = PersistenceMapper.toDomain(metadataEntity)
        
        return PersistenceMapper.toDomain(beamEntity, metadata, project)
    }

    override suspend fun getCalculationsForProject(projectId: UUID): List<CalculationMetadata> {
        return metadataStorage.values
            .filter { it.projectId == projectId }
            .map { PersistenceMapper.toDomain(it) }
    }

    override suspend fun saveBeamCalculation(calculation: BeamCalculation) {
        val metadata = calculation.metadata
        val project = calculation.inputs.project
        
        val metadataEntity = PersistenceMapper.toEntity(metadata, project.id, "BEAM_SIMPLY_SUPPORTED")
        val beamEntity = PersistenceMapper.toEntity(calculation)
        
        metadataStorage[metadata.id] = metadataEntity
        beamStorage[metadata.id] = beamEntity
    }

    override suspend fun deleteCalculation(id: UUID) {
        metadataStorage.remove(id)
        beamStorage.remove(id)
    }
}
