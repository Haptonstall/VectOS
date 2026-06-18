package com.lz.model.structural

import kotlinx.serialization.Serializable

@Serializable
data class DofConstraint(
    val type: ConstraintType = ConstraintType.FREE,
    val stiffness: Double? = null
)