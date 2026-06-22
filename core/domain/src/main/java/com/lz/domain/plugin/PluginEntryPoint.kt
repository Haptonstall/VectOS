package com.lz.domain.plugin

import com.lz.domain.calculation.CalculationContext

interface PluginEntryPoint {

    val destination: CalculatorDestination
}