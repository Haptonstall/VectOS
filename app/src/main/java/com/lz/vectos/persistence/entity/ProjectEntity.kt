package com.lz.vectos.persistence.entity

import com.lz.vectos.domain.structural.BuildingCode
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.structural.Standard
import com.lz.vectos.domain.units.UnitSystem
import com.lz.vectos.domain.beam.MaterialType
import java.util.UUID

/**
 * Persistence model for a Project.
 * Stores core metadata using primitive types and context configuration.
 */
data class ProjectEntity(
    val id: UUID,
    val name: String,
    val description: String?,
    val clientName: String?,
    val engineerName: String?,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long,
    
    // Design Context fields
    val unitSystem: UnitSystem,
    val methodology: DesignMethodology,
    val buildingCode: BuildingCode,
    val loadingStandard: Standard,
    val materialStandards: Map<MaterialType, Standard>
)
