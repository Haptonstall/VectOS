package com.lz.vectos.persistence.repository

import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.structural.*
import com.lz.vectos.persistence.room.dao.BuildingCodeDao
import com.lz.vectos.persistence.room.entity.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class StructuralRepository(
    private val dao: BuildingCodeDao,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    suspend fun getBuildingCode(id: String): BuildingCode? {
        val entity = dao.getBuildingCodeById(id) ?: return null
        
        // 1. Resolve Base Code (Recursive Inheritance)
        val baseCode = entity.base_code_id?.let { getBuildingCode(it) }

        // 2. Resolve Standards associated with this code
        val standardEntities = dao.getStandardsForCode(id)
        val standards = standardEntities.map { mapStandard(it) }

        // 3. Resolve Default Material Standards (Merged with Base)
        val materialEntities = dao.getDefaultMaterialStandardsForCode(id)
        val materialStandards = baseCode?.defaultMaterialStandards?.toMutableMap() ?: mutableMapOf()
        materialEntities.forEach { matEnt ->
            dao.getStandardById(matEnt.standardId)?.let { stdEntity ->
                materialStandards[matEnt.materialType] = mapStandard(stdEntity)
            }
        }

        // 4. Resolve Load Combination Sets (Merged with Base)
        val setEntities = dao.getLoadCombinationSetsForSource(id)
        val comboSets = baseCode?.stateSpecificCombinations?.associateBy { it.id }?.toMutableMap() ?: mutableMapOf()
        
        for (setEntity in setEntities) {
            val comboEntities = dao.getLoadCombinationsForSet(setEntity.id)
            val combos = comboEntities.map { comboEntity ->
                val factors = dao.getFactorsForCombination(comboEntity.id).associate { 
                    it.category to it.factor 
                }
                LoadCombination(
                    id = comboEntity.id,
                    name = comboEntity.name,
                    methodology = setEntity.methodology,
                    equation = "", 
                    factors = factors,
                    codeReference = "",
                    limitState = if (setEntity.methodology == DesignMethodology.LRFD) LimitState.STRENGTH else LimitState.SERVICEABILITY
                )
            }
            comboSets[setEntity.id] = LoadCombinationSet(
                id = setEntity.id,
                sourceId = setEntity.source_id,
                sourceName = setEntity.source_name,
                methodology = setEntity.methodology,
                description = setEntity.description,
                combinations = combos
            )
        }

        // 5. Resolve Serviceability Criteria (Merged with Base)
        val criteriaEntities = dao.getServiceabilityCriteriaForCode(id)
        val criteriaMap = baseCode?.serviceabilityCriteria?.associateBy { it.limitType }?.toMutableMap() ?: mutableMapOf()
        criteriaEntities.forEach { ent ->
            criteriaMap[ent.limitType] = ServiceabilityCriterion(
                limitType = ent.limitType,
                name = ent.name,
                loadCategory = ent.loadCategory,
                spanDenominator = ent.spanDenominator,
                description = ent.description ?: ""
            )
        }

        return BuildingCode(
            id = entity.id,
            shortName = entity.short_name,
            longName = entity.long_name,
            baseCode = baseCode,
            stateSpecificReferences = json.decodeFromString(entity.references_json),
            stateSpecificCombinations = comboSets.values.toList(),
            serviceabilityCriteria = criteriaMap.values.toList(),
            defaultAsdSetId = entity.default_asd_set_id,
            defaultLrfdSetId = entity.default_lrfd_set_id,
            defaultMaterialStandards = materialStandards,
            standards = (baseCode?.standards ?: emptyList()) + standards
        )
    }

    private fun mapStandard(entity: StandardEntity): Standard {
        return Standard(
            id = entity.id,
            shortName = entity.short_name,
            longName = entity.long_name,
            references = json.decodeFromString(entity.references_json)
        )
    }
}
