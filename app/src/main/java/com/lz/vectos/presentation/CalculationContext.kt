package com.lz.vectos.presentation

import com.lz.domain.project.Project
import com.lz.model.structural.DesignMethodology
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
