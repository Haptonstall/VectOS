package com.lz.beam.data.persistence.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lz.beam.data.persistence.room.entity.BeamCalculationRoomEntity
import java.util.UUID

@Dao
interface BeamCalculationDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(beamCalculation: BeamCalculationRoomEntity)

    @Update
    suspend fun update(beamCalculation: BeamCalculationRoomEntity)

    @Query("SELECT * FROM beam_calculations WHERE calculationId = :calculationId")
    suspend fun getByCalculationId(calculationId: UUID): BeamCalculationRoomEntity?

    @Query("DELETE FROM beam_calculations WHERE calculationId = :calculationId")
    suspend fun deleteByCalculationId(calculationId: UUID)

    @Query("SELECT * FROM beam_calculations")
    suspend fun getAll(): List<BeamCalculationRoomEntity>
}