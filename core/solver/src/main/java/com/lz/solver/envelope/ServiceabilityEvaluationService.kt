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
     * span's own length as the denominator base (doubled for a cantilever,
     * per IBC 1604.3) and that span's own governing deflection, not the
     * whole member's. (Deflection limits like L/360 are defined per span;
     * lumping a multi-span beam's total length into one L/360 check was
     * never correct, independent of the override feature this enables.)
     */
    fun evaluate(
        member: StructuralMember,
        analysisResult: AnalysisResult,
        buildingCode: BuildingCode,
        spanOverrides: Map<UUID, List<ServiceabilityCriterion>> = emptyMap()
    ): List<ServiceabilityResult> {
        val nodesById = member.nodes.associateBy { it.id }

        return member.spans.flatMap { span ->
            val criteria = spanOverrides[span.id] ?: buildingCode.serviceabilityCriteria

            // Per IBC 1604.3 (and matching convention elsewhere), a cantilever's
            // deflection-limit length is taken as TWICE its actual physical
            // length — same denominators (L/360, L/240, etc.) as any other span,
            // just against double the L. Net effect: a cantilever's allowable
            // deflection in absolute inches is double what using its raw physical
            // length would give. A span counts as a cantilever here if either end
            // node is genuinely unsupported (NodeBoundaryCondition.free() — no
            // restraint on any DOF), regardless of which end of the span that
            // node happens to be, and independent of whether default or
            // per-span-override criteria are in use — this is a property of the
            // span's own structural condition, not of which criteria set applies.
            val isCantilever = listOf(span.startNodeId, span.endNodeId).any { nodeId ->
                nodesById[nodeId]?.boundaryCondition?.isConstrained() == false
            }
            val spanLengthInches = span.length.inches * if (isCantilever) 2.0 else 1.0

            criteria.map { criterion ->
                val governingStationResult = if (criterion.loadCategory == null) {
                    findMaxDeflectionForGoverningCombination(analysisResult, span.id)
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

    private fun findMaxDeflectionForGoverningCombination(
        result: AnalysisResult,
        spanId: UUID
    ): StationDemand {
        // "Total Load" deflection now means "under the same governing
        // combination already shown everywhere else on this screen"
        // (result.spanResults comes from combinationResults[governingCombination-
        // Name], one single combo held consistent at every station — see the
        // MemberAnalysisSolver fix), not the worst deflection independently
        // found under ANY factored strength combo scanned across
        // combinationResults. The old approach had no consistent engineering
        // meaning: it could report the worst deflection from a dead-only combo
        // at one span/station and a dead+live combo at another, with nothing
        // tying it to the same governing case the Analysis tab and the GOV%
        // badge were already built around — hence the header/Design-tab
        // mismatch even with only Dead + Live active.
        return result.spanResults
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
        // Was: looked for a *combination* literally named after the category
        // (e.g. "Live Load" or "LL") — but combos are named things like
        // "1.2D + 1.6L", so that lookup essentially never matched anything,
        // and silently fell through to the same governing-envelope fallback
        // used when there's no category filter at all. That's why a "Live
        // Load" deflection check and the "Total Load" check reported the
        // same number. What "Live Load deflection" actually means is the
        // unfactored, live-only demand — exactly what categoryResults holds.
        val categoryResult = result.categoryResults[category.name]

        // No entry for this category means no loads of that category were ever
        // applied to the member — the correct live/snow/wind-alone deflection
        // in that case really is zero, not "whatever the governing envelope
        // happens to show" (that envelope is dominated by whichever categories
        // ARE present, and has nothing to do with this one).
        return categoryResult?.spanResults
            ?.flatMap { it.stationDemands }
            ?.filter { it.spanId == spanId }
            ?.maxByOrNull { abs(it.deflection.inInches) }
            ?: StationDemand(
                spanId = spanId,
                x = 0.0.inches,
                moment = Moment(0.0),
                shear = Force(0.0)
            )
    }
}