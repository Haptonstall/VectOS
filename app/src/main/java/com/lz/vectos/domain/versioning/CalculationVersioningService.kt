package com.lz.vectos.domain.versioning

import com.lz.vectos.domain.provenance.CalculationProvenance
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pure Kotlin service to manage calculation versions and detect changes.
 */
object CalculationVersioningService {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Creates a new version and automatically generates diffs against the previous version.
     */
    fun createVersion(
        number: Int,
        note: String,
        current: CalculationProvenance,
        previous: CalculationProvenance?
    ): CalculationVersion {
        val diffs = if (previous != null) computeDiffs(previous, current) else emptyList()
        
        return CalculationVersion(
            versionNumber = number,
            createdAt = LocalDateTime.now().format(formatter),
            summaryNote = note,
            provenance = current,
            diffs = diffs
        )
    }

    private fun computeDiffs(old: CalculationProvenance, new: CalculationProvenance): List<CalculationRevisionDiff> {
        val diffs = mutableListOf<CalculationRevisionDiff>()

        if (old.spanLength != new.spanLength) {
            diffs.add(CalculationRevisionDiff(DiffCategory.GEOMETRY, old.spanLength, new.spanLength, "Span length changed"))
        }
        if (old.sectionDesignation != new.sectionDesignation) {
            diffs.add(CalculationRevisionDiff(DiffCategory.SECTION, old.sectionDesignation, new.sectionDesignation, "Section profile updated"))
        }
        if (old.buildingCode != new.buildingCode) {
            diffs.add(CalculationRevisionDiff(DiffCategory.CODE, old.buildingCode, new.buildingCode, "Governing building code changed"))
        }
        if (old.loadCasesSummary != new.loadCasesSummary) {
            diffs.add(CalculationRevisionDiff(DiffCategory.LOADS, old.loadCasesSummary, new.loadCasesSummary, "Loading configuration modified"))
        }

        // Compare assumptions count or serialized string as a simple heuristic
        if (old.assumptions.size != new.assumptions.size || old.assumptions != new.assumptions) {
            diffs.add(CalculationRevisionDiff(DiffCategory.ASSUMPTIONS, "${old.assumptions.size} active", "${new.assumptions.size} active", "Assumptions or basis updated"))
        }

        return diffs
    }
}
