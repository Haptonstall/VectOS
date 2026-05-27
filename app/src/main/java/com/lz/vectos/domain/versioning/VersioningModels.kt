package com.lz.vectos.domain.versioning

import com.lz.vectos.domain.provenance.CalculationProvenance
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Categories for tracking changes between revisions.
 */
enum class DiffCategory {
    GEOMETRY,
    LOADS,
    SECTION,
    CODE,
    ASSUMPTIONS,
    ACKNOWLEDGMENTS
}

/**
 * Represents a single change between two calculation versions.
 */
@Serializable
data class CalculationRevisionDiff(
    val category: DiffCategory,
    val beforeValue: String,
    val afterValue: String,
    val description: String
)

/**
 * A formal revision of a calculation.
 */
@Serializable
data class CalculationVersion(
    val versionId: String = UUID.randomUUID().toString(),
    val versionNumber: Int,
    val createdAt: String,
    val summaryNote: String,
    val provenance: CalculationProvenance,
    val diffs: List<CalculationRevisionDiff> = emptyList()
)
