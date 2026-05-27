package com.lz.vectos.persistence.repository

import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.beam.SectionRepository
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.MomentOfInertia

/**
 * Stubbed implementation of the AISC steel shapes database.
 */
class AiscSectionRepository : SectionRepository {

    private val sections = listOf(
        SectionProfile(
            id = "aisc_w12x26",
            name = "W12x26",
            materialType = MaterialType.STEEL,
            momentOfInertia = MomentOfInertia(0.00008491),
            area = 0.004935,
            depth = Length(0.310),
            elasticModulus = 0.000547,
            plasticModulus = 0.000610,
            radiusOfGyration = 0.131
        ),
        SectionProfile(
            id = "aisc_w14x90",
            name = "W14x90",
            materialType = MaterialType.STEEL,
            momentOfInertia = MomentOfInertia(0.0004158),
            area = 0.0171,
            depth = Length(0.356),
            elasticModulus = 0.00234,
            plasticModulus = 0.00257,
            radiusOfGyration = 0.156
        ),
        SectionProfile(
            id = "aisc_w18x35",
            name = "W18x35",
            materialType = MaterialType.STEEL,
            momentOfInertia = MomentOfInertia(0.0002123),
            area = 0.00664,
            depth = Length(0.450),
            elasticModulus = 0.000944,
            plasticModulus = 0.00109,
            radiusOfGyration = 0.178
        )
    )

    override suspend fun getAllSections(): List<SectionProfile> = sections

    override suspend fun getSectionsByMaterial(material: MaterialType): List<SectionProfile> {
        return sections.filter { it.materialType == material }
    }

    override suspend fun getSectionById(id: String): SectionProfile? {
        return sections.find { it.id == id }
    }
}
