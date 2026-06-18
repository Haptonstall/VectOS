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
    FREE("Free"),
    FIXED("Fixed"),
    PINNED("Pinned"),
    ROLLER_X("Roller X"),
    ROLLER_Y("Roller Y"),
    ROLLER_Z("Roller Z"),
    GUIDE_X("Guide X"),
    GUIDE_Y("Guide Y"),
    GUIDE_Z("Guide Z"),
    CUSTOM("Custom")
}