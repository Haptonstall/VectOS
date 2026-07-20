package com.lz.domain.project

import kotlinx.coroutines.flow.StateFlow

interface ActiveProjectProvider {

    val activeProject: StateFlow<Project>

    fun setActiveProject(
        project: Project
    )

}