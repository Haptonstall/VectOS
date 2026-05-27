package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.MaterialType

/**
 * Represents a specific version of an engineering standard (e.g., ASCE 7-22, AISC 360-16).
 */
data class Standard(
    val id: String,
    val shortName: String,
    val longName: String,
    val materialType: MaterialType? = null,
    val references: Map<StructuralReferenceKey, String> = emptyMap()
) {
    companion object {
        /**
         * Reconstructs a Standard from an ID.
         * NOTE: Since data classes with lists/maps cannot be reconstructed from just an ID
         * without a database lookup, this usually implies these items are Enums or
         * Singletons. If they are dynamic DB entities, this logic will require a Repository.
         *
         * For now, this throws an error or returns a stub to satisfy the compiler.
         */
        fun fromId(id: String): Standard {
            // You must implement actual logic here to find the Standard by ID.
            // If these are static/hardcoded standards, you might return a specific one.
            // Example implementation:
            return Standard(
                id = id,
                shortName = id, // Fallback
                longName = "Unknown Standard",
                references = emptyMap()
            )

            // OR if this should strictly not be used, throw an error:
            // throw UnsupportedOperationException("Cannot reconstruct Standard from ID without a Repository lookup.")

        }
    }
}

/**
 * Encapsulates regional building codes and their default configurations.
 * Supports a hierarchical structure where state codes (e.g., CBC) can inherit from base codes (e.g., IBC).
 */
data class BuildingCode(
    val id: String,
    val shortName: String,
    val longName: String,
    val baseCode: BuildingCode? = null,
    val stateSpecificReferences: Map<StructuralReferenceKey, String> = emptyMap(),
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
    fun getReference(key: StructuralReferenceKey): String? {
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
