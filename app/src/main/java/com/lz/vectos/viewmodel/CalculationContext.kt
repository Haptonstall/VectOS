package com.lz.vectos.viewmodel

import com.lz.vectos.domain.project.Project
import com.lz.vectos.domain.structural.DesignMethodology
import java.util.UUID

sealed class CalculationContext(
    val methodology: DesignMethodology = DesignMethodology.LRFD
) {
    data class ProjectContext(
        val project: Project,
        val method: DesignMethodology = DesignMethodology.LRFD
    ) : CalculationContext(method)

    data class QuickCalcContext(
        val method: DesignMethodology = DesignMethodology.LRFD
    ) : CalculationContext(method)
}
