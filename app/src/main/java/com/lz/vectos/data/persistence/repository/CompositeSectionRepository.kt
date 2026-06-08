package com.lz.vectos.data.repository

import com.lz.model.structural.MaterialType
import com.lz.model.structural.SectionDatabaseMetadata
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SectionRepository
import com.lz.model.structural.ShapeType
import com.lz.vectos.domain.beam.*

/**
 * Orchestrates multiple material-specific repositories.
 */
class CompositeSectionRepository(
    private val steelRepo: SectionRepository,
    private val woodRepo: SectionRepository
) : SectionRepository {

    override suspend fun getDatabaseMetadata(material: MaterialType): SectionDatabaseMetadata? = when(material) {
        MaterialType.STEEL -> steelRepo.getDatabaseMetadata(material)
        MaterialType.WOOD -> woodRepo.getDatabaseMetadata(material)
        else -> null
    }

    override suspend fun getMaterials(): List<MaterialType> = listOf(MaterialType.STEEL, MaterialType.WOOD)

    override suspend fun getShapeTypes(material: MaterialType): List<ShapeType> = when(material) {
        MaterialType.STEEL -> steelRepo.getShapeTypes(material)
        MaterialType.WOOD -> woodRepo.getShapeTypes(material)
        else -> emptyList()
    }

    override suspend fun getSections(material: MaterialType, shapeType: ShapeType): List<SectionProfile> = when(material) {
        MaterialType.STEEL -> steelRepo.getSections(material, shapeType)
        MaterialType.WOOD -> woodRepo.getSections(material, shapeType)
        else -> emptyList()
    }

    override suspend fun getSectionById(id: String): SectionProfile? {
        return steelRepo.getSectionById(id) ?: woodRepo.getSectionById(id)
    }
}
