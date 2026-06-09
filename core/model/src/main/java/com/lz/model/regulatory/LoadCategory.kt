package com.lz.model.regulatory

/**
 * High-level classification of loads per ASCE 7.
 */
enum class LoadCategory(val label: String, val shortLabel: String) {
    DEAD("Dead Load", "DL"),
    LIVE("Live Load", "LL"),
    ROOF_LIVE("Roof Live Load", "RLL"),
    SNOW("Snow Load", "S"),
    WIND("Wind Load", "W"),
    SEISMIC("Seismic Load", "E"),
    RAIN("Rain Load", "R")
}