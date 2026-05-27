package com.lz.vectos.domain.project

import com.lz.vectos.domain.structural.BuildingCode
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.units.UnitSystem
import kotlinx.serialization.Serializable

/**
 * Single source of truth for project-level configurations and metadata.
 */
@Serializable
data class ProjectSettings(
    val projectName: String,
    val projectNumber: String? = null,
    val engineerName: String? = null,
    val firmName: String? = null,
    val designMethodology: DesignMethodology = DesignMethodology.ASD,
    val unitSystem: UnitSystem = UnitSystem.IMPERIAL,
    val buildingCode: String = "IBC 2021"
)
