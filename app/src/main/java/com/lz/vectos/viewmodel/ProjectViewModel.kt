package com.lz.vectos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.vectos.domain.project.Project
import com.lz.vectos.application.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class ProjectViewModel(private val projectRepository: ProjectRepository) : ViewModel() {

    private val _activeProject = MutableStateFlow(createDefaultProject())
    val activeProject: StateFlow<Project> = _activeProject

    init {
        viewModelScope.launch {
            projectRepository.saveProject(_activeProject.value)
        }
    }

    private fun createDefaultProject(): Project {
        return Project(
            id = UUID.randomUUID(),
            name = "Default Project",
            description = "A temporary project for calculations.",
            clientName = "Internal",
            engineerName = "Default User",
            createdAt = LocalDateTime.now()
        )
    }

    fun setActiveProject(project: Project) {
        _activeProject.value = project
    }
}
