package com.lz.domain.project

import com.lz.model.regulatory.PrimaryBuildingCode
import com.lz.model.regulatory.asce7.RiskCategory
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.ProjectDesignContext
import com.lz.model.units.UnitSystem
import com.lz.model.util.LocalDateTimeSerializer
import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Global engineering rules and design parameters governing the math engines.
 */
@Serializable
data class ProjectSettings(
    //val buildingCodeId: String = "IBC_2024",
    val buildingCode: PrimaryBuildingCode = PrimaryBuildingCode.IBC_2024,
    val designMethodology: DesignMethodology = DesignMethodology.ASD,
    val unitSystem: UnitSystem = UnitSystem.IMPERIAL,
    val riskCategory: RiskCategory = RiskCategory.II,
    val isWindDesignEnabled: Boolean = true,
    val isSeismicDesignEnabled: Boolean = true
)

/**
 * Geographical site coordinates and address metadata used to track project location.
 */
@Serializable
data class GeographicCoordinates(
    val streetAddress: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val elevationFeet: Double = 0.0,
    val isGeocoded: Boolean = false
) {
    val fullAddressString: String get() = buildString {
        if (streetAddress.isNotBlank()) append("$streetAddress, ")
        if (city.isNotBlank()) append("$city, ")
        if (state.isNotBlank()) append("$state ")
        if (zipCode.isNotBlank()) append(zipCode)
    }.trim().removeSuffix(",")
}

/**
 * Analytical ground motion hazard variables retrieved for the project coordinates.
 */
@Serializable
data class SeismicHazardData(
    val ss: Double = 0.0,
    val s1: Double = 0.0,
    val sds: Double = 0.0,
    val sd1: Double = 0.0,
    val seismicDesignCategory: String = "A",
    val isAuthoritativeOverride: Boolean = false
)

/**
 * The Root Domain Entity representing a distinct project file in VectOS.
 */
@Serializable
data class Project(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val projectNumber: String? = null,
    val description: String? = null,
    val clientName: String? = null,
    val engineerName: String? = null,
    val firmName: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,

    // Strongly-typed sub-domains
    val settings: ProjectSettings = ProjectSettings(),
    val coordinates: GeographicCoordinates = GeographicCoordinates(),
    val seismicData: SeismicHazardData = SeismicHazardData(),
    val designContext: ProjectDesignContext = ProjectDesignContext()
)
