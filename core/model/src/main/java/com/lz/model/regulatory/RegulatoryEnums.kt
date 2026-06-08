package com.lz.model.regulatory

import kotlinx.serialization.Serializable

/**
 * Standard referenced editions for ASCE 7 structural loading rules.
 */
@Serializable
enum class Asce7Edition(val label: String, val publicationYear: Int) {
    ASCE_7_16("ASCE 7-16", 2016),
    ASCE_7_22("ASCE 7-22", 2022)
}

/**
 * Standard referenced editions for AISC structural steel rules.
 */
@Serializable
enum class AiscEdition(val label: String, val publicationYear: Int) {
    AISC_360_16("AISC 360-16", 2016),
    AISC_360_22("AISC 360-22", 2022)
}

/**
 * Standard referenced editions for NDS wood rules.
 */
@Serializable
enum class NdsEdition(val label: String, val publicationYear: Int) {
    NDS_12("NDS 2012", 2012),
    NDS_15("NDS 2015", 2015),
    NDS_18("NDS 2018", 2018),
    NDS_24("NDS 2024", 2024)
}

/**
 * Standard legal reference IDs for the primary parent building codes
 * selected by the user inside the Project workspace.
 */
@Serializable
enum class PrimaryBuildingCode(val label: String) {
    IBC_2021("2021 International Building Code"),
    IBC_2024("2024 International Building Code"),
    CBC_2025("2025 California Building Code") // Explicitly treated as a top-level selection option
}

