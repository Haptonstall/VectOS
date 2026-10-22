package com.lz.data.persistence.room.seeder

import com.lz.data.persistence.room.dao.LoadCombinationDao
import com.lz.data.persistence.room.entity.LoadCombinationEntity
import com.lz.data.persistence.room.entity.LoadCombinationSetEntity
import com.lz.data.persistence.room.entity.LoadFactorEntity
import com.lz.model.regulatory.loads.AsceEdition
import com.lz.model.regulatory.loads.AsceLoadCombinationCatalog
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.structural.DesignMethodology

/**
 * Seeds the ASCE 7 load combination sets from [AsceLoadCombinationCatalog].
 *
 * Each set is checked on every launch against the catalog and re-seeded when
 * it differs — so an install that was seeded with an older (incomplete)
 * definition is corrected, instead of the table being left alone forever
 * once it is non-empty. "Differs" means the stored combination ids aren't
 * exactly the catalog's; the ids are derived from each combination's factors
 * ([AsceLoadCombinationCatalog.combinationId]), so a changed factor or an
 * added / removed alternative changes an id and triggers the re-seed. The
 * set ids themselves (e.g. "ASCE_7_16_ASD") never change, so building codes
 * and projects that point at a set keep working.
 */
class StructuralDataSeeder(private val dao: LoadCombinationDao) {

    suspend fun seed() {
        for (edition in AsceLoadCombinationCatalog.editions) {
            seedSet(edition, DesignMethodology.LRFD)
            seedSet(edition, DesignMethodology.ASD)
        }
    }

    private suspend fun seedSet(edition: AsceEdition, methodology: DesignMethodology) {
        val setId = AsceLoadCombinationCatalog.setId(edition, methodology)
        val combos = AsceLoadCombinationCatalog.combinations(edition, methodology)
        val desiredIds = combos.map { AsceLoadCombinationCatalog.combinationId(edition, methodology, it) }.toSet()

        val existingIds = dao.getCombinationIds(setId).toSet()
        if (existingIds == desiredIds) return                 // already up to date
        if (existingIds.isNotEmpty()) dao.deleteCombinationSet(setId)   // stale definition — replace it

        dao.insertCombinationSet(
            LoadCombinationSetEntity(
                setId,
                edition.sourceId,
                edition.sourceName,
                methodology,
                "${edition.sourceName} ${methodology.name} Load Combinations"
            )
        )
        combos.forEach { combo ->
            val comboId = AsceLoadCombinationCatalog.combinationId(edition, methodology, combo)
            dao.insertCombination(LoadCombinationEntity(comboId, setId, combo.name, CombinationType.STRENGTH))
            dao.insertLoadFactors(combo.factors.map {
                LoadFactorEntity(combination_id = comboId, category = it.key, factor = it.value)
            })
        }
    }
}
