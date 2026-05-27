package com.lz.vectos.persistence.room.dao

import androidx.room.*
import com.lz.vectos.persistence.room.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Composite data class representing a load combination with its associated factors.
 */
data class CombinationWithFactors(
    @Embedded val combination: LoadCombinationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "combination_id"
    )
    val factors: List<LoadFactorEntity>
)

/**
 * Composite data class representing a set of combinations, containing nested combinations and factors.
 */
data class CombinationSetWithDetails(
    @Embedded val set: LoadCombinationSetEntity,
    @Relation(
        entity = LoadCombinationEntity::class,
        parentColumn = "id",
        entityColumn = "set_id"
    )
    val combinations: List<CombinationWithFactors>
)

/**
 * Top-level composite data class for a Building Code, including its Standards and all associated Load Combinations.
 */
data class BuildingCodeWithDetails(
    @Embedded val buildingCode: BuildingCodeEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BuildingCodeStandardCrossRef::class,
            parentColumn = "code_id",
            entityColumn = "standard_id"
        )
    )
    val standards: List<StandardEntity>,

    @Relation(
        entity = LoadCombinationSetEntity::class,
        parentColumn = "id",
        entityColumn = "source_id"
    )
    val combinationSets: List<CombinationSetWithDetails>
)

@Dao
interface StructuralDataDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingCode(code: BuildingCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandard(standard: StandardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingCodeStandardCrossRef(crossRef: BuildingCodeStandardCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombinationSet(set: LoadCombinationSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombination(combination: LoadCombinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoadFactors(factors: List<LoadFactorEntity>)

    @Transaction
    @Query("SELECT * FROM load_combination_sets WHERE source_id IN (:sourceIds)")
    suspend fun getCombinationSetsBySources(sourceIds: List<String>): List<CombinationSetWithDetails>

    @Transaction
    @Query("SELECT * FROM building_codes WHERE id = :codeId")
    fun getBuildingCodeById(codeId: String): Flow<BuildingCodeWithDetails?>

    @Transaction
    @Query("SELECT * FROM building_codes")
    fun getAllBuildingCodes(): Flow<List<BuildingCodeWithDetails>>

    @Query("SELECT * FROM standards")
    fun getAllStandards(): Flow<List<StandardEntity>>

    @Delete
    suspend fun deleteBuildingCode(code: BuildingCodeEntity)
}
