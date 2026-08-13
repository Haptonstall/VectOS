package com.lz.solver.analysis

import com.lz.model.regulatory.LoadCombination
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
import kotlin.math.abs

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
     * Filters and resolves governing results specifically for Strength
     * and Serviceability.
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

        val setId =
            if (methodology == DesignMethodology.LRFD) {
                buildingCode.defaultLrfdSetId
            } else {
                buildingCode.defaultAsdSetId
            }

        val comboSet = setId?.let {
            buildingCode.getCombinationSet(it)
        }

        val allCombos = comboSet?.combinations ?: emptyList()

        return LimitState.entries.mapNotNull { state ->

            val combosForState = allCombos.filter {
                mapToLimitState(it) == state
            }

            if (combosForState.isEmpty()) {
                return@mapNotNull null
            }

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

                maxMoment = resolveMoment(
                    envelope = envelope,
                    selector = { it.maxMoment },
                    diagramSelector = { it.momentDiagram }
                ),

                maxMomentY = resolveMoment(
                    envelope = envelope,
                    selector = { it.maxMomentY },
                    diagramSelector = { it.momentYDiagram }
                ),

                maxShear = resolveForce(
                    envelope = envelope,
                    selector = { it.maxShear },
                    diagramSelector = { it.shearDiagram }
                ),

                maxShearY = resolveForce(
                    envelope = envelope,
                    selector = { it.maxShearY },
                    diagramSelector = { it.shearYDiagram }
                ),

                maxAxial = resolveAxial(envelope),

                maxTorsion = resolveMoment(
                    envelope = envelope,
                    selector = { it.maxTorsion },
                    diagramSelector = { it.torqueDiagram }
                ),

                maxDeflection = resolveDeflection(envelope)
            )
        }.toMap()
    }

    private fun mapToLimitState(
        combo: LoadCombination
    ): LimitState {
        return if (combo.methodology == DesignMethodology.LRFD) {
            LimitState.STRENGTH
        } else {
            LimitState.SERVICEABILITY
        }
    }

    /**
     * Resolves the governing moment by absolute magnitude from the
     * actual combination result and locates that value on its diagram.
     */
    private fun resolveMoment(
        envelope: AnalysisResult,
        selector: (AnalysisResult) -> Moment,
        diagramSelector: (SpanAnalysisResult) -> List<AnalysisPoint>
    ): GoverningEffect<Moment> {

        var governingValue = 0.0
        var governingMagnitude = Double.NEGATIVE_INFINITY
        var governingLocation = 0.0
        var governingCombination = "Governing"

        for ((combinationName, result) in envelope.combinationResults) {

            val value = selector(result).lbIn
            val magnitude = abs(value)

            if (magnitude > governingMagnitude) {

                val location = findMaximumLocation(
                    result = result,
                    diagramSelector = diagramSelector
                )

                governingValue = value
                governingMagnitude = magnitude
                governingLocation = location
                governingCombination = combinationName
            }
        }

        if (envelope.combinationResults.isEmpty()) {
            val value = selector(envelope)

            return GoverningEffect(
                value,
                findMaximumLocation(
                    result = envelope,
                    diagramSelector = diagramSelector
                ),
                envelope.governingCombinationName ?: "Governing"
            )
        }

        return GoverningEffect(
            Moment(governingValue),
            governingLocation,
            governingCombination
        )
    }

    /**
     * Resolves the governing force by absolute magnitude from the
     * actual combination result and locates that value on its diagram.
     */
    private fun resolveForce(
        envelope: AnalysisResult,
        selector: (AnalysisResult) -> Force,
        diagramSelector: (SpanAnalysisResult) -> List<AnalysisPoint>
    ): GoverningEffect<Force> {

        var governingValue = 0.0
        var governingMagnitude = Double.NEGATIVE_INFINITY
        var governingLocation = 0.0
        var governingCombination = "Governing"

        for ((combinationName, result) in envelope.combinationResults) {

            val value = selector(result).pounds
            val magnitude = abs(value)

            if (magnitude > governingMagnitude) {

                val location = findMaximumLocation(
                    result = result,
                    diagramSelector = diagramSelector
                )

                governingValue = value
                governingMagnitude = magnitude
                governingLocation = location
                governingCombination = combinationName
            }
        }

        if (envelope.combinationResults.isEmpty()) {
            val value = selector(envelope)

            return GoverningEffect(
                value,
                findMaximumLocation(
                    result = envelope,
                    diagramSelector = diagramSelector
                ),
                envelope.governingCombinationName ?: "Governing"
            )
        }

        return GoverningEffect(
            Force(governingValue),
            governingLocation,
            governingCombination
        )
    }

    /**
     * Resolves the governing axial force using the actual station
     * where the combination result reaches its maximum axial demand.
     */
    private fun resolveAxial(
        envelope: AnalysisResult
    ): GoverningEffect<Force> {

        if (envelope.combinationResults.isEmpty()) {
            val target = envelope.maxAxial.pounds

            val point = envelope.spanResults
                .flatMap { it.stationDemands }
                .maxByOrNull { abs(it.axial.pounds) }

            return GoverningEffect(
                envelope.maxAxial,
                point?.x?.inches ?: 0.0,
                envelope.governingCombinationName ?: "Governing"
            )
        }

        var governingValue = 0.0
        var governingMagnitude = Double.NEGATIVE_INFINITY
        var governingLocation = 0.0
        var governingCombination = "Governing"

        for ((combinationName, result) in envelope.combinationResults) {

            val value = result.maxAxial.pounds
            val magnitude = abs(value)

            if (magnitude > governingMagnitude) {

                val point = result.spanResults
                    .flatMap { it.stationDemands }
                    .maxByOrNull { abs(it.axial.pounds) }

                governingValue = value
                governingMagnitude = magnitude
                governingLocation = point?.x?.inches ?: 0.0
                governingCombination = combinationName
            }
        }

        return GoverningEffect(
            Force(governingValue),
            governingLocation,
            governingCombination
        )
    }

    /**
     * Resolves maximum absolute deflection and its actual location.
     */
    private fun resolveDeflection(
        envelope: AnalysisResult
    ): GoverningEffect<Length> {

        if (envelope.combinationResults.isEmpty()) {
            return resolveDeflectionFromResult(
                envelope,
                envelope.governingCombinationName ?: "Governing"
            )
        }

        var governingValue = Double.NEGATIVE_INFINITY
        var governingLocation = 0.0
        var governingCombination = "Governing"

        for ((combinationName, result) in envelope.combinationResults) {

            val point = result.spanResults
                .flatMap { it.deflectionDiagram }
                .maxByOrNull { abs(it.value) }

            if (point != null && abs(point.value) > governingValue) {
                governingValue = abs(point.value)
                governingLocation = point.x.inches
                governingCombination = combinationName
            }
        }

        return GoverningEffect(
            Length(governingValue),
            governingLocation,
            governingCombination
        )
    }

    private fun resolveDeflectionFromResult(
        result: AnalysisResult,
        combinationName: String
    ): GoverningEffect<Length> {

        val point = result.spanResults
            .flatMap { it.deflectionDiagram }
            .maxByOrNull { abs(it.value) }

        return GoverningEffect(
            Length(point?.let { abs(it.value) } ?: 0.0),
            point?.x?.inches ?: 0.0,
            combinationName
        )
    }

    /**
     * Finds the location of the maximum value in a diagram.
     *
     * AnalysisPoint.x is already a GLOBAL member coordinate in
     * MemberAnalysisSolver.analyzeSpan().
     */
    /**
     * Finds the location of the maximum absolute value in a diagram.
     *
     * AnalysisPoint.x is already a GLOBAL member coordinate in
     * MemberAnalysisSolver.analyzeSpan().
     *
     * This must use absolute magnitude because AnalysisResult's
     * maxMoment/maxShear/etc. are themselves resolved by absolute value.
     */
    private fun findMaximumLocation(
        result: AnalysisResult,
        diagramSelector: (SpanAnalysisResult) -> List<AnalysisPoint>
    ): Double {

        return result.spanResults
            .flatMap { diagramSelector(it) }
            .maxByOrNull { abs(it.value) }
            ?.x
            ?.inches
            ?: 0.0
    }
}