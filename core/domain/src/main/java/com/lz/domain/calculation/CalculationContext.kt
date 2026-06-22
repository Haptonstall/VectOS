package com.lz.domain.calculation

import com.lz.domain.project.Project
import com.lz.model.structural.DesignMethodology

sealed class CalculationContext(
    val methodology: DesignMethodology = DesignMethodology.ASD
) {
    data class ProjectContext(
        val project: Project,
        val method: DesignMethodology = DesignMethodology.ASD
    ) : CalculationContext(method)

    data class QuickCalcContext(
        val method: DesignMethodology = DesignMethodology.ASD
    ) : CalculationContext(method)
}