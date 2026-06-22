package com.lz.data.persistence.room.seeder

import com.lz.data.persistence.room.dao.CodeRegistryDao
import com.lz.data.persistence.room.entity.BuildingCodeEntity
import com.lz.data.persistence.room.entity.BuildingCodeStandardCrossRef
import com.lz.data.persistence.room.entity.StandardEntity

class BuildingCodeSeeder(private val dao: CodeRegistryDao) {
    suspend fun seed() {
        if (dao.getCount() > 0) return
        seedStandards()
        seedBuildingCodes()
    }

    private suspend fun seedStandards() {
        listOf(
            StandardEntity("ASCE_7_10", "ASCE 7-10", "Minimum Design Loads for Buildings and Other Structures (2010)"),
            StandardEntity("ASCE_7_16", "ASCE 7-16", "Minimum Design Loads and Associated Criteria for Buildings and Other Structures (2016)"),
            StandardEntity("ASCE_7_22", "ASCE 7-22", "Minimum Design Loads and Associated Criteria for Buildings and Other Structures (2022)"),
            StandardEntity("AISC_360_10", "AISC 360-10", "Specification for Structural Steel Buildings (2010)"),
            StandardEntity("AISC_360_16", "AISC 360-16", "Specification for Structural Steel Buildings (2016)"),
            StandardEntity("AISC_360_22", "AISC 360-22", "Specification for Structural Steel Buildings (2022)"),
            StandardEntity("NDS_2015", "NDS 2015", "National Design Specification for Wood Construction (2015)"),
            StandardEntity("NDS_2018", "NDS 2018", "National Design Specification for Wood Construction (2018)"),
            StandardEntity("NDS_2024", "NDS 2024", "National Design Specification for Wood Construction (2024)")
        ).forEach { dao.insertStandard(it) }
    }

    private suspend fun seedBuildingCodes() {
        // IBC codes
        dao.insertBuildingCode(BuildingCodeEntity("IBC_2015", "IBC 2015", "International Building Code 2015",
            default_asd_set_id = "ASCE_7_10_ASD", default_lrfd_set_id = "ASCE_7_10_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef("IBC_2015", "ASCE_7_10"))

        dao.insertBuildingCode(BuildingCodeEntity("IBC_2018", "IBC 2018", "International Building Code 2018",
            default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef("IBC_2018", "ASCE_7_16"))

        dao.insertBuildingCode(BuildingCodeEntity("IBC_2021", "IBC 2021", "International Building Code 2021",
            default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef("IBC_2021", "ASCE_7_16"))

        dao.insertBuildingCode(BuildingCodeEntity("IBC_2024", "IBC 2024", "International Building Code 2024",
            default_asd_set_id = "ASCE_7_22_ASD", default_lrfd_set_id = "ASCE_7_22_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef("IBC_2024", "ASCE_7_22"))

        // State codes
        dao.insertBuildingCode(BuildingCodeEntity("CBC_2022", "CBC 2022", "California Building Code 2022",
            base_code_id = "IBC_2021", default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))

        dao.insertBuildingCode(BuildingCodeEntity("FBC_2023", "FBC 2023", "Florida Building Code 2023",
            base_code_id = "IBC_2021", default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
    }
}