package com.lz.vectos.viewmodel

import com.lz.vectos.domain.project.Project
import java.util.UUID

sealed class CalculationContext {
    data class ProjectContext(val project: Project) : CalculationContext()
    object QuickCalcContext : CalculationContext()
}
