package com.lz.vectos.data.persistence.mapper

import com.lz.model.structural.MaterialType
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.project.Project
import com.lz.model.structural.ProjectDesignContext
import com.lz.model.regulatory.codes.Standard
import com.lz.model.structural.StructuralMember
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.data.persistence.room.entity.ProjectRoomEntity
import com.lz.data.persistence.room.entity.CalculationRoomEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Mappers for Room entities.
 * These map between Domain models and Room entities, resolving structural IDs
 * via the [com.lz.data.repository.IStructuralCodeRepository].
 */
class RoomPersistenceMapper(
    private val structuralRepository: IStructuralCodeRepository,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    fun toRoomEntity(domain: Project): ProjectRoomEntity = ProjectRoomEntity(
        id = domain.id,
        name = domain.name,
        projectNumber = domain.projectNumber,
        siteLocation = domain.siteLocation,
        description = domain.description,
        clientName = domain.clientName,
        engineerName = domain.engineerName,
        createdAtEpoch = domain.createdAt.toEpochMilli(),
        updatedAtEpoch = Instant.now().toEpochMilli(),
        
        // Context Mapping
        unitSystem = domain.designContext.units,
        methodology = domain.designContext.methodology,
        buildingCodeId = domain.designContext.buildingCode.id,
        loadingStandardId = domain.designContext.loadingStandard.id,
        materialStandardIdsJson = json.encodeToString(
            domain.designContext.materialStandards.mapKeys { it.key.name }.mapValues { it.value.id }
        )
    )

    suspend fun toDomain(entity: ProjectRoomEntity): Project {
        val buildingCode = structuralRepository.getBuildingCode(entity.buildingCodeId)
            
        // For loading standard, we find it in the building code's standards list
        val loadingStandard = buildingCode.standards.find { it.id == entity.loadingStandardId }
            ?: buildingCode.standards.firstOrNull() // Fallback to first if not found
            ?: Standard(id = "EMPTY", shortName = "None", longName = "None")

        val materialIds: Map<String, String> = json.decodeFromString(entity.materialStandardIdsJson)
        val materialStandards = materialIds.map { (matName, stdId) ->
            val matType = MaterialType.valueOf(matName)
            val standard = buildingCode.standards.find { it.id == stdId }
                ?: buildingCode.standards.firstOrNull() // Fallback
                ?: Standard(id = "EMPTY", shortName = "None", longName = "None")
            matType to standard
        }.toMap()

        return Project(
            id = entity.id,
            name = entity.name,
            projectNumber = entity.projectNumber,
            siteLocation = entity.siteLocation,
            description = entity.description,
            clientName = entity.clientName,
            engineerName = entity.engineerName,
            createdAt = entity.createdAtEpoch.toLocalDateTime(),
            designContext = ProjectDesignContext(
                units = entity.unitSystem,
                methodology = entity.methodology,
                buildingCode = buildingCode,
                loadingStandard = loadingStandard,
                materialStandards = materialStandards
            )
        )
    }

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
        memberJson = json.encodeToString(domain.member),
        resultsJson = json.encodeToString(domain.results),
        assumptionsJson = json.encodeToString(domain.assumptions),
        maxBendingMomentLbIn = domain.results.analysisResult.maxMoment.lbIn,
        maxShearLbs = domain.results.analysisResult.maxShear.pounds,
        maxDeflectionInches = domain.results.analysisResult.maxDeflection.inches
    )

    fun toDomain(
        entity: BeamCalculationRoomEntity,
        metadata: CalculationMetadata,
        project: Project
    ): BeamCalculation = BeamCalculation(
        metadata = metadata,
        project = project,
        member = json.decodeFromString<StructuralMember>(entity.memberJson),
        results = json.decodeFromString<BeamCalculationResults>(entity.resultsJson),
        assumptions = json.decodeFromString<Assumptions>(entity.assumptionsJson)
    )

    private fun LocalDateTime.toEpochMilli(): Long = 
        this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun Long.toLocalDateTime(): LocalDateTime = 
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
