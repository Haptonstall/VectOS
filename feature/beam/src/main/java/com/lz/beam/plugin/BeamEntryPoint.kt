package com.lz.beam.plugin

import com.lz.domain.plugin.CalculatorDestination
import com.lz.domain.plugin.PluginEntryPoint

class BeamEntryPoint : PluginEntryPoint {

    override val destination =
        CalculatorDestination(
            route = "beam/home"
        )
}