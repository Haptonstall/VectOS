package com.lz.vectos.persistence.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lz.vectos.persistence.room.entity.AiscSectionRoomEntity

@Dao
interface AiscSectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<AiscSectionRoomEntity>)

    @Query("SELECT * FROM aisc_sections")
    suspend fun getAllSections(): List<AiscSectionRoomEntity>

    @Query("SELECT * FROM aisc_sections WHERE id = :id")
    suspend fun getSectionById(id: String): AiscSectionRoomEntity?
}
