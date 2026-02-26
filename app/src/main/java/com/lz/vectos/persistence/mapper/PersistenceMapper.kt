package com.lz.vectos.persistence.mapper

import com.lz.vectos.domain.beam.*
import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.domain.project.Project
import com.lz.vectos.domain.units.*
import com.lz.vectos.persistence.entity.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Explicit mappers for translating between Domain and Persistence models.
 * 
 * Rules strictly followed:
 * - Manual mapping (no reflection/serialization).
 * - Domain is primary (units are wrapped during reconstruction).
 * - Persistence is secondary (primitives only).
 * - Persistence stores Base Units only.
 */
object PersistenceMapper {

    // --- PROJECT MAPPING ---

    fun toEntity(domain: Project): ProjectEntity = ProjectEntity(
        id = domain.id,
        name = domain.name,
        description = domain.description,
        clientName = domain.clientName,
        engineerName = domain.engineerName,
        createdAtEpoch = domain.createdAt.toEpochMilli(),
        updatedAtEpoch = Instant.now().toEpochMilli() // Use now as update time
    )

    fun toDomain(entity: ProjectEntity): Project = Project(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        clientName = entity.clientName,
        engineerName = entity.engineerName,
        createdAt = entity.createdAtEpoch.toLocalDateTime()
    )

    // --- CALCULATION METADATA MAPPING ---

    fun toEntity(domain: CalculationMetadata, projectId: java.util.UUID, type: String): CalculationEntity = CalculationEntity(
        id = domain.id,
        projectId = projectId,
        name = domain.name,
        calculationType = type,
        createdAtEpoch = domain.createdAt.toEpochMilli(),
        updatedAtEpoch = Instant.now().toEpochMilli()
    )

    fun toDomain(entity: CalculationEntity): CalculationMetadata = CalculationMetadata(
        id = entity.id,
        name = entity.name,
        createdAt = entity.createdAtEpoch.toLocalDateTime()
    )

    // --- BEAM CALCULATION MAPPING ---

    fun toEntity(domain: BeamCalculation): BeamCalculationEntity = BeamCalculationEntity(
        calculationId = domain.metadata.id,
        
        // Inputs (Flattened to base unit primitives)
        spanMeters = domain.inputs.span.meters,
        loadValueBase = domain.inputs.loadValue,
        materialName = domain.inputs.material.name,
        momentOfInertiaM4 = domain.inputs.momentOfInertia.metersToFourth,
        loadTypeName = domain.inputs.loadType.name,
        unitSystemName = domain.inputs.unitSystem.name,
        
        // Assumptions
        isLinearElastic = domain.assumptions.linearElastic,
        isSmallDeflection = domain.assumptions.smallDeflection,
        isSimplySupported = domain.assumptions.simplySupported,
        
        // Results (Flattened to base unit primitives)
        maxBendingMomentNm = domain.results.maxBendingMoment.newtonMeters,
        maxShearN = domain.results.maxShear.newtons,
        maxDeflectionM = domain.results.maxDeflection.meters
    )

    fun toDomain(
        entity: BeamCalculationEntity,
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

    // --- HELPERS (INTERNAL USE ONLY) ---

    private fun LocalDateTime.toEpochMilli(): Long = 
        this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun Long.toLocalDateTime(): LocalDateTime = 
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
