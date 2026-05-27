package com.lz.vectos.domain.structural

/**
 * Strictly typed keys for engineering references within Building Codes and Standards.
 * Prevents "Magic String" errors and ensures contract-based lookups.
 */
enum class StructuralReferenceKey {
    // Wood (NDS)
    WOOD_ADJUSTMENT_FACTORS,
    WOOD_STABILITY_FACTOR,
    WOOD_BEAM_STABILITY,
    
    // Wind (ASCE/FBC)
    WIND_SPEED_TABLE,
    WIND_PRESSURE_COEFFICIENTS,
    WIND_EXPOSURE_CATEGORIES,
    
    // Concrete (ACI)
    CONCRETE_STRENGTH_REDUCTION,
    CONCRETE_MIN_REINFORCEMENT,
    
    // Steel (AISC)
    STEEL_COMPACTNESS_LIMITS,
    STEEL_LATERAL_TORSIONAL_BUCKLING,
    
    // General / Building Code
    LIVE_LOAD_REDUCTION,
    DEFLECTION_LIMITS_TABLE,
    RISK_CATEGORY_DEFINITIONS,
    LOAD_COMBINATIONS,
    SERVICEABILITY_CRITERIA
}
