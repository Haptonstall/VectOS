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
)

