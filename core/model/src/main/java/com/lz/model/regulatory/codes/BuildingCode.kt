package com.lz.model.regulatory.codes

import com.lz.model.structural.MaterialType
import com.lz.model.regulatory.LoadCombinationSet

/**
 * Encapsulates regional building codes and their default configurations.
 * Supports a hierarchical structure where state codes (e.g., CBC) can inherit from base codes (e.g., IBC).
 */
data class BuildingCode(
    val id: String,
    val shortName: String,
    val longName: String,
    val baseCode: BuildingCode? = null,
    val stateSpecificReferences: Map<CodeReferenceKey, String> = emptyMap(),
    val stateSpecificCombinations: List<LoadCombinationSet> = emptyList(),
    val serviceabilityCriteria: List<ServiceabilityCriterion> = emptyList(),
    val defaultAsdSetId: String? = null,
    val defaultLrfdSetId: String? = null,
    val defaultMaterialStandards: Map<MaterialType, Standard> = emptyMap(),
    val standards: List<Standard> = emptyList()
) {
    /**
     * Resolves a code reference. Checks the state code first, falls back to the base code.
     */
    fun getReference(key: CodeReferenceKey): String? {
        return stateSpecificReferences[key] ?: baseCode?.getReference(key)
    }

    /**
     * Returns the base code name for the UI, or null if it's already a base code.
     */
    fun getBaseCodeDisplayName(): String? {
        return baseCode?.shortName
    }

    /**
     * Finds a specific combination set, falling back to the base code if not defined locally.
     */
    fun getCombinationSet(setId: String): LoadCombinationSet? {
        return stateSpecificCombinations.find { it.id == setId } ?: baseCode?.getCombinationSet(setId)
    }

    companion object {
        /**
         * Reconstructs a BuildingCode from an ID.
         * NOTE: Your converter saves 'shortName', but calls 'fromId'.
         * This implies 'id' passed here might actually be the shortName.
         */
        fun fromId(id: String): BuildingCode {
            // Placeholder implementation to satisfy the compiler
            return BuildingCode(
                id = id,
                shortName = id,
                longName = "Unknown Code",
                defaultMaterialStandards = emptyMap(),
                standards = emptyList()
            )
        }
    }
}