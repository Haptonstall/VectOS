package com.lz.vectos.domain.structural

import com.lz.model.structural.DesignInterpretation
import com.lz.model.structural.DesignUtilizationStatus
import com.lz.model.structural.StrengthCheckResult
import java.util.*

/**
 * Pure Kotlin service to interpret raw utilization ratios into qualitative feedback.
 */
object DesignInterpretationService {

    /**
     * Interprets a strength check result into a descriptive model.
     */
    fun interpret(check: StrengthCheckResult<*>, mechanism: String): DesignInterpretation {
        val ratio = check.utilization
        
        val status = when {
            ratio > 1.0 -> DesignUtilizationStatus.EXCEEDS_CAPACITY
            ratio > 0.85 -> DesignUtilizationStatus.HIGH
            ratio > 0.5 -> DesignUtilizationStatus.MODERATE
            else -> DesignUtilizationStatus.LOW_UTILIZATION
        }

        val explanation = when (status) {
            DesignUtilizationStatus.LOW_UTILIZATION -> "Member has significant reserve capacity for $mechanism."
            DesignUtilizationStatus.MODERATE -> "Member is efficiently loaded for $mechanism."
            DesignUtilizationStatus.HIGH -> "Member is approaching capacity limits for $mechanism."
            DesignUtilizationStatus.EXCEEDS_CAPACITY -> "Design demand exceeds calculated capacity for $mechanism."
        }

        val advisoryNote = when (status) {
            DesignUtilizationStatus.EXCEEDS_CAPACITY -> "Consider increasing section size or material grade."
            DesignUtilizationStatus.HIGH -> "Review loading assumptions or connection details."
            else -> "Section appears adequate for current strength demands."
        }

        return DesignInterpretation(
            status = status,
            explanation = explanation,
            advisoryNote = advisoryNote,
            ratio = ratio
        )
    }
}
