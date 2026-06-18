package com.lz.data.repository

import com.lz.data.persistence.room.dao.ProjectDao
import com.lz.data.persistence.room.mapper.toDomain
import com.lz.data.persistence.room.mapper.toRoomEntity
import com.lz.domain.project.Project
import com.lz.domain.repository.ProjectRepository
import java.util.UUID

class RoomProjectRepository(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override suspend fun getProject(id: UUID): Project? =
        projectDao.getById(id)?.toDomain()

    override suspend fun getAllProjects(): List<Project> =
        projectDao.getAll().map { it.toDomain() }

    override suspend fun saveProject(project: Project) =
        projectDao.insert(project.toRoomEntity())

    override suspend fun deleteProject(id: UUID) =
        projectDao.deleteById(id)
}