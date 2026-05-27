package com.lz.vectos.persistence.room.dao

import androidx.room.*
import com.lz.vectos.persistence.room.entity.*

@Dao
interface AiscSectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<AiscSectionRoomEntity>)

    @Query("SELECT * FROM aisc_sections")
    suspend fun getAllSections(): List<AiscSectionRoomEntity>

    @Query("SELECT * FROM aisc_sections WHERE id = :id")
    suspend fun getSectionById(id: String): AiscSectionRoomEntity?
}

@Dao
interface WoodSectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<WoodSectionRoomEntity>)

    @Query("SELECT * FROM wood_sections")
    suspend fun getAllSections(): List<WoodSectionRoomEntity>

    @Query("SELECT * FROM wood_sections WHERE id = :id")
    suspend fun getSectionById(id: String): WoodSectionRoomEntity?
}

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
