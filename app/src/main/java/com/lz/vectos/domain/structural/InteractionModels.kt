package com.lz.vectos.domain.structural

import com.lz.model.structural.DesignEquationTrace

/**
 * Result of a flexure-shear interaction check (AISC Chapter H).
 */
data class FlexureShearInteractionResult(
    val flexuralUtilization: Double,
    val shearUtilization: Double,
    val interactionValue: Double,
    val interactionEquation: String,
    val interactionStatus: InteractionStatus,
    val governingClause: String,
    val trace: DesignEquationTrace? = null
)

enum class InteractionStatus {
    PASS,
    FAIL,
    NOT_EVALUATED,
    NOT_APPLICABLE
}
