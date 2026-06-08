package com.lz.solver.envelope

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.structural.LimitState
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment

object LoadCombinationEngine {

    /**
     * Complete structural envelope results for an analyzed member,
     * isolating distinct limit states and force components.
     */
    data class MemberEnvelopeResult(
        val strengthEnvelope: List<StationEnvelopeResult>,
        val serviceabilityEnvelope: List<StationEnvelopeResult>,
        val governingMaxMoment: StationEnvelopeResult,
        val governingMaxShear: StationEnvelopeResult,
        val governingMaxDeflection: StationEnvelopeResult
    )

    data class StationEnvelopeResult(
        val x: Length,
        val combinedDemand: StationDemand,
        val governingCombination: LoadCombination
    )

    /**
     * Resolves comprehensive multi-criteria envelopes across all stations.
     */
    fun resolveMemberEnvelopes(
        analysisResultsByCategory: Map<LoadCategory, List<StationDemand>>,
        combinations: List<LoadCombination>
    ): MemberEnvelopeResult {
        if (analysisResultsByCategory.isEmpty() || combinations.isEmpty()) {
            throw IllegalArgumentException("Analysis results and load combinations must not be empty.")
        }

        val firstCategory = analysisResultsByCategory.keys.first()
        val stationsCount = analysisResultsByCategory[firstCategory]?.size ?: 0

        val strengthResults = mutableListOf<StationEnvelopeResult>()
        val serviceResults = mutableListOf<StationEnvelopeResult>()

        // 1. Process every station along the member
        for (index in 0 until stationsCount) {
            val allCombinedDemandsForStation = mutableListOf<Pair<StationDemand, LoadCombination>>()

            // Expand combinations to account for lateral reversals (+ and - wind/seismic)
            combinations.forEach { combo ->
                allCombinedDemandsForStation.add(combineDemands(index, analysisResultsByCategory, combo, invertLateral = false) to combo)

                if (combo.isLateralReversible && (combo.factors.containsKey(LoadCategory.WIND) || combo.factors.containsKey(LoadCategory.SEISMIC))) {
                    allCombinedDemandsForStation.add(combineDemands(index, analysisResultsByCategory, combo, invertLateral = true) to combo)
                }
            }

            // 2. Separate by Limit State
            val strengthDemands = allCombinedDemandsForStation.filter { it.second.limitState == LimitState.STRENGTH }
            val serviceDemands = allCombinedDemandsForStation.filter { it.second.limitState == LimitState.SERVICEABILITY }

            // 3. Resolve controlling local demands at this specific station
            if (strengthDemands.isNotEmpty()) {
                // For a robust station demand envelope, find the max absolute moment effect at this point
                val maxStrength = strengthDemands.maxBy { maxOf(Math.abs(it.first.moment.value), Math.abs(it.first.shear.value)) } // simplified example placeholder
                strengthResults.add(StationEnvelopeResult(maxStrength.first.x, maxStrength.first, maxStrength.second))
            }

            if (serviceDemands.isNotEmpty()) {
                val maxService = serviceDemands.maxBy { Math.abs(it.first.deflection.value) }
                serviceResults.add(StationEnvelopeResult(maxService.first.x, maxService.first, maxService.second))
            }
        }

        // 4. Extract global worst-case critical values across the entire member length
        val governingMoment = strengthResults.maxBy { Math.abs(it.combinedDemand.moment.value) }
        val governingShear = strengthResults.maxBy { Math.abs(it.combinedDemand.shear.value) }
        val governingDeflection = serviceResults.maxBy { Math.abs(it.combinedDemand.deflection.value) }

        return MemberEnvelopeResult(
            strengthEnvelope = strengthResults,
            serviceabilityEnvelope = serviceResults,
            governingMaxMoment = governingMoment,
            governingMaxShear = governingShear,
            governingMaxDeflection = governingDeflection
        )
    }

    private fun combineDemands(
        stationIndex: Int,
        resultsByCategory: Map<LoadCategory, List<StationDemand>>,
        combo: LoadCombination,
        invertLateral: Boolean
    ): StationDemand {
        var mX = 0.0; var vY = 0.0; var pZ = 0.0
        var mY = 0.0; var vX = 0.0; var tZ = 0.0; var delta = 0.0

        val firstStation = resultsByCategory.values.first()[stationIndex]

        combo.factors.forEach { (category, baseFactor) ->
            resultsByCategory[category]?.get(stationIndex)?.let { demand ->
                // Apply a sign inversion factor if this is a reversed lateral load pass
                val operationalFactor = if (invertLateral && (category == LoadCategory.WIND || category == LoadCategory.SEISMIC)) {
                    -baseFactor
                } else {
                    baseFactor
                }

                // Explicit unit math values safely managed
                mX += operationalFactor * demand.moment.value
                vY += operationalFactor * demand.shear.value
                pZ += operationalFactor * demand.axial.value
                mY += operationalFactor * demand.momentY.value
                vX += operationalFactor * demand.shearY.value
                tZ += operationalFactor * demand.torque.value
                delta += operationalFactor * demand.deflection.value
            }
        }

        return firstStation.copy(
            moment = Moment(mX),
            shear = Force(vY),
            axial = Force(pZ),
            momentY = Moment(mY),
            shearY = Force(vX),
            torque = Moment(tZ),
            deflection = Length(delta)
        )
    }
}