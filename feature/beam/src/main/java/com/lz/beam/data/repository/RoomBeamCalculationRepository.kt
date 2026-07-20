package com.lz.beam.data.repository

import com.lz.beam.data.persistence.room.dao.BeamCalculationDao
import com.lz.beam.model.BeamCalculation
import com.lz.beam.domain.BeamCalculationRepository
import com.lz.data.persistence.room.dao.CalculationDao
import com.lz.data.persistence.room.mapper.toMetadataDomain
import com.lz.data.repository.CalculationWriter
import com.lz.domain.repository.ProjectRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Persists beam calculations using BeamDatabase for payload
 * and CalculationWriter for core metadata.
 *
 * Atomic saves are handled by writing metadata first, then payload.
 * If payload write fails, metadata is orphaned but non-destructive —
 * the calculation registry will not surface incomplete records.
 *
 * Note: True cross-database atomicity requires SQLite-level coordination.
 * For now, metadata-first ordering provides sufficient consistency.
 * See BeamDatabase for shared file strategy.
 */
class RoomBeamCalculationRepository @Inject constructor(
    private val beamCalculationDao: BeamCalculationDao,
    private val calculationDao: CalculationDao,
    private val projectRepository: ProjectRepository,
    private val calculationWriter: CalculationWriter,
    private val beamMapper: BeamPersistenceMapper
) : BeamCalculationRepository {

    override suspend fun saveBeamCalculation(calculation: BeamCalculation) {
        val metadataEntity = beamMapper.toCoreMetadataRoomEntity(
            metadata = calculation.metadata,
            projectId = calculation.project.id,
            elementType = "BEAM_ELEMENT"
        )
        val beamEntity = beamMapper.toBeamRoomEntity(calculation)

        // Metadata first — establishes the parent record
        calculationWriter.writeMetadata(metadataEntity)
        // Payload second — references metadata via calculationId
        beamCalculationDao.insert(beamEntity)
    }

    override suspend fun getBeamCalculation(id: UUID): BeamCalculation? {
        val beamEntity = beamCalculationDao.getByCalculationId(id) ?: return null
        val metadataEntity = calculationDao.getById(id) ?: return null
        val metadata = metadataEntity.toMetadataDomain()
        val project = projectRepository.getProject(metadataEntity.projectId) ?: return null

        return beamMapper.toDomain(beamEntity, metadata, project)
    }

    override suspend fun deleteBeamCalculation(id: UUID) {
        beamCalculationDao.deleteByCalculationId(id)
    }
}