package com.lz.solver.analysis

import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.GoverningEffect
import com.lz.model.structural.LimitState
import com.lz.model.structural.LoadCase
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.in4
import com.lz.model.units.ksiModulus
import com.lz.model.regulatory.LoadCombination

/**
 * Result of evaluation for a specific limit state.
 */
data class LimitStateEnvelope(
    val limitState: LimitState,
    val maxMoment: GoverningEffect<Moment>,
    val maxMomentY: GoverningEffect<Moment>,
    val maxShear: GoverningEffect<Force>,
    val maxShearY: GoverningEffect<Force>,
    val maxAxial: GoverningEffect<Force>,
    val maxTorsion: GoverningEffect<Moment>,
    val maxDeflection: GoverningEffect<Length>,
)

/**
 * Pure Kotlin service to partition analysis results into limit states.
 */
object LimitStateService {

    /**
     * Filters and resolves governing results specifically for Strength and Serviceability.
     */
    fun evaluate(
        member: StructuralMember,
        loadCases: List<LoadCase>,
        buildingCode: BuildingCode,
        methodology: DesignMethodology,
        e: Double,
        ix: Double,
        iy: Double
    ): Map<LimitState, LimitStateEnvelope> {
        val setId = if (methodology == DesignMethodology.LRFD) buildingCode.defaultLrfdSetId else buildingCode.defaultAsdSetId
        val comboSet = setId?.let { buildingCode.getCombinationSet(it) }
        val allCombos = comboSet?.combinations ?: emptyList()
        
        return LimitState.entries.mapNotNull { state ->
            val combosForState = allCombos.filter { mapToLimitState(it) == state }
            if (combosForState.isEmpty()) return@mapNotNull null

            val envelope = LoadResolutionService.resolveEnvelope(
                member = member,
                loadCases = loadCases,
                combinations = combosForState,
                modulusOfElasticity = e.ksiModulus,
                momentOfInertiaX = ix.in4,
                momentOfInertiaY = iy.in4
            )
            
            state to LimitStateEnvelope(
                limitState = state,
                maxMoment = resolveGoverningEffect(envelope, { it.maxMoment }) { it.momentDiagram },
                maxMomentY = resolveGoverningEffect(envelope, { it.maxMomentY }) { it.momentYDiagram },
                maxShear = resolveGoverningEffect(envelope, { it.maxShear }) { it.shearDiagram },
                maxShearY = resolveGoverningEffect(envelope, { it.maxShearY }) { it.shearYDiagram },
                maxAxial = resolveGoverningAxial(envelope),
                maxTorsion = resolveGoverningEffect(envelope, { it.maxTorsion }) { it.torqueDiagram },
                maxDeflection = resolveGoverningEffect(envelope, { it.maxDeflection }) { it.deflectionDiagram }
            )
        }.toMap()
    }

    private fun mapToLimitState(combo: LoadCombination): LimitState {
        // Heuristic: LRFD -> STRENGTH, ASD -> SERVICEABILITY
        return if (combo.methodology == DesignMethodology.LRFD) LimitState.STRENGTH else LimitState.SERVICEABILITY
    }

    private fun <T : Comparable<T>> resolveGoverningEffect(
        envelope: AnalysisResult,
        maxValue: (AnalysisResult) -> T,
        diagram: (SpanAnalysisResult) -> List<AnalysisPoint>
    ): GoverningEffect<T> {
        val targetValue = maxValue(envelope)
        val governingName = envelope.governingCombinationName ?: "Governing"

        // Search spans for the absolute location of this value
        var globalOffset = 0.0
        for (span in envelope.spanResults) {
            val point = diagram(span).find { 
                it.value == ((targetValue as? Moment)?.lbIn ?: (targetValue as? Force)?.pounds ?: (targetValue as? Length)?.inches)
            }
            if (point != null) {
                return GoverningEffect(targetValue, globalOffset + point.x.inches, governingName)
            }
            // Length of span is not directly in SpanAnalysisResult, but we can infer it from diagrams or SpanExtremePoints
            val spanLength = diagram(span).maxOfOrNull { it.x.inches } ?: 0.0
            globalOffset += spanLength
        }

        return GoverningEffect(targetValue, 0.0, governingName)
    }

    private fun resolveGoverningAxial(envelope: AnalysisResult): GoverningEffect<Force> {
        val targetValue = envelope.maxAxial
        val governingName = envelope.governingCombinationName ?: "Governing"
        
        var globalOffset = 0.0
        for (span in envelope.spanResults) {
            val demand = span.stationDemands.find { it.axial == targetValue }
            if (demand != null) {
                return GoverningEffect(targetValue, globalOffset + demand.x.inches, governingName)
            }
            val spanLength = span.stationDemands.maxOfOrNull { it.x.inches } ?: 0.0
            globalOffset += spanLength
        }
        
        return GoverningEffect(targetValue, 0.0, governingName)
    }
}