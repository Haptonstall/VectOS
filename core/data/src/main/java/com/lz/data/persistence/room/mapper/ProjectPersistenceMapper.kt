package com.lz.data.persistence.room.mapper

import com.lz.data.persistence.room.entity.ProjectRoomEntity
import com.lz.domain.project.GeographicCoordinates
import com.lz.domain.project.Project
import com.lz.domain.project.ProjectSettings
import com.lz.domain.project.SeismicHazardData
import java.time.LocalDateTime

fun Project.toRoomEntity(): ProjectRoomEntity = ProjectRoomEntity(
    id = id,
    name = name,
    projectNumber = projectNumber,
    description = description,
    clientName = clientName,
    engineerName = engineerName,
    firmName = firmName,
    createdAtIso = createdAt.toString(),

    // Settings
    buildingCode = settings.buildingCode,
    designMethodology = settings.designMethodology,
    unitSystem = settings.unitSystem,
    riskCategory = settings.riskCategory,
    isWindDesignEnabled = settings.isWindDesignEnabled,
    isSeismicDesignEnabled = settings.isSeismicDesignEnabled,

    // Coordinates
    streetAddress = coordinates.streetAddress,
    city = coordinates.city,
    state = coordinates.state,
    zipCode = coordinates.zipCode,
    latitude = coordinates.latitude,
    longitude = coordinates.longitude,
    elevationFeet = coordinates.elevationFeet,
    isGeocoded = coordinates.isGeocoded,

    // Seismic
    seismicSs = seismicData.ss,
    seismicS1 = seismicData.s1,
    seismicSds = seismicData.sds,
    seismicSd1 = seismicData.sd1,
    seismicDesignCategory = seismicData.seismicDesignCategory,
    isSeismicAuthoritativeOverride = seismicData.isAuthoritativeOverride,

    steelStandardOverride = designContext.steelOverride
)

fun ProjectRoomEntity.toDomain(): Project = Project(
    id = id,
    name = name,
    projectNumber = projectNumber,
    description = description,
    clientName = clientName,
    engineerName = engineerName,
    firmName = firmName,
    createdAt = LocalDateTime.parse(createdAtIso),

    settings = ProjectSettings(
        buildingCode = buildingCode,
        designMethodology = designMethodology,
        unitSystem = unitSystem,
        riskCategory = riskCategory,
        isWindDesignEnabled = isWindDesignEnabled,
        isSeismicDesignEnabled = isSeismicDesignEnabled
    ),

    coordinates = GeographicCoordinates(
        streetAddress = streetAddress,
        city = city,
        state = state,
        zipCode = zipCode,
        latitude = latitude,
        longitude = longitude,
        elevationFeet = elevationFeet,
        isGeocoded = isGeocoded
    ),

    seismicData = SeismicHazardData(
        ss = seismicSs,
        s1 = seismicS1,
        sds = seismicSds,
        sd1 = seismicSd1,
        seismicDesignCategory = seismicDesignCategory,
        isAuthoritativeOverride = isSeismicAuthoritativeOverride
    ),

    designContext = com.lz.model.structural.ProjectDesignContext(
        steelOverride = steelStandardOverride
    )
)
