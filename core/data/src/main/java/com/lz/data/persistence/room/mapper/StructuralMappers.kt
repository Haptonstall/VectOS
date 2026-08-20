package com.lz.data.persistence.room.mapper

import com.lz.data.persistence.room.dao.BuildingCodeWithDetails
import com.lz.data.persistence.room.dao.CombinationSetWithDetails
import com.lz.data.persistence.room.dao.CombinationWithFactors
import com.lz.data.persistence.room.entity.StandardEntity
import com.lz.model.regulatory.CombinationSource
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.LoadCombinationSet
import com.lz.model.regulatory.StandardReferenceKey
import com.lz.model.regulatory.aci318.Aci318Edition
import com.lz.model.regulatory.asce7.Asce7Edition
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.CodeReferenceKey
import com.lz.model.regulatory.codes.Standard
import com.lz.model.regulatory.codes.StandardEdition
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.MaterialType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Resolves a [StandardEntity]'s `edition_family`/`edition_key` columns into
 * a real [StandardEdition]. Single place this mapping happens — keeps the
 * seeder (which writes these two columns) and this reader in sync, since
 * both are just string keys with no compile-time link between them.
 */
private fun StandardEntity.resolveEdition(): StandardEdition {
    val key = edition_key ?: return StandardEdition.Unknown
    return when (edition_family) {
        "ASCE7"   -> Asce7Edition.entries.find { it.name == key }?.let { StandardEdition.Asce7(it) }
        "AISC360" -> com.lz.model.regulatory.AiscEdition.entries.find { it.name == key }?.let { StandardEdition.Aisc360(it) }
        "NDS"     -> NdsEdition.entries.find { it.name == key }?.let { StandardEdition.Nds(it) }
        "ACI318"  -> Aci318Edition.entries.find { it.name == key }?.let { StandardEdition.Aci318(it) }
        else      -> null
    } ?: StandardEdition.Unknown
}

fun StandardEntity.toDomainModel(): Standard {
    return Standard(
        id = id,
        shortName = short_name,
        longName = long_name,
        edition = resolveEdition(),
        materialType = material_type?.let {
            try { MaterialType.valueOf(it) } catch (e: IllegalArgumentException) { null }
        },
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
        type = combination.type,
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
    val resolvedStandards = standards.map { it.toDomainModel() }
    val standardsById = resolvedStandards.associateBy { it.id }

    return BuildingCode(
        id = buildingCode.id,
        shortName = buildingCode.short_name,
        longName = buildingCode.long_name,
        baseCode = baseCode,
        stateSpecificReferences = decodeReferences(buildingCode.references_json),
        stateSpecificCombinations = combinationSets.map { it.toDomainModel() },
        defaultAsdSetId = buildingCode.default_asd_set_id,
        defaultLrfdSetId = buildingCode.default_lrfd_set_id,
        defaultMaterialStandards = defaultMaterialStandards.mapNotNull { entry ->
            standardsById[entry.standardId]?.let { entry.materialType to it }
        }.toMap(),
        standards = resolvedStandards
    )
}

private inline fun <reified T> decodeReferences(value: String): Map<T, String> {
    return try {
        json.decodeFromString(value)
    } catch (e: Exception) {
        emptyMap()
    }
}
