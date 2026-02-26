package com.lz.vectos.persistence.mapper

import com.lz.vectos.domain.beam.*
import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.domain.project.Project
import com.lz.vectos.domain.units.*
import com.lz.vectos.persistence.room.entity.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Explicit mappers for Room entities.
 * These map directly between Domain models and Room entities.
 */
object RoomPersistenceMapper {

    fun toRoomEntity(domain: Project): ProjectRoomEntity = ProjectRoomEntity(
        id = domain.id,
        name = domain.name,
        description = domain.description,
        clientName = domain.clientName,
        engineerName = domain.engineerName,
        createdAtEpoch = domain.createdAt.toEpochMilli(),
        updatedAtEpoch = Instant.now().toEpochMilli()
    )

    fun toDomain(entity: ProjectRoomEntity): Project = Project(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        clientName = entity.clientName,
        engineerName = entity.engineerName,
        createdAt = entity.createdAtEpoch.toLocalDateTime()
    )

    fun toRoomEntity(domain: CalculationMetadata, projectId: java.util.UUID, type: String): CalculationRoomEntity = CalculationRoomEntity(
        id = domain.id,
        projectId = projectId,
        name = domain.name,
        calculationType = type,
        createdAtEpoch = domain.createdAt.toEpochMilli(),
        updatedAtEpoch = Instant.now().toEpochMilli()
    )

    fun toDomain(entity: CalculationRoomEntity): CalculationMetadata = CalculationMetadata(
        id = entity.id,
        name = entity.name,
        createdAt = entity.createdAtEpoch.toLocalDateTime()
    )

    fun toRoomEntity(domain: BeamCalculation): BeamCalculationRoomEntity = BeamCalculationRoomEntity(
        calculationId = domain.metadata.id,
        spanMeters = domain.inputs.span.meters,
        loadValueBase = domain.inputs.loadValue,
        materialName = domain.inputs.material.name,
        momentOfInertiaM4 = domain.inputs.momentOfInertia.metersToFourth,
        loadTypeName = domain.inputs.loadType.name,
        unitSystemName = domain.inputs.unitSystem.name,
        isLinearElastic = domain.assumptions.linearElastic,
        isSmallDeflection = domain.assumptions.smallDeflection,
        isSimplySupported = domain.assumptions.simplySupported,
        maxBendingMomentNm = domain.results.maxBendingMoment.newtonMeters,
        maxShearN = domain.results.maxShear.newtons,
        maxDeflectionM = domain.results.maxDeflection.meters
    )

    fun toDomain(
        entity: BeamCalculationRoomEntity,
        metadata: CalculationMetadata,
        project: Project
    ): BeamCalculation = BeamCalculation(
        metadata = metadata,
        inputs = BeamInputs(
            project = project,
            span = Length(entity.spanMeters),
            loadValue = entity.loadValueBase,
            material = Material.valueOf(entity.materialName),
            momentOfInertia = MomentOfInertia(entity.momentOfInertiaM4),
            loadType = LoadType.valueOf(entity.loadTypeName),
            unitSystem = UnitSystem.valueOf(entity.unitSystemName)
        ),
        results = BeamResults(
            maxBendingMoment = Moment(entity.maxBendingMomentNm),
            maxShear = Force(entity.maxShearN),
            maxDeflection = Length(entity.maxDeflectionM)
        ),
        assumptions = Assumptions(
            linearElastic = entity.isLinearElastic,
            smallDeflection = entity.isSmallDeflection,
            simplySupported = entity.isSimplySupported
        )
    )

    private fun LocalDateTime.toEpochMilli(): Long = 
        this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun Long.toLocalDateTime(): LocalDateTime = 
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
