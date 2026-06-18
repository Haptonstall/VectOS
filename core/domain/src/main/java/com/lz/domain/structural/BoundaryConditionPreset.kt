package com.lz.domain.structural

/**
 * Common engineering presets for nodal restraints.
 *
 * These are convenience selections for the UI only.
 * The actual solver uses NodeBoundaryCondition.
 */
enum class BoundaryConditionPreset(
    val displayName: String
) {
    FIXED("Fixed"),
    PINNED("Pinned"),
    ROLLER_X("Roller X"),
    ROLLER_Y("Roller Y"),
    ROLLER_Z("Roller Z"),
    FREE("Free"),
    CUSTOM("Custom")
}