package com.lz.vectos.domain.project

import java.util.UUID

/**
 * Domain repository for Project operations.
 * Exposes only pure domain models.
 */
interface ProjectRepository {
    suspend fun getProject(id: UUID): Project?
    suspend fun getAllProjects(): List<Project>
    suspend fun saveProject(project: Project)
    suspend fun deleteProject(id: UUID)
}
