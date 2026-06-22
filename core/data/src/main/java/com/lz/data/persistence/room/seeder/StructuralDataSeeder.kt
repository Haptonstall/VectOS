package com.lz.data.persistence.room.seeder

import com.lz.data.persistence.room.dao.LoadCombinationDao
import com.lz.data.persistence.room.entity.LoadCombinationEntity
import com.lz.data.persistence.room.entity.LoadCombinationSetEntity
import com.lz.data.persistence.room.entity.LoadFactorEntity
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.structural.DesignMethodology

class StructuralDataSeeder(private val dao: LoadCombinationDao) {
    suspend fun seed() {
        if (dao.getCount() > 0) return
        createAsce7ComboSet("ASCE_7_10", "ASCE 7-10")
        createAsce7ComboSet("ASCE_7_16", "ASCE 7-16")
        createAsce7ComboSet("ASCE_7_22", "ASCE 7-22")
    }

    private suspend fun createAsce7ComboSet(sourceId: String, sourceName: String) {
        val lrfdSetId = "${sourceId}_LRFD"
        val asdSetId  = "${sourceId}_ASD"

        dao.insertCombinationSet(
            LoadCombinationSetEntity(
                lrfdSetId,
                sourceId,
                sourceName,
                DesignMethodology.LRFD,
                "$sourceName LRFD Load Combinations"
            )
        )
        dao.insertCombinationSet(
            LoadCombinationSetEntity(
                asdSetId,
                sourceId,
                sourceName,
                DesignMethodology.ASD,
                "$sourceName ASD Load Combinations"
            )
        )

        val lrfdCombos = listOf(
            Triple("1.4D",                                          "1.4D",                  mapOf(
                LoadCategory.DEAD to 1.4)),
            Triple("1.2D + 1.6L + 0.5(Lr or S or R)",             "1.2D_1.6L_0.5Lr",       mapOf(
                LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6, LoadCategory.ROOF_LIVE to 0.5)),
            Triple("1.2D + 1.6(Lr or S or R) + (L or 0.5W)",      "1.2D_1.6Lr_L",          mapOf(
                LoadCategory.DEAD to 1.2, LoadCategory.ROOF_LIVE to 1.6, LoadCategory.LIVE to 1.0)),
            Triple("1.2D + 1.0W + L + 0.5(Lr or S or R)",         "1.2D_1.0W_L_0.5Lr",     mapOf(
                LoadCategory.DEAD to 1.2, LoadCategory.WIND to 1.0, LoadCategory.LIVE to 1.0, LoadCategory.ROOF_LIVE to 0.5)),
            Triple("1.2D + 1.0E + L + 0.2S",                      "1.2D_1.0E_L_0.2S",      mapOf(
                LoadCategory.DEAD to 1.2, LoadCategory.SEISMIC to 1.0, LoadCategory.LIVE to 1.0, LoadCategory.SNOW to 0.2)),
            Triple("0.9D + 1.0W",                                  "0.9D_1.0W",             mapOf(
                LoadCategory.DEAD to 0.9, LoadCategory.WIND to 1.0)),
            Triple("0.9D + 1.0E",                                  "0.9D_1.0E",             mapOf(
                LoadCategory.DEAD to 0.9, LoadCategory.SEISMIC to 1.0))
        )

        val asdCombos = listOf(
            Triple("D",                                             "D",                     mapOf(
                LoadCategory.DEAD to 1.0)),
            Triple("D + L",                                        "D_L",                   mapOf(
                LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 1.0)),
            Triple("D + (Lr or S or R)",                           "D_Lr",                  mapOf(
                LoadCategory.DEAD to 1.0, LoadCategory.ROOF_LIVE to 1.0)),
            Triple("D + 0.75L + 0.75(Lr or S or R)",              "D_075L_075Lr",          mapOf(
                LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.ROOF_LIVE to 0.75)),
            Triple("D + (0.6W or 0.7E)",                           "D_06W",                 mapOf(
                LoadCategory.DEAD to 1.0, LoadCategory.WIND to 0.6)),
            Triple("D + 0.75L + 0.75(0.6W) + 0.75(Lr or S or R)","D_075L_075_06W_075Lr",  mapOf(
                LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.WIND to 0.45, LoadCategory.ROOF_LIVE to 0.75)),
            Triple("D + 0.75L + 0.75(0.7E) + 0.75S",             "D_075L_075_07E_075S",   mapOf(
                LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.SEISMIC to 0.525, LoadCategory.SNOW to 0.75)),
            Triple("0.6D + 0.6W",                                  "06D_06W",               mapOf(
                LoadCategory.DEAD to 0.6, LoadCategory.WIND to 0.6)),
            Triple("0.6D + 0.7E",                                  "06D_07E",               mapOf(
                LoadCategory.DEAD to 0.6, LoadCategory.SEISMIC to 0.7))
        )

        lrfdCombos.forEach { (name, idSuffix, factors) ->
            val comboId = "${lrfdSetId}_$idSuffix"
            dao.insertCombination(
                LoadCombinationEntity(
                    comboId,
                    lrfdSetId,
                    name,
                    CombinationType.STRENGTH
                )
            )
            dao.insertLoadFactors(factors.map {
                LoadFactorEntity(
                    combination_id = comboId,
                    category = it.key,
                    factor = it.value
                )
            })
        }

        asdCombos.forEach { (name, idSuffix, factors) ->
            val comboId = "${asdSetId}_$idSuffix"
            dao.insertCombination(
                LoadCombinationEntity(
                    comboId,
                    asdSetId,
                    name,
                    CombinationType.STRENGTH
                )
            )
            dao.insertLoadFactors(factors.map {
                LoadFactorEntity(
                    combination_id = comboId,
                    category = it.key,
                    factor = it.value
                )
            })
        }
    }
}