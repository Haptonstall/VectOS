package com.lz.data.project

import com.lz.domain.project.ActiveProjectProvider
import com.lz.domain.project.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultActiveProjectProvider @Inject constructor() : ActiveProjectProvider {
    private val _activeProject = MutableStateFlow(
        Project(
            name = "Default Project",
            createdAt = LocalDateTime.now()
        )
    )
    override val activeProject: StateFlow<Project> = _activeProject.asStateFlow()

    override fun setActiveProject(project: Project) {
        _activeProject.value = project
    }
}
