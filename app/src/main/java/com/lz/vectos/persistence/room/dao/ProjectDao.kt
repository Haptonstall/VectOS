package com.lz.vectos.persistence.room.dao

import androidx.room.*
import com.lz.vectos.persistence.room.entity.ProjectRoomEntity
import java.util.UUID

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
