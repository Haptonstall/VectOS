package com.lz.data.persistence.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lz.model.structural.DesignMethodology
import com.lz.model.units.UnitSystem
import com.lz.model.regulatory.AiscEdition
import com.lz.model.regulatory.asce7.RiskCategory
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectRoomEntity(
    // 1. Root Core Metadata Identities
    @PrimaryKey val id: UUID,
    val name: String,
    val projectNumber: String?,
    val description: String?,
    val clientName: String?,
    val engineerName: String?,
    val firmName: String?,
    val createdAtIso: String, // Kept as ISO String across modules

    // 2. Global Engineering Settings Configuration
    // NOTE: buildingCodeId matches a real com.lz.model.regulatory.codes.BuildingCode.id
    // seeded in the standards/building_codes tables (e.g. "IBC_2024", "CBC_2022") —
    // NOT the old 3-value PrimaryBuildingCode enum, which is retired.
    val buildingCodeId: String,
    val designMethodology: DesignMethodology,
    val unitSystem: UnitSystem,
    val riskCategory: RiskCategory,
    val isWindDesignEnabled: Boolean,
    val isSeismicDesignEnabled: Boolean,

    // 3. Complete Geographic Position Fields
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    val elevationFeet: Double,
    val isGeocoded: Boolean,

    // 4. Authoritative USGS Seismic Design Web Metrics
    val seismicSs: Double,
    val seismicS1: Double,
    val seismicSds: Double,
    val seismicSd1: Double,
    val seismicDesignCategory: String,
    val isSeismicAuthoritativeOverride: Boolean,

    // 5. Active Sub-Module Material Standards Configurations
    // Storing specific enum overrides directly removes fragile JSON map runtime logic
    val steelStandardOverride: AiscEdition? = null
    // val concreteStandardOverride: Aci318Edition? = null
)
