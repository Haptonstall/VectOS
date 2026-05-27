package com.lz.vectos.repository

import com.lz.vectos.domain.structural.BuildingCode
import com.lz.vectos.domain.structural.Standard
import com.lz.vectos.persistence.room.dao.StructuralDataDao
import com.lz.vectos.persistence.room.mapper.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Custom exception thrown when a building code cannot be found in the database.
 */
class CodeNotFoundException(codeId: String) : Exception("Building code with ID '$codeId' not found.")

interface IStructuralCodeRepository {
    suspend fun getBuildingCode(id: String): BuildingCode
    suspend fun getAllBuildingCodes(): List<BuildingCode>
    suspend fun getAllStandards(): List<Standard>
    suspend fun getDefaultBuildingCode(): BuildingCode
}

class StructuralCodeRepositoryImpl(
    private val dao: StructuralDataDao
) : IStructuralCodeRepository {

    override suspend fun getBuildingCode(id: String): BuildingCode {
        if (id == "EMPTY" || id == "None") {
            val emptyStandard = Standard(id = "EMPTY", shortName = "None", longName = "None")
            return BuildingCode(
                id = "EMPTY",
                shortName = "None",
                longName = "None",
                standards = listOf(emptyStandard)
            )
        }
        val codeDetails = dao.getBuildingCodeById(id).first() ?: throw CodeNotFoundException(id)
        
        // Resolve base code without infinite recursion
        val baseCode = codeDetails.buildingCode.base_code_id?.let { baseId ->
            if (baseId == id) null // Prevent simple self-reference
            else {
                val baseDetails = dao.getBuildingCodeById(baseId).first()
                baseDetails?.toDomainModel() // Non-recursive domain model creation
            }
        }

        // Fetch combinations from the standards associated with this code
        val standardIds = codeDetails.standards.map { it.id }
        val combinationSets = dao.getCombinationSetsBySources(standardIds).map { it.toDomainModel() }
        
        return codeDetails.toDomainModel(baseCode).copy(
            stateSpecificCombinations = (codeDetails.combinationSets.map { it.toDomainModel() } + combinationSets).distinctBy { it.id }
        )
    }

    override suspend fun getAllBuildingCodes(): List<BuildingCode> {
        return dao.getAllBuildingCodes().first().map { it.toDomainModel() }
    }

    override suspend fun getAllStandards(): List<Standard> {
        return dao.getAllStandards().first().map { it.toDomainModel() }
    }

    override suspend fun getDefaultBuildingCode(): BuildingCode {
        // Engineering requirement: Defaulting to IBC 2024 if available
        // This can be expanded to be configurable via Datastore in the future.
        return try {
            getBuildingCode("IBC_2024")
        } catch (e: CodeNotFoundException) {
            // Fallback to the first available code if IBC 2024 is not in DB
            getAllBuildingCodes().firstOrNull() ?: throw CodeNotFoundException("Any")
        }
    }
}
