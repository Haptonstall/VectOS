package com.lz.vectos.ui.calculator

import com.lz.vectos.presentation.CalculationContext

sealed class CalculatorRoute {
    data class BeamCalculator(
        val context: CalculationContext
    ) : CalculatorRoute()
}
