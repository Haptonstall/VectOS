package com.lz.vectos.persistence.entity

import java.util.UUID

/**
 * Persistence model for beam-specific calculation data.
 * Stores all engineering values as primitives in Base Units.
 */
data class BeamCalculationEntity(
    val calculationId: UUID,
    
    // Inputs
    val spanMeters: Double,
    val loadValueBase: Double,
    val materialName: String,
    val momentOfInertiaM4: Double,
    val loadTypeName: String,
    val unitSystemName: String,
    
    // Assumptions
    val isLinearElastic: Boolean,
    val isSmallDeflection: Boolean,
    val isSimplySupported: Boolean,
    
    // Results
    val maxBendingMomentNm: Double,
    val maxShearN: Double,
    val maxDeflectionM: Double
)
