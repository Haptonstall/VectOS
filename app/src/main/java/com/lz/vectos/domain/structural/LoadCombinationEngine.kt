package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.*
import java.util.UUID

/**
 * Utility for resolving governing structural demands by applying code-mandated load combinations
 * to analyzed station results.
 */
object LoadCombinationEngine {

    /**
     * Result of an envelope resolution at a specific station.
     */
    data class StationEnvelopeResult(
        val x: Length,
        val governingDemand: StationDemand,
        val governingCombination: LoadCombination,
        val allCombinationsResults: Map<String, StationDemand>
    )

    /**
     * Resolves the governing envelope for a member by processing every station across all combinations.
     */
    fun resolveEnvelope(
        analysisResultsByCategory: Map<LoadCategory, List<StationDemand>>,
        combinations: List<LoadCombination>
    ): List<StationEnvelopeResult> {
        if (analysisResultsByCategory.isEmpty() || combinations.isEmpty()) return emptyList()

        // Ensure all categories have the same number of stations
        val sizes = analysisResultsByCategory.mapValues { it.value.size }
        val distinctSizes = sizes.values.distinct()
        if (distinctSizes.size > 1) {
            val details = sizes.entries.joinToString { "${it.key}: ${it.value}" }
            throw IllegalArgumentException("All load categories must have the same number of analysis stations. Found: $details")
        }

        // Assume all categories have the same station points
        val firstCategory = analysisResultsByCategory.keys.first()
        val stations = analysisResultsByCategory[firstCategory] ?: return emptyList()

        return stations.mapIndexed { index, _ ->
            resolveGoverningStation(index, analysisResultsByCategory, combinations)
        }
    }

    private fun resolveGoverningStation(
        stationIndex: Int,
        resultsByCategory: Map<LoadCategory, List<StationDemand>>,
        combinations: List<LoadCombination>
    ): StationEnvelopeResult {
        val resultsByCombo = mutableMapOf<String, StationDemand>()
        
        combinations.forEach { combo ->
            resultsByCombo[combo.name] = combineStationDemands(stationIndex, resultsByCategory, combo)
        }

        // Find governing by absolute max moment (simple implementation)
        val governingEntry = resultsByCombo.entries.maxByOrNull { Math.abs(it.value.moment.inLbIn) }!!
        val governingCombo = combinations.find { it.name == governingEntry.key }!!

        return StationEnvelopeResult(
            x = governingEntry.value.x,
            governingDemand = governingEntry.value,
            governingCombination = governingCombo,
            allCombinationsResults = resultsByCombo
        )
    }

    private fun combineStationDemands(
        stationIndex: Int,
        resultsByCategory: Map<LoadCategory, List<StationDemand>>,
        combo: LoadCombination
    ): StationDemand {
        var totalMoment = 0.0
        var totalShear = 0.0
        var totalAxial = 0.0
        var totalMomentY = 0.0
        var totalShearY = 0.0
        var totalTorque = 0.0
        var totalDeflection = 0.0
        
        val firstStation = resultsByCategory.values.first()[stationIndex]

        combo.factors.forEach { (category, factor) ->
            resultsByCategory[category]?.get(stationIndex)?.let { demand ->
                totalMoment += factor * demand.moment.inLbIn
                totalShear += factor * demand.shear.inPoundsForce
                totalAxial += factor * demand.axial.inPoundsForce
                totalMomentY += factor * demand.momentY.inLbIn
                totalShearY += factor * demand.shearY.inPoundsForce
                totalTorque += factor * demand.torque.inLbIn
                totalDeflection += factor * demand.deflection.inInches
            }
        }

        return StationDemand(
            x = firstStation.x,
            moment = Moment(totalMoment),
            shear = Force(totalShear),
            axial = Force(totalAxial),
            momentY = Moment(totalMomentY),
            shearY = Force(totalShearY),
            torque = Moment(totalTorque),
            deflection = Length(totalDeflection),
            spanId = firstStation.spanId,
            cb = firstStation.cb,
            lbTop = firstStation.lbTop,
            lbBottom = firstStation.lbBottom,
            allowableDeflection = firstStation.allowableDeflection,
            compressionFlange = firstStation.compressionFlange
        )
    }
}
