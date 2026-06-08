package com.lz.data.persistence.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lz.data.persistence.room.entity.ProjectRoomEntity
import java.util.UUID

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(project: ProjectRoomEntity)

    @Update
    suspend fun update(project: ProjectRoomEntity)

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: UUID): ProjectRoomEntity?

    @Query("SELECT * FROM projects")
    suspend fun getAll(): List<ProjectRoomEntity>

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: UUID)
}
