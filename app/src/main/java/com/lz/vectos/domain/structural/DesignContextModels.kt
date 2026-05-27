package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.UnitSystem

enum class IbcEdition(val label: String) {
    IBC_2018("IBC 2018"),
    IBC_2021("IBC 2021"),
    IBC_2024("IBC 2024")
}

enum class AsceEdition(val label: String) {
    ASCE_7_16("ASCE 7-16"),
    ASCE_7_22("ASCE 7-22")
}

enum class AiscEdition(val label: String) {
    AISC_360_16("AISC 360-16"),
    AISC_360_22("AISC 360-22")
}

data class ProjectDesignContext(
    val units: UnitSystem,
    val methodology: DesignMethodology,
    val ibcEdition: IbcEdition,
    val asceEdition: AsceEdition,
    val aiscEdition: AiscEdition
)
