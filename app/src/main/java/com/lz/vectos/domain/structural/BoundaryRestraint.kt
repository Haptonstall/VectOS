package com.lz.vectos.domain.structural

/**
 * Low-level restraint flags for support conditions.
 */
data class BoundaryRestraint(
    val verticalFixed: Boolean,
    val rotationFixed: Boolean
)
