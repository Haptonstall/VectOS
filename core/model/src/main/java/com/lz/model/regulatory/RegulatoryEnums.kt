package com.lz.model.regulatory

import kotlinx.serialization.Serializable

/**
 * Standard referenced editions for AISC structural steel rules.
 *
 * (This file previously also held duplicate, unused `Asce7Edition`/
 * `NdsEdition` definitions — distinct from and incompatible with the real,
 * used ones in com.lz.model.regulatory.asce7/.nds — and a `PrimaryBuildingCode`
 * enum that has been retired in favor of matching real
 * com.lz.model.regulatory.codes.BuildingCode.id strings directly. All three
 * were only consumed by the now-deleted RegulatoryRegistry.kt.)
 */
@Serializable
enum class AiscEdition(val label: String, val publicationYear: Int) {
    AISC_360_10("AISC 360-10", 2010),
    AISC_360_16("AISC 360-16", 2016),
    AISC_360_22("AISC 360-22", 2022)
}
