package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment
import java.util.Locale

/**
 * Deterministic engine to resolve governing ASD demands per ASCE 7.
 */
object AsdLoadCombinationEngine {

    data class GoverningDemand(
        val moment: Double,
        val shear: Double,
        val controllingCombination: LoadCombination,
        val trace: DesignEquationTrace
    )

    fun resolve(
        member: StructuralMember,
        loads: List<Load>,
        asceEdition: AsceEdition
    ): GoverningDemand {
        val combinations = AsdLoadCombinationRepository.getAsdCombinations(asceEdition)
        
        var maxMoment = 0.0
        var maxShear = 0.0
        var controllingCombo = combinations.first()

        // Iterate through each ASCE 7 ASD combination
        combinations.forEach { combo ->
            val combinedMoment = computeCombinedEffect(loads, combo, 1000.0) // Mock logic for Step 32A
            val combinedShear = computeCombinedEffect(loads, combo, 500.0)

            if (combinedMoment > maxMoment) {
                maxMoment = combinedMoment
                maxShear = combinedShear
                controllingCombo = combo
            }
        }

        val trace = DesignEquationTrace(
            symbolicEquation = controllingCombo.equation,
            substitutedEquation = "Sum of categories per ${controllingCombo.name}",
            result = String.format(Locale.US, "%.2f", maxMoment),
            units = "N-m", // Simplified for internal SI, converted in ViewModel
            codeReference = controllingCombo.codeReference
        )

        return GoverningDemand(maxMoment, maxShear, controllingCombo, trace)
    }

    private fun computeCombinedEffect(
        loads: List<Load>,
        combo: LoadCombination,
        baseEffect: Double
    ): Double {
        var total = 0.0
        combo.factors.forEach { (category, factor) ->
            val categoryTotal = loads.filter { it.category == category }.sumOf { load ->
                when (load) {
                    is Load.PointLoad -> load.value
                    is Load.UniformDistributedLoad -> load.value
                    is Load.AxialLoad -> load.value
                    is Load.TrapezoidalLoad -> (load.valueStart + load.valueEnd) / 2.0
                }
            }
            total += factor * categoryTotal * (baseEffect / 1000.0)
        }
        return total
    }
}
