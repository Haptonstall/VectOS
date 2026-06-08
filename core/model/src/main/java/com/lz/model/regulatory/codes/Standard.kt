package com.lz.model.regulatory.codes

import com.lz.model.regulatory.StandardReferenceKey
import com.lz.model.structural.MaterialType

/**
 * Represents a specific version of an engineering standard (e.g., ASCE 7-22, AISC 360-16).
 */
data class Standard(
    val id: String,
    val shortName: String,
    val longName: String,
    val edition: StandardEdition = StandardEdition.Unknown,
    val materialType: MaterialType? = null,
    val references: Map<StandardReferenceKey, String> = emptyMap()
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

