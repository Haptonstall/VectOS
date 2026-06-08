package com.lz.vectos.domain.structural

import com.lz.model.structural.CapacityEvaluationStatus
import com.lz.model.structural.SectionOrientation

/**
 * Governing modes for steel flexural failure.
 */
enum class SteelFlexuralMode {
    YIELDING,
    LATERAL_TORSIONAL_BUCKLING,
    LOCAL_BUCKLING_FLANGE,
    LOCAL_BUCKLING_WEB
}

/**
 * Detailed result of steel stability and LTB evaluation.
 */
data class SteelStabilityResult(
    val governingMode: SteelFlexuralMode,
    val nominalMomentCapacityNm: Double,
    val designMomentCapacityNm: Double, // phi * Mn
    val orientation: SectionOrientation,
    val unbracedLengthM: Double,
    val evaluationStatus: CapacityEvaluationStatus
)
