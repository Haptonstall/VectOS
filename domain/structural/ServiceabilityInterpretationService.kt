package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length

/**
 * Pure Kotlin service to interpret serviceability evaluation results.
 */
object ServiceabilityInterpretationService {

    /**
     * Interprets a serviceability result into qualitative feedback.
     */
    fun interpret(result: ServiceabilityResult): DesignInterpretation {
        val ratio = result.utilization
        val criterionName = result.criterion.name
        
        val status = when {
            ratio > 1.0 -> DesignUtilizationStatus.EXCEEDS_CAPACITY
            ratio > 0.85 -> DesignUtilizationStatus.HIGH
            ratio > 0.5 -> DesignUtilizationStatus.MODERATE
            else -> DesignUtilizationStatus.LOW_UTILIZATION
        }

        val explanation = when (status) {
            DesignUtilizationStatus.LOW_UTILIZATION -> "Deflection for $criterionName is well within limits."
            DesignUtilizationStatus.MODERATE -> "Deflection for $criterionName is acceptable for standard finishes."
            DesignUtilizationStatus.HIGH -> "Deflection for $criterionName is approaching the limit."
            DesignUtilizationStatus.EXCEEDS_CAPACITY -> "Actual deflection exceeds the code-defined limit ($criterionName)."
        }

        val advisoryNote = when (status) {
            DesignUtilizationStatus.EXCEEDS_CAPACITY -> "Increase member stiffness or reduce span."
            DesignUtilizationStatus.HIGH -> "Consider impact on brittle finishes or partitions."
            else -> "Member stiffness is adequate for serviceability criteria."
        }

        return DesignInterpretation(
            status = status,
            explanation = explanation,
            advisoryNote = advisoryNote,
            ratio = ratio
        )
    }
}
