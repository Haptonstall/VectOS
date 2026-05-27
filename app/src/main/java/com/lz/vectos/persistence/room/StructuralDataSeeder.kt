package com.lz.vectos.persistence.room

import com.lz.vectos.domain.structural.CombinationType
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.structural.LoadCategory
import com.lz.vectos.persistence.room.dao.StructuralDataDao
import com.lz.vectos.persistence.room.entity.*

/**
 * Seeder to populate the database with International Building Codes, State Codes,
 * and their associated load combinations (ASD & LRFD).
 */
class StructuralDataSeeder(
    private val dao: StructuralDataDao
) {
    suspend fun seed() {
        seedStandards()
        seedIBC2015()
        seedIBC2018()
        seedIBC2021()
        seedIBC2024()
        seedStateCodes()
    }

    private suspend fun seedStandards() {
        val standards = listOf(
            StandardEntity("ASCE_7_10", "ASCE 7-10", "Minimum Design Loads for Buildings and Other Structures (2010)"),
            StandardEntity("ASCE_7_16", "ASCE 7-16", "Minimum Design Loads and Associated Criteria for Buildings and Other Structures (2016)"),
            StandardEntity("ASCE_7_22", "ASCE 7-22", "Minimum Design Loads and Associated Criteria for Buildings and Other Structures (2022)"),
            StandardEntity("AISC_360_10", "AISC 360-10", "Specification for Structural Steel Buildings (2010)"),
            StandardEntity("AISC_360_16", "AISC 360-16", "Specification for Structural Steel Buildings (2016)"),
            StandardEntity("AISC_360_22", "AISC 360-22", "Specification for Structural Steel Buildings (2022)"),
            StandardEntity("NDS_2015", "NDS 2015", "National Design Specification for Wood Construction (2015)"),
            StandardEntity("NDS_2018", "NDS 2018", "National Design Specification for Wood Construction (2018)"),
            StandardEntity("NDS_2024", "NDS 2024", "National Design Specification for Wood Construction (2024)")
        )
        standards.forEach { dao.insertStandard(it) }
    }

    private suspend fun seedIBC2015() {
        val codeId = "IBC_2015"
        dao.insertBuildingCode(BuildingCodeEntity(codeId, "IBC 2015", "International Building Code 2015", 
            default_asd_set_id = "ASCE_7_10_ASD", default_lrfd_set_id = "ASCE_7_10_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef(codeId, "ASCE_7_10"))
        seedAsce710Combinations()
    }

    private suspend fun seedIBC2018() {
        val codeId = "IBC_2018"
        dao.insertBuildingCode(BuildingCodeEntity(codeId, "IBC 2018", "International Building Code 2018", 
            default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef(codeId, "ASCE_7_16"))
        seedAsce716Combinations()
    }

    private suspend fun seedIBC2021() {
        val codeId = "IBC_2021"
        dao.insertBuildingCode(BuildingCodeEntity(codeId, "IBC 2021", "International Building Code 2021", 
            default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef(codeId, "ASCE_7_16"))
        // Combinations already seeded by IBC 2018 (ASCE 7-16)
    }

    private suspend fun seedIBC2024() {
        val codeId = "IBC_2024"
        dao.insertBuildingCode(BuildingCodeEntity(codeId, "IBC 2024", "International Building Code 2024", 
            default_asd_set_id = "ASCE_7_22_ASD", default_lrfd_set_id = "ASCE_7_22_LRFD"))
        dao.insertBuildingCodeStandardCrossRef(BuildingCodeStandardCrossRef(codeId, "ASCE_7_22"))
        seedAsce722Combinations()
    }

    private suspend fun seedStateCodes() {
        // CBC 2022 Based on IBC 2021
        dao.insertBuildingCode(BuildingCodeEntity("CBC_2022", "CBC 2022", "California Building Code 2022", 
            base_code_id = "IBC_2021", default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
        
        // FBC 2023 Based on IBC 2021
        dao.insertBuildingCode(BuildingCodeEntity("FBC_2023", "FBC 2023", "Florida Building Code 2023", 
            base_code_id = "IBC_2021", default_asd_set_id = "ASCE_7_16_ASD", default_lrfd_set_id = "ASCE_7_16_LRFD"))
    }

    private suspend fun seedAsce710Combinations() {
        createAsce7ComboSet("ASCE_7_10", "ASCE 7-10")
    }

    private suspend fun seedAsce716Combinations() {
        createAsce7ComboSet("ASCE_7_16", "ASCE 7-16")
    }

    private suspend fun seedAsce722Combinations() {
        createAsce7ComboSet("ASCE_7_22", "ASCE 7-22")
    }

    private suspend fun createAsce7ComboSet(sourceId: String, sourceName: String) {
        val lrfdSetId = "${sourceId}_LRFD"
        val asdSetId = "${sourceId}_ASD"

        dao.insertCombinationSet(LoadCombinationSetEntity(lrfdSetId, sourceId, sourceName, DesignMethodology.LRFD, "$sourceName LRFD Load Combinations"))
        dao.insertCombinationSet(LoadCombinationSetEntity(asdSetId, sourceId, sourceName, DesignMethodology.ASD, "$sourceName ASD Load Combinations"))

        // LRFD Combinations (Full ASCE 7 Set)
        val lrfdCombos = listOf(
            Triple("1.4D", "1.4D", mapOf(LoadCategory.DEAD to 1.4)),
            Triple("1.2D + 1.6L + 0.5(Lr or S or R)", "1.2D_1.6L_0.5Lr", mapOf(LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6, LoadCategory.ROOF_LIVE to 0.5)),
            Triple("1.2D + 1.6(Lr or S or R) + (L or 0.5W)", "1.2D_1.6Lr_L", mapOf(LoadCategory.DEAD to 1.2, LoadCategory.ROOF_LIVE to 1.6, LoadCategory.LIVE to 1.0)),
            Triple("1.2D + 1.0W + L + 0.5(Lr or S or R)", "1.2D_1.0W_L_0.5Lr", mapOf(LoadCategory.DEAD to 1.2, LoadCategory.WIND to 1.0, LoadCategory.LIVE to 1.0, LoadCategory.ROOF_LIVE to 0.5)),
            Triple("1.2D + 1.0E + L + 0.2S", "1.2D_1.0E_L_0.2S", mapOf(LoadCategory.DEAD to 1.2, LoadCategory.SEISMIC to 1.0, LoadCategory.LIVE to 1.0, LoadCategory.SNOW to 0.2)),
            Triple("0.9D + 1.0W", "0.9D_1.0W", mapOf(LoadCategory.DEAD to 0.9, LoadCategory.WIND to 1.0)),
            Triple("0.9D + 1.0E", "0.9D_1.0E", mapOf(LoadCategory.DEAD to 0.9, LoadCategory.SEISMIC to 1.0))
        )

        lrfdCombos.forEach { (name, idSuffix, factors) ->
            val comboId = "${lrfdSetId}_$idSuffix"
            dao.insertCombination(LoadCombinationEntity(comboId, lrfdSetId, name, CombinationType.STRENGTH))
            dao.insertLoadFactors(factors.map { LoadFactorEntity(combination_id = comboId, category = it.key, factor = it.value) })
        }

        // ASD Combinations (Full ASCE 7 Set)
        val asdCombos = listOf(
            Triple("D", "D", mapOf(LoadCategory.DEAD to 1.0)),
            Triple("D + L", "D_L", mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 1.0)),
            Triple("D + (Lr or S or R)", "D_Lr", mapOf(LoadCategory.DEAD to 1.0, LoadCategory.ROOF_LIVE to 1.0)),
            Triple("D + 0.75L + 0.75(Lr or S or R)", "D_075L_075Lr", mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.ROOF_LIVE to 0.75)),
            Triple("D + (0.6W or 0.7E)", "D_06W", mapOf(LoadCategory.DEAD to 1.0, LoadCategory.WIND to 0.6)),
            Triple("D + 0.75L + 0.75(0.6W) + 0.75(Lr or S or R)", "D_075L_075_06W_075Lr", mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.WIND to 0.45, LoadCategory.ROOF_LIVE to 0.75)),
            Triple("D + 0.75L + 0.75(0.7E) + 0.75S", "D_075L_075_07E_075S", mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.SEISMIC to 0.525, LoadCategory.SNOW to 0.75)),
            Triple("0.6D + 0.6W", "06D_06W", mapOf(LoadCategory.DEAD to 0.6, LoadCategory.WIND to 0.6)),
            Triple("0.6D + 0.7E", "06D_07E", mapOf(LoadCategory.DEAD to 0.6, LoadCategory.SEISMIC to 0.7))
        )

        asdCombos.forEach { (name, idSuffix, factors) ->
            val comboId = "${asdSetId}_$idSuffix"
            dao.insertCombination(LoadCombinationEntity(comboId, asdSetId, name, CombinationType.STRENGTH))
            dao.insertLoadFactors(factors.map { LoadFactorEntity(combination_id = comboId, category = it.key, factor = it.value) })
        }
    }
}
