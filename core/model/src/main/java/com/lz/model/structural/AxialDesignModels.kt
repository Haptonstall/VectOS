package com.lz.model.structural

/**
 * Result of axial-flexural interaction check (AISC Chapter H).
 */
data class AxialFlexuralInteractionResult(
    val axialUtilization: Double,
    val flexuralUtilization: Double,
    val interactionValue: Double,
    val status: InteractionStatus,
    val governingClause: String,
    val trace: DesignEquationTrace? = null
)