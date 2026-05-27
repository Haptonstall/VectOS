package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.Moment

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
        e: Double,
        i: Double
    ): Map<LimitState, LimitStateEnvelope> {
        val allCombos = buildingCode.defaultLoadCombinations
        
        return LimitState.entries.associateWith { state ->
            val combosForState = allCombos.filter { it.limitState == state }
            val envelope = LoadResolutionService.resolveEnvelope(
                member = member,
                loadCases = loadCases,
                combinations = combosForState,
                modulusOfElasticityPa = e,
                momentOfInertiaM4 = i
            )
            LimitStateEnvelope(
                limitState = state,
                maxMoment = envelope.maxMoment,
                maxShear = envelope.maxShear,
                maxDeflection = envelope.maxDeflection
            )
        }
    }
}
