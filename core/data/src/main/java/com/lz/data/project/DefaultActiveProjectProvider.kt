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
        // Selecting a real project always means the user is no longer in
        // the Quick Calc flow.
        _isQuickCalcMode.value = false
    }

    // Defaults true: on a cold start no navigation choice has been made yet,
    // and treating that as Quick Calc (prompt for methodology, don't trust
    // the placeholder "Default Project" above) is the safer default than
    // silently inheriting it.
    private val _isQuickCalcMode = MutableStateFlow(true)
    override val isQuickCalcMode: StateFlow<Boolean> = _isQuickCalcMode.asStateFlow()

    override fun setQuickCalcMode(isQuickCalc: Boolean) {
        _isQuickCalcMode.value = isQuickCalc
    }
}
