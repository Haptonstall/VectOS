package com.lz.vectos.persistence.repository

import com.lz.vectos.domain.project.Project
import com.lz.vectos.application.repository.ProjectRepository
import com.lz.vectos.persistence.entity.ProjectEntity
import com.lz.vectos.persistence.mapper.PersistenceMapper
import java.util.UUID

class InMemoryProjectRepository : ProjectRepository {
    private val storage = mutableMapOf<UUID, ProjectEntity>()

    override suspend fun getProject(id: UUID): Project? {
        return storage[id]?.let { PersistenceMapper.toDomain(it) }
    }

    override suspend fun getAllProjects(): List<Project> {
        return storage.values.map { PersistenceMapper.toDomain(it) }
    }

    override suspend fun saveProject(project: Project) {
        val entity = PersistenceMapper.toEntity(project)
        storage[project.id] = entity
    }

    override suspend fun deleteProject(id: UUID) {
        storage.remove(id)
    }
}
