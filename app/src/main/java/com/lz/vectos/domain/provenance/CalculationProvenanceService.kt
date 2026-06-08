package com.lz.vectos.domain.provenance

import com.lz.model.regulatory.codes.BuildingCode
import com.lz.vectos.domain.structural.LoadCase
import com.lz.model.units.UnitSystem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pure Kotlin service to assemble a formal calculation provenance record.
 */
object CalculationProvenanceService {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Assembles a snapshot of the current calculation context.
     */
    fun assemble(
        projectId: String?,
        calculatorId: String,
        buildingCode: BuildingCode,
        unitSystem: UnitSystem,
        sectionDesignation: String,
        spanLength: String,
        loadCases: List<LoadCase>,
        assumptions: List<CalculationAssumption>,
        acknowledgments: List<String>
    ): CalculationProvenance {
        
        val activeAssumptions = assumptions.filter { it.isEnabled }
        val loadSummary = loadCases.joinToString(", ") { "${it.id}(${it.loads.size})" }

        return CalculationProvenance(
            timestamp = LocalDateTime.now().format(formatter),
            projectId = projectId,
            calculatorId = calculatorId,
            buildingCode = buildingCode.shortName,
            unitSystem = unitSystem.name,
            sectionDesignation = sectionDesignation,
            spanLength = spanLength,
            loadCasesSummary = loadSummary,
            assumptions = activeAssumptions,
            acknowledgments = acknowledgments
        )
    }
}
