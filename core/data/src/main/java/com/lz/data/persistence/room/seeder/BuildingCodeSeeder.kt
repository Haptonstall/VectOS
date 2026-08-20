package com.lz.data.persistence.room.seeder

import com.lz.data.persistence.room.dao.CodeRegistryDao
import com.lz.data.persistence.room.entity.BuildingCodeEntity
import com.lz.data.persistence.room.entity.BuildingCodeStandardCrossRef
import com.lz.data.persistence.room.entity.DefaultMaterialStandardEntity
import com.lz.data.persistence.room.entity.StandardEntity
import com.lz.model.structural.MaterialType

class BuildingCodeSeeder(private val dao: CodeRegistryDao) {
    suspend fun seed() {
        if (dao.getCount() > 0) return
        seedStandards()
        seedBuildingCodes()
    }

    private suspend fun seedStandards() {
        listOf(
            StandardEntity("ASCE_7_10", "ASCE 7-10", "Minimum Design Loads for Buildings and Other Structures (2010)",
                material_type = null, edition_family = "ASCE7", edition_key = "ASCE_7_10"),
            StandardEntity("ASCE_7_16", "ASCE 7-16", "Minimum Design Loads and Associated Criteria for Buildings and Other Structures (2016)",
                material_type = null, edition_family = "ASCE7", edition_key = "ASCE_7_16"),
            StandardEntity("ASCE_7_22", "ASCE 7-22", "Minimum Design Loads and Associated Criteria for Buildings and Other Structures (2022)",
                material_type = null, edition_family = "ASCE7", edition_key = "ASCE_7_22"),
            StandardEntity("AISC_360_10", "AISC 360-10", "Specification for Structural Steel Buildings (2010)",
                material_type = MaterialType.STEEL.name, edition_family = "AISC360", edition_key = "AISC_360_10"),
            StandardEntity("AISC_360_16", "AISC 360-16", "Specification for Structural Steel Buildings (2016)",
                material_type = MaterialType.STEEL.name, edition_family = "AISC360", edition_key = "AISC_360_16"),
            StandardEntity("AISC_360_22", "AISC 360-22", "Specification for Structural Steel Buildings (2022)",
                material_type = MaterialType.STEEL.name, edition_family = "AISC360", edition_key = "AISC_360_22"),
            StandardEntity("NDS_2015", "NDS 2015", "National Design Specification for Wood Construction (2015)",
                material_type = MaterialType.WOOD.name, edition_family = "NDS", edition_key = "NDS_2015"),
            StandardEntity("NDS_2018", "NDS 2018", "National Design Specification for Wood Construction (2018)",
                material_type = MaterialType.WOOD.name, edition_family = "NDS", edition_key = "NDS_2018"),
            StandardEntity("NDS_2024", "NDS 2024", "National Design Specification for Wood Construction (2024)",
                material_type = MaterialType.WOOD.name, edition_family = "NDS", edition_key = "NDS_2024")
        ).forEach { dao.insertStandard(it) }
    }

    /**
     * Cross-references a building code to its ASCE/AISC/NDS standards and
     * seeds its default per-material standard (used to resolve
     * BuildingCode.defaultMaterialStandards). Typical code-adoption
     * pairings — e.g. IBC 2021 referencing ASCE 7-16 rather than 7-22 —
     * reflect the actual multi-year lag between IBC/ASCE/AISC/NDS release
     * cycles; worth Cody's sign-off since it's domain judgment, not derived
     * from any single source of truth in this codebase.
     */
    private suspend fun linkCodeToStandards(
        codeId: String,
        asceId: String,
        aiscId: String,
        ndsId: String
    ) {
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef(codeId, asceId))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef(codeId, aiscId))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef(codeId, ndsId))

        dao.insertDefaultMaterialStandard(DefaultMaterialStandardEntity(codeId, MaterialType.STEEL, aiscId))
        dao.insertDefaultMaterialStandard(DefaultMaterialStandardEntity(codeId, MaterialType.WOOD, ndsId))
    }

    private suspend fun seedBuildingCodes() {
        // IBC codes
        dao.insertBuildingCode(BuildingCodeEntity("IBC_2015", "IBC 2015", "International Building Code 2015",
            default_asd_set_id = "ASCE_7_10_ASD", default_lrfd_set_id = "ASCE_7_10_LRFD"))
        linkCodeToStandards("IBC_2015", "ASCE_7_10", "AISC_360_10", "NDS_2015")

        dao.insertBuildingCode(BuildingCodeEntity("IBC_2018", "IBC 2018", "International Building Code 2018",
            default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        linkCodeToStandards("IBC_2018", "ASCE_7_16", "AISC_360_16", "NDS_2018")

        dao.insertBuildingCode(BuildingCodeEntity("IBC_2021", "IBC 2021", "International Building Code 2021",
            default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        linkCodeToStandards("IBC_2021", "ASCE_7_16", "AISC_360_16", "NDS_2018")

        dao.insertBuildingCode(BuildingCodeEntity("IBC_2024", "IBC 2024", "International Building Code 2024",
            default_asd_set_id = "ASCE_7_22_ASD", default_lrfd_set_id = "ASCE_7_22_LRFD"))
        linkCodeToStandards("IBC_2024", "ASCE_7_22", "AISC_360_22", "NDS_2024")

        // State codes
        dao.insertBuildingCode(BuildingCodeEntity("CBC_2022", "CBC 2022", "California Building Code 2022",
            base_code_id = "IBC_2021", default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        linkCodeToStandards("CBC_2022", "ASCE_7_16", "AISC_360_16", "NDS_2018")

        dao.insertBuildingCode(BuildingCodeEntity("FBC_2023", "FBC 2023", "Florida Building Code 2023",
            base_code_id = "IBC_2021", default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        linkCodeToStandards("FBC_2023", "ASCE_7_16", "AISC_360_16", "NDS_2018")
    }
}
