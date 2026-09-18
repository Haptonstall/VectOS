package com.lz.domain.project

import kotlinx.coroutines.flow.StateFlow

interface ActiveProjectProvider {

    val activeProject: StateFlow<Project>

    fun setActiveProject(
        project: Project
    )

    // True when the user entered a calculation tool via "Quick Calc" rather
    // than through a specific project. Quick Calc has no project to inherit
    // a default design methodology from, so the tool should prompt for
    // ASD/LRFD up front; a project-launched calculation already has one via
    // [activeProject]'s settings and should never prompt.
    val isQuickCalcMode: StateFlow<Boolean>

    fun setQuickCalcMode(
        isQuickCalc: Boolean
    )

}