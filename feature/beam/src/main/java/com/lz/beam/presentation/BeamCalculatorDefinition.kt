package com.lz.beam.presentation

import com.lz.domain.CalculationContext

object BeamCalculatorDefinition : CalculatorDefinition {

    override val id = "beam"
    override val displayName = "Beam Design"
    override val description = "Analyze bending, shear, torsion, and deflection"
    override val icon = Icons.Default.ShowChart
    override val supportsQuickCalc = true

    override fun createRoute(context: CalculationContext): CalculatorRoute =
        CalculatorRoute.BeamCalculator(context)
}