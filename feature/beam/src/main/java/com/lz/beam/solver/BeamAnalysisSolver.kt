package com.lz.beam.solver

import com.lz.model.regulatory.AiscEdition
import com.lz.model.regulatory.codes.StandardEdition
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.MaterialType
import com.lz.model.structural.StationDemand
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inches
import com.lz.model.units.inInches
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import com.lz.solver.analysis.AnalysisConfig
import com.lz.solver.analysis.AnalysisPoint
import com.lz.solver.analysis.AnalysisResult
import com.lz.solver.analysis.MemberAnalysisSolver
import com.lz.solver.analysis.MemberAnalysisSolver.createSummaryResult
import com.lz.solver.analysis.SpanAnalysisResult
import com.lz.solver.analysis.SpanExtremePoints
import com.lz.solver.analysis.UtilizationPoint
import com.lz.solver.bracing.BracingLogic
import com.lz.solver.bracing.StabilityFactorCalculator
import com.lz.solver.capacity.CapacityCalculator
import com.lz.solver.capacity.CapacityEngine
import com.lz.solver.material.AiscCbCalculator
import com.lz.solver.material.AiscSteelCapacityCalculator
import com.lz.solver.material.NdsClCalculator
import com.lz.solver.material.NdsWoodCapacityCalculator
import kotlin.math.abs

object BeamAnalysisSolver {

    /**
     * Resolves the AISC edition that governs this config's building code,
     * via BuildingCode.defaultMaterialStandards[STEEL] -> Standard.edition.
     * Returns null (calculator falls back to its own default) if the
     * config has no building code, or the resolved standard's edition
     * isn't a recognized StandardEdition.Aisc360.
     */
    private fun resolveAiscEdition(config: BeamAnalysisConfig): AiscEdition? =
        (config.buildingCode?.defaultMaterialStandards?.get(MaterialType.STEEL)?.edition as? StandardEdition.Aisc360)?.edition

    /**
     * Same as [resolveAiscEdition] but for NDS / wood.
     */
    private fun resolveNdsEdition(config: BeamAnalysisConfig): NdsEdition? =
        (config.buildingCode?.defaultMaterialStandards?.get(MaterialType.WOOD)?.edition as? StandardEdition.Nds)?.edition

    fun solve(config: BeamAnalysisConfig): AnalysisResult {

        // Step 1 — Generic FEM pass: get unfactored forces, reactions,
        // station demands per combination. MemberAnalysisSolver knows
        // nothing about beams, materials, or capacity.
        val genericResult = MemberAnalysisSolver.solve(config.toAnalysisConfig())

        // Step 2 — Select material-appropriate stability factor calculator.
        // NdsClCalculator needs section/material data (CL depends on d, b,
        // Fb, E) — only available when both are configured; falls back to
        // the conservative 1.0 default otherwise, same as the `else` branch.
        val profile = config.sectionProfile
        val material = config.material
        val stabilityCalculator: StabilityFactorCalculator = when {
            material is MaterialGrade.Steel    -> AiscCbCalculator
            material is MaterialGrade.Aluminum -> AiscCbCalculator  // ADM uses same Cb formula
            material is MaterialGrade.Wood && profile != null -> NdsClCalculator(profile, material)
            else                                -> StabilityFactorCalculator { _, _ -> 1.0 }
        }

        val totalLength = config.member.spans.sumOf { it.length.inches }

        // Step 3 — Enrich each span's station demands with Lb, Cb,
        // compression flange, then run capacity checks and build
        // utilization diagrams. Reconstruct SpanAnalysisResult list
        // with utilizationDiagram populated (was emptyList() from solver).
        val enrichedSpanResults = genericResult.spanResults.map { spanResult ->

            val enrichedDemands = BracingLogic.calculateDiscreteLb(
                stationDemands      = spanResult.stationDemands,
                braceState          = config.braceState,
                totalLength         = totalLength,
                member              = config.member,
                stabilityCalculator = stabilityCalculator
            )

            val utilizationPoints: List<UtilizationPoint> =
                buildUtilizationPoints(config, enrichedDemands)

            spanResult.copy(
                stationDemands     = enrichedDemands,
                utilizationDiagram = utilizationPoints
            )
        }

        // Step 4 — Rebuild combination results with enriched spans.
        // Each combination's AnalysisResult also gets its spans enriched.
        val enrichedCombinationResults = genericResult.combinationResults.mapValues { (_, comboResult) ->
            val enrichedComboSpans = comboResult.spanResults.map { spanResult ->
                val enrichedDemands = BracingLogic.calculateDiscreteLb(
                    stationDemands      = spanResult.stationDemands,
                    braceState          = config.braceState,
                    totalLength         = totalLength,
                    member              = config.member,
                    stabilityCalculator = stabilityCalculator
                )
                spanResult.copy(
                    stationDemands     = enrichedDemands,
                    utilizationDiagram = buildUtilizationPoints(config, enrichedDemands)
                )
            }
            createSummaryResult(enrichedComboSpans, comboResult.reactions)
        }

        // Step 5 — Return final result with enriched governing spans
        // and enriched combination map
        return createSummaryResult(enrichedSpanResults, genericResult.reactions).copy(
            combinationResults       = enrichedCombinationResults,
            governingCombinationName = genericResult.governingCombinationName
        )
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Runs capacity checks at every station and returns utilization points.
     * Returns empty list if section profile or material is not configured.
     */
    private fun buildUtilizationPoints(
        config: BeamAnalysisConfig,
        demands: List<StationDemand>
    ): List<UtilizationPoint> {
        val profile  = config.sectionProfile  ?: return emptyList()
        val material = config.material         ?: return emptyList()

        val calculator: CapacityCalculator? = when (material) {
            is MaterialGrade.Steel -> AiscSteelCapacityCalculator(
                profile, material,
                edition = resolveAiscEdition(config) ?: AiscEdition.AISC_360_22
            )
            is MaterialGrade.Wood  -> NdsWoodCapacityCalculator(
                profile, material,
                edition = resolveNdsEdition(config) ?: NdsEdition.NDS_2018
            )
            else                   -> null
        }

        if (calculator == null) return emptyList()

        return CapacityEngine.evaluate(
            demands              = demands,
            section              = profile,
            methodology          = config.designMethodology,
            factors              = calculator.designFactors(config.designMethodology)
        ) { demand -> calculator.evaluate(demand) }
            .map { cap ->
                UtilizationPoint(
                    x        = cap.demand.x,
                    demand   = cap.utilizationRatio * cap.designCapacity,
                    capacity = cap.designCapacity,
                    ratio    = cap.utilizationRatio
                )
            }
    }

    /**
     * Builds span diagram points from a list of station demands.
     * Used when span results need to be reconstructed after bracing enrichment.
     */
    private fun buildEnrichedSpanResult(
        spanId: java.util.UUID,
        demands: List<StationDemand>,
        utilizationPoints: List<UtilizationPoint>,
        combinationName: String?
    ): SpanAnalysisResult {
        val shearPoints    = demands.map { AnalysisPoint(it.x, it.shear.inPoundsForce) }
        val momentPoints   = demands.map { AnalysisPoint(it.x, it.moment.inLbIn) }
        val shearYPoints   = demands.map { AnalysisPoint(it.x, it.shearY.inPoundsForce) }
        val momentYPoints  = demands.map { AnalysisPoint(it.x, it.momentY.inLbIn) }
        val axialPoints    = demands.map { AnalysisPoint(it.x, it.axial.inPoundsForce) }
        val torquePoints   = demands.map { AnalysisPoint(it.x, it.torque.inLbIn) }
        val deflPoints     = demands.map { AnalysisPoint(it.x, it.deflection.inInches) }

        val maxMoment  = momentPoints.maxByOrNull { it.value }      ?: AnalysisPoint(0.0.inches, 0.0)
        val minMoment  = momentPoints.minByOrNull { it.value }      ?: AnalysisPoint(0.0.inches, 0.0)
        val maxShear   = shearPoints.maxByOrNull { it.value }       ?: AnalysisPoint(0.0.inches, 0.0)
        val minShear   = shearPoints.minByOrNull { it.value }       ?: AnalysisPoint(0.0.inches, 0.0)
        val maxAxial   = axialPoints.maxByOrNull { it.value }       ?: AnalysisPoint(0.0.inches, 0.0)
        val minAxial   = axialPoints.minByOrNull { it.value }       ?: AnalysisPoint(0.0.inches, 0.0)
        val maxDefl    = deflPoints.maxByOrNull { abs(it.value) }   ?: AnalysisPoint(0.0.inches, 0.0)

        return SpanAnalysisResult(
            spanId             = spanId,
            maxMoment          = Moment(maxMoment.value),
            maxShear           = Force(maxShear.value),
            maxDeflection      = Length(abs(maxDefl.value)),
            maxAxial           = Force(maxAxial.value),
            minMoment          = Moment(minMoment.value),
            minShear           = Force(minShear.value),
            minAxial           = Force(minAxial.value),
            extremePoints      = SpanExtremePoints(
                maxMomentX     = maxMoment.x,
                minMomentX     = minMoment.x,
                maxShearX      = maxShear.x,
                minShearX      = minShear.x,
                maxAxialX      = maxAxial.x,
                minAxialX      = minAxial.x,
                maxDeflectionX = maxDefl.x
            ),
            shearDiagram       = shearPoints,
            momentDiagram      = momentPoints,
            deflectionDiagram  = deflPoints,
            shearYDiagram      = shearYPoints,
            momentYDiagram     = momentYPoints,
            torqueDiagram      = torquePoints,
            stationDemands     = demands,
            utilizationDiagram = utilizationPoints,
            governingCombinationName = combinationName
        )
    }
}