package com.lz.vectos.repository

import com.lz.vectos.domain.project.Project
import java.util.UUID

/**
 * Repository interface for Project operations.
 * Exposes only pure domain models.
 */
interface ProjectRepository {
    suspend fun getProject(id: UUID): Project?
    suspend fun getAllProjects(): List<Project>
    suspend fun saveProject(project: Project)
    suspend fun deleteProject(id: UUID)
}
