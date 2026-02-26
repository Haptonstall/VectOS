package com.lz.vectos.persistence.room.dao

import androidx.room.*
import com.lz.vectos.persistence.room.entity.BeamCalculationRoomEntity
import java.util.UUID

@Dao
interface BeamCalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(beamCalculation: BeamCalculationRoomEntity)

    @Update
    suspend fun update(beamCalculation: BeamCalculationRoomEntity)

    @Query("SELECT * FROM beam_calculations WHERE calculationId = :calculationId")
    suspend fun getByCalculationId(calculationId: UUID): BeamCalculationRoomEntity?

    @Query("DELETE FROM beam_calculations WHERE calculationId = :calculationId")
    suspend fun deleteByCalculationId(calculationId: UUID)
}
