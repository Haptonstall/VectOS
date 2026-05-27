package com.lz.vectos.persistence.room.mapper

import com.lz.vectos.domain.structural.*
import com.lz.vectos.persistence.room.dao.BuildingCodeWithDetails
import com.lz.vectos.persistence.room.dao.CombinationSetWithDetails
import com.lz.vectos.persistence.room.dao.CombinationWithFactors
import com.lz.vectos.persistence.room.entity.*

/**
 * Extension functions to map Room entities to pure Domain models.
 */

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

private val json = Json { ignoreUnknownKeys = true }

fun StandardEntity.toDomainModel(): Standard {
    return Standard(
        id = this.id,
        shortName = this.short_name,
        longName = this.long_name,
        references = try {
            json.decodeFromString<Map<StructuralReferenceKey, String>>(this.references_json)
        } catch (e: Exception) {
            emptyMap()
        }
    )
}

fun CombinationWithFactors.toDomainModel(methodology: DesignMethodology): LoadCombination {
    val equation = this.factors.joinToString(" + ") { 
        if (it.factor == 1.0) it.category.shortLabel else "${it.factor}${it.category.shortLabel}" 
    }.replace("+ -", "- ")

    return LoadCombination(
        name = this.combination.name,
        methodology = methodology,
        equation = equation,
        factors = this.factors.associate { it.category to it.factor },
        codeReference = "" // Could be added to LoadCombinationEntity if needed
    )
}

fun CombinationSetWithDetails.toDomainModel(): LoadCombinationSet {
    return LoadCombinationSet(
        id = this.set.id,
        sourceId = this.set.source_id,
        sourceName = this.set.source_name,
        methodology = this.set.methodology,
        description = this.set.description,
        combinations = this.combinations.map { it.toDomainModel(this.set.methodology) }
    )
}

fun BuildingCodeWithDetails.toDomainModel(baseCode: BuildingCode? = null): BuildingCode {
    return BuildingCode(
        id = this.buildingCode.id,
        shortName = this.buildingCode.short_name,
        longName = this.buildingCode.long_name,
        baseCode = baseCode,
        stateSpecificReferences = try {
            json.decodeFromString<Map<StructuralReferenceKey, String>>(this.buildingCode.references_json)
        } catch (e: Exception) {
            emptyMap()
        },
        stateSpecificCombinations = this.combinationSets.map { it.toDomainModel() },
        defaultAsdSetId = this.buildingCode.default_asd_set_id,
        defaultLrfdSetId = this.buildingCode.default_lrfd_set_id,
        standards = this.standards.map { it.toDomainModel() }
    )
}
