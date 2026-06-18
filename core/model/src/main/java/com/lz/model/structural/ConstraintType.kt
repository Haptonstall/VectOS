package com.lz.model.structural

import kotlinx.serialization.Serializable

@Serializable
enum class ConstraintType {
    FREE,
    FIXED,
    SPRING,
    GAP,
    TENSION_ONLY,
    COMPRESSION_ONLY,
    NONLINEAR_SPRING
}