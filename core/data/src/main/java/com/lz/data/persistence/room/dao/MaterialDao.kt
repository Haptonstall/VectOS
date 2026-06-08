package com.lz.data.persistence.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lz.data.persistence.room.entity.MaterialRoomEntity
import com.lz.model.structural.MaterialType

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials WHERE type = :type")
    suspend fun getMaterialsByType(type: MaterialType): List<MaterialRoomEntity>

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: String): MaterialRoomEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(materials: List<MaterialRoomEntity>)
}
