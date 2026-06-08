package com.lz.data.persistence.room.dao.catalog

import androidx.room.*
import com.lz.data.persistence.room.entity.catalog.AiscSectionRoomEntity

@Dao
interface AiscSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<AiscSectionRoomEntity>)

    @Query("SELECT * FROM aisc_sections")
    suspend fun getAllSections(): List<AiscSectionRoomEntity>

    @Query("SELECT * FROM aisc_sections WHERE id = :id")
    suspend fun getSectionById(id: String): AiscSectionRoomEntity?

    // Added: essential for section picker UI filtering by shape type
    @Query("SELECT * FROM aisc_sections WHERE type = :type")
    suspend fun getSectionsByType(type: String): List<AiscSectionRoomEntity>
}