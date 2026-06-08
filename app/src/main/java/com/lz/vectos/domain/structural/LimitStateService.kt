package com.lz.vectos.domain.structural

import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment

/**
 * Result of evaluation for a specific limit state.
 */
data class LimitStateEnvelope(
    val limitState: LimitState,
    val maxMoment: GoverningEffect<Moment>,
    val maxShear: GoverningEffect<Force>,
    val maxDeflection: GoverningEffect<Length>
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
        i: Double
    ): Map<LimitState, LimitStateEnvelope> {
        val setId = if (methodology == DesignMethodology.LRFD) buildingCode.defaultLrfdSetId else buildingCode.defaultAsdSetId
        val comboSet = setId?.let { buildingCode.getCombinationSet(it) }
        val allCombos = comboSet?.combinations ?: emptyList()
        
        return LimitState.entries.associateWith { state ->
            val combosForState = allCombos.filter { it.limitState == state }
            val envelope = LoadResolutionService.resolveEnvelope(
                member = member,
                loadCases = loadCases,
                combinations = combosForState,
                modulusOfElasticityPsi = e,
                momentOfInertiaIn4 = i
            )
            LimitStateEnvelope(
                limitState = state,
                maxMoment = GoverningEffect(envelope.maxMoment, 0.0, envelope.governingCombinationName ?: "Governing"),
                maxShear = GoverningEffect(envelope.maxShear, 0.0, envelope.governingCombinationName ?: "Governing"),
                maxDeflection = GoverningEffect(envelope.maxDeflection, 0.0, envelope.governingCombinationName ?: "Governing")
            )
        }
    }
}
