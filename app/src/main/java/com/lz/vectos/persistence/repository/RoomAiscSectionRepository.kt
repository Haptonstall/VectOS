package com.lz.vectos.persistence.repository

import com.lz.vectos.domain.beam.*
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.UnitSystem
import com.lz.vectos.persistence.room.dao.AiscSectionDao

/**
 * Implementation of [SectionRepository] that pulls from the Room database.
 */
class RoomAiscSectionRepository(private val aiscDao: AiscSectionDao) : SectionRepository {

    override suspend fun getDatabaseMetadata(material: MaterialType): SectionDatabaseMetadata? {
        if (material != MaterialType.STEEL) return null
        return SectionDatabaseMetadata(
            source = "AISC",
            edition = "15th Edition",
            publicationYear = 2017,
            revisionDate = null,
            units = UnitSystem.IMPERIAL
        )
    }

    override suspend fun getMaterials(): List<MaterialType> = listOf(MaterialType.STEEL)

    override suspend fun getShapeTypes(material: MaterialType): List<ShapeType> {
        if (material != MaterialType.STEEL) return emptyList()
        // Simplification: AISC v15.0 mostly covers these. 
        // In a full impl, we'd query the DB for distinct 'type' and map to ShapeType.
        return listOf(ShapeType.WIDE_FLANGE, ShapeType.CHANNEL, ShapeType.TEE, ShapeType.ANGLE)
    }

    override suspend fun getSections(material: MaterialType, shapeType: ShapeType): List<SectionProfile> {
        if (material != MaterialType.STEEL) return emptyList()
        
        val aiscType = when(shapeType) {
            ShapeType.WIDE_FLANGE -> "W"
            ShapeType.CHANNEL -> "C"
            ShapeType.TEE -> "WT"
            ShapeType.ANGLE -> "L"
            else -> return emptyList()
        }

        val metadata = getDatabaseMetadata(material)
        return aiscDao.getAllSections()
            .filter { it.type == aiscType }
            .map { entity ->
                SectionProfile(
                    id = entity.id,
                    designation = entity.designation,
                    materialType = MaterialType.STEEL,
                    shapeType = shapeType,
                    area = entity.area * 0.00064516, // sq in to sq m
                    depth = Length(entity.depth * 0.0254), // in to m
                    webThickness = entity.webThickness * 0.0254,
                    torsionalConstantJ = entity.torsionalJ * 0.00000041623, // in4 to m4
                    warpingConstantCw = entity.warpingCw * 0.0000000002687, // in6 to m6
                    propertiesStrongAxis = SectionAxisProperties(
                        i = entity.ix * 0.00000041623,
                        s = entity.sx * 0.000016387,
                        z = entity.zx * 0.000016387,
                        r = entity.rx * 0.0254
                    ),
                    propertiesWeakAxis = SectionAxisProperties(
                        i = entity.iy * 0.00000041623,
                        s = entity.sy * 0.000016387,
                        z = entity.zy * 0.000016387,
                        r = entity.ry * 0.0254
                    ),
                    databaseMetadata = metadata
                )
            }
    }

    override suspend fun getSectionById(id: String): SectionProfile? {
        val entity = aiscDao.getSectionById(id) ?: return null
        // Map back to a ShapeType (simplified)
        val shapeType = when(entity.type) {
            "W" -> ShapeType.WIDE_FLANGE
            "C" -> ShapeType.CHANNEL
            "WT" -> ShapeType.TEE
            "L" -> ShapeType.ANGLE
            else -> ShapeType.WIDE_FLANGE
        }
        return getSections(MaterialType.STEEL, shapeType).find { it.id == id }
    }
}
