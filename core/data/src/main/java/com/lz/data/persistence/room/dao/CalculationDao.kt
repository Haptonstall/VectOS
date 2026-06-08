package com.lz.data.persistence.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lz.data.persistence.room.entity.CalculationRoomEntity
import java.util.UUID

/**
 * Global DAO managing the element-agnostic calculation inventory metadata shells.
 * Stays completely stable in the core data tier as you add or remove features.
 */
@Dao
interface CalculationDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(calculation: CalculationRoomEntity)

    @Update
    suspend fun update(calculation: CalculationRoomEntity)

    @Query("SELECT * FROM calculations WHERE id = :id")
    suspend fun getById(id: UUID): CalculationRoomEntity?

    @Query("SELECT * FROM calculations WHERE projectId = :projectId")
    suspend fun getByProjectId(projectId: UUID): List<CalculationRoomEntity>

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteById(id: UUID)
}
