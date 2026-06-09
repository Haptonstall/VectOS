package com.lz.model.structural

/**
 * Low-level restraint flags for support conditions.
 */
data class BoundaryRestraint(
    val verticalFixed: Boolean,
    val rotationFixed: Boolean
)