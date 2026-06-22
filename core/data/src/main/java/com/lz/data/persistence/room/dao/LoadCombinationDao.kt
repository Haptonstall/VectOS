package com.lz.data.persistence.room.dao

import androidx.room.*
import com.lz.data.persistence.room.entity.*

data class CombinationWithFactors(
    @Embedded val combination: LoadCombinationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "combination_id"
    )
    val factors: List<LoadFactorEntity>
)

data class CombinationSetWithDetails(
    @Embedded val set: LoadCombinationSetEntity,
    @Relation(
        entity = LoadCombinationEntity::class,
        parentColumn = "id",
        entityColumn = "set_id"
    )
    val combinations: List<CombinationWithFactors>
)

@Dao
interface LoadCombinationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombinationSet(set: LoadCombinationSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombination(combination: LoadCombinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoadFactors(factors: List<LoadFactorEntity>)

    @Transaction
    @Query("SELECT * FROM load_combination_sets WHERE source_id IN (:sourceIds)")
    suspend fun getCombinationSetsBySources(sourceIds: List<String>): List<CombinationSetWithDetails>

    @Query("SELECT COUNT(*) FROM load_combination_sets")
    suspend fun getCount(): Int
}