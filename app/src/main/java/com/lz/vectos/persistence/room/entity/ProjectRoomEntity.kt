package com.lz.vectos.persistence.room.entity

import com.lz.vectos.domain.structural.BuildingCode
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.structural.Standard
import com.lz.vectos.domain.units.UnitSystem
import com.lz.vectos.domain.beam.MaterialType
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectRoomEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val projectNumber: String?,
    val siteLocation: String?,
    val description: String?,
    val clientName: String?,
    val engineerName: String?,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long,
    
    // Design Context fields
    val unitSystem: UnitSystem,
    val methodology: DesignMethodology,
    val buildingCodeId: String,       // Store ID instead of object
    val loadingStandardId: String,     // Store ID instead of object
    val materialStandardIdsJson: String // Serialized Map<MaterialType, String>
)
