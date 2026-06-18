package com.lz.domain.structural

data class DofConstraint(
    val type: ConstraintType,
    val stiffness: Double? = null
)