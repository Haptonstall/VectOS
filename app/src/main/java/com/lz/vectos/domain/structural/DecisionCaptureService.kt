package com.lz.vectos.domain.structural

import com.lz.model.structural.DesignDecision
import com.lz.model.structural.DesignInterpretation

/**
 * Pure Kotlin service to capture engineering intent based on design interpretations.
 */
object DecisionCaptureService {

    /**
     * Creates a formal decision record from an interpretation.
     */
    fun capture(
        interpretation: DesignInterpretation,
        mechanism: String,
        engineerNote: String? = null
    ): DesignDecision {
        return DesignDecision(
            mechanism = mechanism,
            ratio = interpretation.ratio,
            status = interpretation.status,
            engineerNote = engineerNote
        )
    }
}
