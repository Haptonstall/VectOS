package com.lz.data.persistence.room.dao

import androidx.room.*
import com.lz.data.persistence.room.entity.*
import kotlinx.coroutines.flow.Flow

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
    val combinationSets: List<CombinationSetWithDetails>,
    @Relation(
        parentColumn = "id",
        entityColumn = "buildingCodeId"
    )
    val defaultMaterialStandards: List<DefaultMaterialStandardEntity>
)

@Dao
interface CodeRegistryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingCode(code: BuildingCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandard(standard: StandardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingCodeStandardCrossRef(crossRef: BuildingCodeStandardCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultMaterialStandard(entity: DefaultMaterialStandardEntity)

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

    @Query("SELECT COUNT(*) FROM building_codes")
    suspend fun getCount(): Int
}