package com.lz.data.persistence.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lz.data.persistence.room.entity.AiscSectionRoomEntity
import com.lz.data.persistence.room.entity.CustomSectionRoomEntity
import com.lz.data.persistence.room.entity.WoodSectionRoomEntity

@Dao
interface AiscSectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<AiscSectionRoomEntity>)

    @Query("SELECT * FROM aisc_sections")
    suspend fun getAllSections(): List<AiscSectionRoomEntity>

    @Query("SELECT * FROM aisc_sections WHERE id = :id")
    suspend fun getSectionById(id: String): AiscSectionRoomEntity?

    @Query("SELECT COUNT(*) FROM aisc_sections")
    suspend fun getCount(): Int
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