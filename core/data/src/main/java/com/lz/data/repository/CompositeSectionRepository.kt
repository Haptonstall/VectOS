package com.lz.data.repository

import com.lz.model.structural.MaterialType
import com.lz.model.structural.SectionDatabaseMetadata
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SectionRepository
import com.lz.model.structural.ShapeType

/**
 * Routes SectionRepository calls to the real backing repository for each
 * material family:
 *
 *   STEEL -> [RoomAiscSectionRepository] (AISC shapes, Room-backed)
 *   WOOD  -> [NdsSectionRepository] (NDS shapes, bundled JSON asset)
 *
 * [AiscSectionRepository] is intentionally NOT wired here — per its own
 * doc comment it's a stub; [RoomAiscSectionRepository] is the real steel
 * source.
 *
 * CONCRETE, COLDFORM, MASONRY, and ALUMINUM have no section database yet
 * and route to empty results rather than failing, so getMaterials() can
 * safely omit them until a real backend exists.
 */
class CompositeSectionRepository(
    private val steelSectionRepository: RoomAiscSectionRepository,
    private val woodSectionRepository: NdsSectionRepository
) : SectionRepository {

    private fun repositoryFor(material: MaterialType): SectionRepository? =
        when (material) {
            MaterialType.STEEL -> steelSectionRepository
            MaterialType.WOOD -> woodSectionRepository
            else -> null
        }

    override suspend fun getDatabaseMetadata(material: MaterialType): SectionDatabaseMetadata? =
        repositoryFor(material)?.getDatabaseMetadata(material)

    override suspend fun getMaterials(): List<MaterialType> =
        steelSectionRepository.getMaterials() + woodSectionRepository.getMaterials()

    override suspend fun getShapeTypes(material: MaterialType): List<ShapeType> =
        repositoryFor(material)?.getShapeTypes(material) ?: emptyList()

    override suspend fun getSections(material: MaterialType, shapeType: ShapeType): List<SectionProfile> =
        repositoryFor(material)?.getSections(material, shapeType) ?: emptyList()

    override suspend fun getSectionById(id: String): SectionProfile? =
        steelSectionRepository.getSectionById(id) ?: woodSectionRepository.getSectionById(id)
}
