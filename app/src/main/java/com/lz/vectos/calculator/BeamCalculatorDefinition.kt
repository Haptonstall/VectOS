package com.lz.vectos.calculator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import com.lz.vectos.viewmodel.CalculationContext

object BeamCalculatorDefinition : CalculatorDefinition {

    override val id = "beam_simply_supported"
    override val displayName = "Beam Design"
    override val description = "Analyze bending, shear, and deflection"
    override val icon = Icons.Default.ShowChart
    override val supportsQuickCalc = true

    override fun createRoute(context: CalculationContext): CalculatorRoute =
        CalculatorRoute.BeamCalculator(context)
}
