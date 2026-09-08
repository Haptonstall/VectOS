package com.lz.solver.envelope

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.ServiceabilityCriterion
import com.lz.model.structural.ServiceabilityResult
import com.lz.model.structural.StationDemand
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inInches
import com.lz.model.units.inches
import com.lz.solver.analysis.AnalysisResult
import java.util.UUID
import kotlin.math.abs

/**
 * Service to evaluate serviceability responses (deflections) against code limits
 * using full analyzed diagrams.
 */
object ServiceabilityEvaluationService {

    /**
     * Evaluates serviceability per span. Each span uses its own override
     * criteria list if present in [spanOverrides], falling back to the
     * building code's criteria otherwise — and, critically, uses that
     * span's own length as the denominator base and that span's own
     * governing deflection, not the whole member's. (Deflection limits like
     * L/360 are defined per span; lumping a multi-span beam's total length
     * into one L/360 check was never correct, independent of the override
     * feature this enables.)
     */
    fun evaluate(
        member: StructuralMember,
        analysisResult: AnalysisResult,
        buildingCode: BuildingCode,
        spanOverrides: Map<UUID, List<ServiceabilityCriterion>> = emptyMap()
    ): List<ServiceabilityResult> {
        return member.spans.flatMap { span ->
            val criteria = spanOverrides[span.id] ?: buildingCode.serviceabilityCriteria
            val spanLengthInches = span.length.inches

            criteria.map { criterion ->
                val governingStationResult = if (criterion.loadCategory == null) {
                    findMaxDeflectionAcrossCombinations(analysisResult, span.id)
                } else {
                    findMaxDeflectionForCategory(analysisResult, criterion.loadCategory!!, span.id)
                }

                val actualDeflection = governingStationResult.deflection
                val allowableDeflectionInches = if (criterion.spanDenominator > 0) spanLengthInches / criterion.spanDenominator else 0.0

                ServiceabilityResult(
                    actualDeflection = actualDeflection,
                    allowableDeflection = Length(allowableDeflectionInches),
                    utilization = if (allowableDeflectionInches > 0) abs(actualDeflection.inInches) / allowableDeflectionInches else 0.0,
                    criterion = criterion,
                    spanId = span.id
                )
            }
        }
    }

    private fun findMaxDeflectionAcrossCombinations(
        result: AnalysisResult,
        spanId: UUID
    ): StationDemand {
        // In a real implementation, we would filter result.combinationResults by those tagged with the limitState.
        // For now, we'll look at all combinations and find the absolute maximum deflection for this span.
        return result.combinationResults.values
            .flatMap { it.spanResults }
            .flatMap { it.stationDemands }
            .filter { it.spanId == spanId }
            .maxByOrNull { abs(it.deflection.inInches) }
            ?: result.spanResults
                .flatMap { it.stationDemands }
                .filter { it.spanId == spanId }
                .maxByOrNull { abs(it.deflection.inInches) }
            ?: StationDemand(
                spanId = spanId,
                x = 0.0.inches,
                moment = Moment(0.0),
                shear = Force(0.0)
            )
    }

    private fun findMaxDeflectionForCategory(
        result: AnalysisResult,
        category: LoadCategory,
        spanId: UUID
    ): StationDemand {
        // Fallback: Look for a combination named after the category (e.g., "Live Load")
        val categoryResult = result.combinationResults[category.label] ?: result.combinationResults[category.shortLabel]

        return categoryResult?.spanResults
            ?.flatMap { it.stationDemands }
            ?.filter { it.spanId == spanId }
            ?.maxByOrNull { abs(it.deflection.inInches) }
            ?: result.spanResults
                .flatMap { it.stationDemands }
                .filter { it.spanId == spanId }
                .maxByOrNull { abs(it.deflection.inInches) }
            ?: StationDemand(
                spanId = spanId,
                x = 0.0.inches,
                moment = Moment(0.0),
                shear = Force(0.0)
            )
    }
}