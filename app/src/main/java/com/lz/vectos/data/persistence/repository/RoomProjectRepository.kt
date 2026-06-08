package com.lz.vectos.data.repository

import com.lz.domain.repository.ProjectRepository
import com.lz.domain.project.Project
import com.lz.vectos.data.persistence.mapper.RoomPersistenceMapper
import com.lz.vectos.data.persistence.room.dao.ProjectDao
import java.util.UUID

/**
 * Room-backed implementation of [ProjectRepository].
 * Translates domain models to Room entities using [RoomPersistenceMapper].
 */
class RoomProjectRepository(
    private val projectDao: ProjectDao,
    private val mapper: RoomPersistenceMapper
) : ProjectRepository {

    override suspend fun getProject(id: UUID): Project? {
        return projectDao.getById(id)?.let { mapper.toDomain(it) }
    }

    override suspend fun getAllProjects(): List<Project> {
        return projectDao.getAll().map { mapper.toDomain(it) }
    }

    override suspend fun saveProject(project: Project) {
        projectDao.insert(mapper.toRoomEntity(project))
    }

    override suspend fun deleteProject(id: UUID) {
        projectDao.deleteById(id)
    }
}
