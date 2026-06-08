package com.lz.vectos.data.repository

import com.lz.model.structural.MaterialType
import com.lz.model.structural.SectionAxisProperties
import com.lz.model.structural.SectionDatabaseMetadata
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SectionRepository
import com.lz.model.structural.ShapeType
import com.lz.model.structural.SteelProfile
import com.lz.model.units.UnitSystem
import com.lz.model.units.in2
import com.lz.model.units.in3
import com.lz.model.units.in4
import com.lz.model.units.inches
import com.lz.vectos.domain.beam.*
import com.lz.model.units.*
import com.lz.vectos.data.persistence.room.dao.AiscSectionDao

/**
 * Implementation of [com.lz.model.structural.SectionRepository] that pulls from the Room database.
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
                SteelProfile(
                    id = entity.id,
                    designation = entity.designation,
                    shapeType = shapeType,
                    area = entity.area.in2,
                    depth = entity.depth.inches,
                    webThickness = entity.webThickness.inches,
                    flangeWidth = entity.flangeWidth.inches,
                    flangeThickness = entity.flangeThickness.inches,
                    torsionalConstantJ = entity.torsionalJ,
                    warpingConstantCw = entity.warpingCw,
                    propertiesStrongAxis = SectionAxisProperties(
                        i = entity.ix.in4,
                        s = entity.sx.in3,
                        z = entity.zx.in3,
                        r = entity.rx.inches
                    ),
                    propertiesWeakAxis = SectionAxisProperties(
                        i = entity.iy.in4,
                        s = entity.sy.in3,
                        z = entity.zy.in3,
                        r = entity.ry.inches
                    ),
                    databaseMetadata = metadata
                )
            }
    }

    override suspend fun getSectionById(id: String): SectionProfile? {
        val entity = aiscDao.getSectionById(id) ?: return null
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
