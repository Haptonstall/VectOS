package com.lz.data.persistence.room.dao.project

import androidx.room.*
import com.lz.data.persistence.room.entity.project.CustomSectionRoomEntity

@Dao
interface CustomSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(section: CustomSectionRoomEntity)

    @Query("SELECT * FROM custom_sections")
    suspend fun getAllSections(): List<CustomSectionRoomEntity>

    @Query("SELECT * FROM custom_sections WHERE id = :id")
    suspend fun getSectionById(id: String): CustomSectionRoomEntity?

    @Delete
    suspend fun delete(section: CustomSectionRoomEntity)
}