package com.lz.data.repository

import com.lz.model.structural.MaterialType
import com.lz.model.structural.SectionDatabaseMetadata
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SectionRepository
import com.lz.model.structural.ShapeType
import com.lz.model.units.UnitSystem

/**
 * Stubbed implementation of the AISC steel shapes database.
 * Note: Real steel sections are retrieved from the Room database via [RoomAiscSectionRepository].
 */
class AiscSectionRepository : SectionRepository {

    private val metadata = SectionDatabaseMetadata(
        source = "AISC",
        edition = "15th Edition",
        publicationYear = 2017,
        revisionDate = null,
        units = UnitSystem.IMPERIAL
    )

    override suspend fun getDatabaseMetadata(material: MaterialType): SectionDatabaseMetadata? =
        if (material == MaterialType.STEEL) metadata else null

    override suspend fun getMaterials(): List<MaterialType> = listOf(MaterialType.STEEL)

    override suspend fun getShapeTypes(material: MaterialType): List<ShapeType> =
        if (material == MaterialType.STEEL) listOf(ShapeType.WIDE_FLANGE) else emptyList()

    override suspend fun getSections(material: MaterialType, shapeType: ShapeType): List<SectionProfile> = emptyList()

    override suspend fun getSectionById(id: String): SectionProfile? = null
}