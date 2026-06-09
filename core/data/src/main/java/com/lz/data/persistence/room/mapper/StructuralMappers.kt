package com.lz.data.persistence.room.mapper

import com.lz.data.persistence.room.dao.BuildingCodeWithDetails
import com.lz.data.persistence.room.dao.CombinationSetWithDetails
import com.lz.data.persistence.room.dao.CombinationWithFactors
import com.lz.data.persistence.room.entity.StandardEntity
import com.lz.model.regulatory.CombinationSource
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.LoadCombinationSet
import com.lz.model.regulatory.StandardReferenceKey
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.CodeReferenceKey
import com.lz.model.regulatory.codes.Standard
import com.lz.model.structural.DesignMethodology
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun StandardEntity.toDomainModel(): Standard {
    return Standard(
        id = id,
        shortName = short_name,
        longName = long_name,
        references = try {
            json.decodeFromString<Map<StandardReferenceKey, String>>(this.references_json)
        } catch (e: Exception) {
            emptyMap()
        }
    )
}

fun CombinationWithFactors.toDomainModel(methodology: DesignMethodology): LoadCombination {
    val equationText = factors.joinToString(" + ") {
        if (it.factor == 1.0) it.category.shortLabel else "${it.factor}${it.category.shortLabel}"
    }.replace("+ -", "- ")

    return LoadCombination(
        id = combination.id,
        name = combination.name,
        methodology = methodology,
        equationText = equationText,
        factors = factors.associate { it.category to it.factor },
        codeReference = ""
    )
}

fun CombinationSetWithDetails.toDomainModel(): LoadCombinationSet {
    return LoadCombinationSet(
        id = set.id,
        source = CombinationSource(
            id = set.source_id,
            name = set.source_name,
            description = set.description
        ),
        methodology = set.methodology,
        description = set.description,
        combinations = combinations.map { it.toDomainModel(set.methodology) }
    )
}

fun BuildingCodeWithDetails.toDomainModel(baseCode: BuildingCode? = null): BuildingCode {
    return BuildingCode(
        id = buildingCode.id,
        shortName = buildingCode.short_name,
        longName = buildingCode.long_name,
        baseCode = baseCode,
        stateSpecificReferences = decodeReferences(buildingCode.references_json),
        stateSpecificCombinations = combinationSets.map { it.toDomainModel() },
        defaultAsdSetId = buildingCode.default_asd_set_id,
        defaultLrfdSetId = buildingCode.default_lrfd_set_id,
        standards = standards.map { it.toDomainModel() }
    )
}

private inline fun <reified T> decodeReferences(value: String): Map<T, String> {
    return try {
        json.decodeFromString(value)
    } catch (e: Exception) {
        emptyMap()
    }
}
