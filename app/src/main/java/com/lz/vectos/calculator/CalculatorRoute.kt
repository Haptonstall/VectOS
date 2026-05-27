package com.lz.vectos.calculator

import com.lz.vectos.viewmodel.CalculationContext

sealed class CalculatorRoute {
    data class BeamCalculator(
        val context: CalculationContext
    ) : CalculatorRoute()
}
