package com.lz.vectos.persistence.room.dao

import androidx.room.*
import com.lz.vectos.persistence.room.entity.BeamCalculationRoomEntity
import com.lz.vectos.persistence.room.entity.CalculationRoomEntity
import java.util.UUID

@Dao
interface CalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calculation: CalculationRoomEntity)

    @Update
    suspend fun update(calculation: CalculationRoomEntity)

    @Query("SELECT * FROM calculations WHERE id = :id")
    suspend fun getById(id: UUID): CalculationRoomEntity?

    @Query("SELECT * FROM calculations WHERE projectId = :projectId")
    suspend fun getByProjectId(projectId: UUID): List<CalculationRoomEntity>

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteById(id: UUID)

    /**
     * Atomic save for a calculation header and its specific payload.
     * This ensures referential integrity and prevents partial writes.
     */
    @Transaction
    suspend fun insertCalculationWithPayload(
        metadata: CalculationRoomEntity,
        payload: BeamCalculationRoomEntity,
        beamCalculationDao: BeamCalculationDao
    ) {
        insert(metadata)
        beamCalculationDao.insert(payload)
    }
}
