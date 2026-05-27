package com.lz.vectos.persistence.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lz.vectos.persistence.room.entity.*

@Dao
interface BuildingCodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandard(standard: StandardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingCode(code: BuildingCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildingCodeStandardCrossRef(crossRef: BuildingCodeStandardCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultMaterialStandard(defaultMaterialStandard: DefaultMaterialStandardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoadCombination(combination: LoadCombinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceabilityCriterion(criterion: ServiceabilityCriterionRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultLoadCase(loadCase: DefaultLoadCaseRoomEntity)

    @Query("SELECT * FROM building_codes WHERE id = :id")
    suspend fun getBuildingCodeById(id: String): BuildingCodeEntity?

    @Query("SELECT * FROM standards WHERE id = :id")
    suspend fun getStandardById(id: String): StandardEntity?

    @Query("SELECT * FROM standards WHERE id IN (SELECT standard_id FROM building_code_standard_cross_ref WHERE code_id = :codeId)")
    suspend fun getStandardsForCode(codeId: String): List<StandardEntity>

    @Query("SELECT * FROM load_combination_sets WHERE source_id = :sourceId")
    suspend fun getLoadCombinationSetsForSource(sourceId: String): List<LoadCombinationSetEntity>

    @Query("SELECT * FROM load_combinations WHERE set_id = :setId")
    suspend fun getLoadCombinationsForSet(setId: String): List<LoadCombinationEntity>

    @Query("SELECT * FROM load_factors WHERE combination_id = :combinationId")
    suspend fun getFactorsForCombination(combinationId: String): List<LoadFactorEntity>

    @Query("SELECT * FROM serviceability_criteria WHERE buildingCodeId = :codeId")
    suspend fun getServiceabilityCriteriaForCode(codeId: String): List<ServiceabilityCriterionRoomEntity>

    @Query("SELECT standard_id FROM building_code_standard_cross_ref WHERE code_id = :codeId")
    suspend fun getStandardIdsForCode(codeId: String): List<String>

    @Query("SELECT * FROM default_material_standards WHERE buildingCodeId = :codeId")
    suspend fun getDefaultMaterialStandardsForCode(codeId: String): List<DefaultMaterialStandardEntity>

    @Query("SELECT * FROM default_load_cases WHERE buildingCodeId = :codeId")
    suspend fun getDefaultLoadCasesForCode(codeId: String): List<DefaultLoadCaseRoomEntity>
}
