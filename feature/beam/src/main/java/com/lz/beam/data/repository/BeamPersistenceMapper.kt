package com.lz.beam.data.repository

import com.lz.beam.data.persistence.room.entity.BeamCalculationRoomEntity
import com.lz.beam.model.BeamCalculation
import com.lz.beam.model.BeamCalculationResults
import com.lz.data.persistence.room.entity.CalculationRoomEntity
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.project.Project
import com.lz.model.units.inInches
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneOffset
import java.util.UUID

/**
 * Maps between Beam domain models and Room entities.
 */
class BeamPersistenceMapper(private val json: Json) {

    fun toCoreMetadataRoomEntity(
        metadata: CalculationMetadata,
        projectId: UUID,
        elementType: String
    ): CalculationRoomEntity {
        return CalculationRoomEntity(
            id = metadata.id,
            projectId = projectId,
            name = metadata.name,
            calculationType = elementType,
            createdAtEpoch = metadata.createdAt.toEpochSecond(ZoneOffset.UTC),
            updatedAtEpoch = metadata.createdAt.toEpochSecond(ZoneOffset.UTC)
        )
    }

    fun toBeamRoomEntity(calculation: BeamCalculation): BeamCalculationRoomEntity {
        return BeamCalculationRoomEntity(
            calculationId = calculation.metadata.id,
            memberJson = json.encodeToString(calculation.member),
            resultsJson = json.encodeToString(calculation.results),
            assumptionsJson = json.encodeToString(calculation.assumptions),
            maxBendingMomentLbIn = calculation.results.analysisResult.maxMoment.inLbIn,
            maxShearLbs = calculation.results.analysisResult.maxShear.inPoundsForce,
            maxDeflectionInches = calculation.results.analysisResult.maxDeflection.inInches
        )
    }

    fun toDomain(
        entity: BeamCalculationRoomEntity,
        metadata: CalculationMetadata,
        project: Project
    ): BeamCalculation {
        return BeamCalculation(
            metadata = metadata,
            project = project,
            member = json.decodeFromString(entity.memberJson),
            results = json.decodeFromString(entity.resultsJson),
            assumptions = json.decodeFromString(entity.assumptionsJson)
        )
    }
}
