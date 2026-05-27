package com.lz.vectos.domain.beam

import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.MomentOfInertia

/**
 * Standardized engineering section profile.
 */
data class SectionProfile(
    val id: String,
    val name: String,
    val material: Material,
    val momentOfInertia: MomentOfInertia,
    val area: Double, // Future use
    val depth: Length // Future use
)

/**
 * Data-only container for section property lookups.
 */
interface SectionRepository {
    suspend fun getAllSections(): List<SectionProfile>
    suspend fun getSectionsByMaterial(material: Material): List<SectionProfile>
    suspend fun getSectionById(id: String): SectionProfile?
}
