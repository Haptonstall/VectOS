package com.lz.vectos.data.persistence.entity

import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.DesignMethodology
import com.lz.model.regulatory.codes.Standard
import com.lz.model.units.UnitSystem
import com.lz.model.structural.MaterialType
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
