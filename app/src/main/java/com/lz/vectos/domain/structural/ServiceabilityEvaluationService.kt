package com.lz.vectos.domain.structural

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.ServiceabilityResult
import com.lz.model.structural.StationDemand
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inInches
import com.lz.model.units.inches
import com.lz.vectos.domain.structural.analysis.BeamAnalysisResult
import java.util.UUID
import kotlin.math.abs

/**
 * Service to evaluate serviceability responses (deflections) against code limits
 * using full analyzed diagrams.
 */
object ServiceabilityEvaluationService {

    /**
     * Evaluates serviceability for a member based on building code criteria.
     * Uses the full analysis result which contains all load combination passes.
     */
    fun evaluate(
        member: StructuralMember,
        analysisResult: BeamAnalysisResult,
        buildingCode: BuildingCode
    ): List<ServiceabilityResult> {
        val totalLength = member.spans.sumOf { it.length.inches }
        
        return buildingCode.serviceabilityCriteria.map { criterion ->
            // 1. Resolve which combination or category result to use for this criterion
            // Serviceability checks are typically unfactored (Service Level)
            
            val governingStationResult = if (criterion.loadCategory == null) {
                // Total Deflection (Look for a combination that represents "D + L + S + ...")
                // For now, we'll find the max deflection across all Serviceability combinations
                findMaxDeflectionAcrossCombinations(analysisResult)
            } else {
                // Specific Category Deflection (e.g. LIVE only)
                // We find the result pass for that specific category
                findMaxDeflectionForCategory(analysisResult, criterion.loadCategory)
            }

            val actualDeflection = governingStationResult.deflection
            val allowableDeflectionInches = if (criterion.spanDenominator > 0) totalLength / criterion.spanDenominator else 0.0

            ServiceabilityResult(
                actualDeflection = actualDeflection,
                allowableDeflection = Length(allowableDeflectionInches),
                utilization = if (allowableDeflectionInches > 0) abs(actualDeflection.inInches) / allowableDeflectionInches else 0.0,
                criterion = criterion
            )
        }
    }

    private fun findMaxDeflectionAcrossCombinations(
        result: BeamAnalysisResult
    ): StationDemand {
        // In a real implementation, we would filter result.combinationResults by those tagged with the limitState.
        // For now, we'll look at all combinations and find the absolute maximum deflection.
        return result.combinationResults.values
            .flatMap { it.spanResults }
            .flatMap { it.stationDemands }
            .maxByOrNull { abs(it.deflection.inInches) } 
            ?: StationDemand(0.0.inches, Moment(0.0), Force(0.0), spanId = UUID.randomUUID())
    }

    private fun findMaxDeflectionForCategory(
        result: BeamAnalysisResult,
        category: LoadCategory
    ): StationDemand {
        // Fallback: Look for a combination named after the category (e.g., "Live Load")
        val categoryResult = result.combinationResults[category.label] ?: result.combinationResults[category.shortLabel]
        
        return categoryResult?.spanResults?.flatMap { it.stationDemands }?.maxByOrNull { abs(it.deflection.inInches) }
            ?: result.spanResults.flatMap { it.stationDemands }.maxByOrNull { abs(it.deflection.inInches) }!!
    }
}
