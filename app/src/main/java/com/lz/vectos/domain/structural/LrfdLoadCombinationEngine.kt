package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment
import java.util.Locale

/**
 * Deterministic engine to resolve governing factored demands (LRFD) per ASCE 7.
 */
object LrfdLoadCombinationEngine {

    data class GoverningFactoredDemand(
        val moment: Double,
        val shear: Double,
        val controllingCombination: LoadCombination,
        val trace: DesignEquationTrace
    )

    fun resolve(
        member: StructuralMember,
        loads: List<Load>,
        asceEdition: AsceEdition
    ): GoverningFactoredDemand {
        val combinations = LrfdLoadCombinationRepository.getCombinations(asceEdition)
        
        var maxMoment = 0.0
        var maxShear = 0.0
        var controllingCombo = combinations.first()

        combinations.forEach { combo ->
            // Using sumEffects logic similar to Asd engine for Step 32B factored results
            val momentValue = sumEffects(loads, combo, 1000.0) // Mock logic for Mu
            val shearValue = sumEffects(loads, combo, 500.0)   // Mock logic for Vu

            if (momentValue > maxMoment) {
                maxMoment = momentValue
                maxShear = shearValue
                controllingCombo = combo
            }
        }

        val trace = DesignEquationTrace(
            symbolicEquation = controllingCombo.equation,
            substitutedEquation = "Factored sum of categories per ${controllingCombo.name}",
            result = String.format(Locale.US, "%.2f", maxMoment),
            units = "N-m", // SI internally
            codeReference = controllingCombo.codeReference
        )

        return GoverningFactoredDemand(maxMoment, maxShear, controllingCombo, trace)
    }

    private fun sumEffects(
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
