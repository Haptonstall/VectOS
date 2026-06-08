package com.lz.data.persistence.room.dao.catalog

import androidx.room.*
import com.lz.data.persistence.room.entity.catalog.WoodSectionRoomEntity

@Dao
interface WoodSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<WoodSectionRoomEntity>)

    @Query("SELECT * FROM wood_sections")
    suspend fun getAllSections(): List<WoodSectionRoomEntity>

    @Query("SELECT * FROM wood_sections WHERE id = :id")
    suspend fun getSectionById(id: String): WoodSectionRoomEntity?

    // Added: filter by nominal dimensions for size-range pickers
    @Query("SELECT * FROM wood_sections WHERE nominalWidth = :width ORDER BY nominalDepth ASC")
    suspend fun getSectionsByWidth(width: Double): List<WoodSectionRoomEntity>
}