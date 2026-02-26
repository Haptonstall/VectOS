package com.lz.vectos.persistence.repository

import com.lz.vectos.application.repository.ProjectRepository
import com.lz.vectos.domain.project.Project
import com.lz.vectos.persistence.mapper.RoomPersistenceMapper
import com.lz.vectos.persistence.room.dao.ProjectDao
import java.util.UUID

/**
 * Room-backed implementation of [ProjectRepository].
 * Translates domain models to Room entities using [RoomPersistenceMapper].
 */
class RoomProjectRepository(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override suspend fun getProject(id: UUID): Project? {
        return projectDao.getById(id)?.let { RoomPersistenceMapper.toDomain(it) }
    }

    override suspend fun getAllProjects(): List<Project> {
        return projectDao.getAll().map { RoomPersistenceMapper.toDomain(it) }
    }

    override suspend fun saveProject(project: Project) {
        projectDao.insert(RoomPersistenceMapper.toRoomEntity(project))
    }

    override suspend fun deleteProject(id: UUID) {
        projectDao.deleteById(id)
    }
}
