package com.lz.model.regulatory

/**
 * Typed keys for section citations within a referenced Standard.
 * Values are citation strings, e.g. "ASCE 7-22 Section 26.5"
 */
enum class StandardReferenceKey {
    // Wood (NDS)
    WOOD_ADJUSTMENT_FACTORS,
    WOOD_STABILITY_FACTOR,
    WOOD_BEAM_STABILITY,

    // Wind (ASCE 7)
    WIND_SPEED_TABLE,
    WIND_PRESSURE_COEFFICIENTS,
    WIND_EXPOSURE_CATEGORIES,

    // Concrete (ACI 318)
    CONCRETE_STRENGTH_REDUCTION,
    CONCRETE_MIN_REINFORCEMENT,

    // Steel (AISC 360)
    STEEL_COMPACTNESS_LIMITS,
    STEEL_LATERAL_TORSIONAL_BUCKLING
}