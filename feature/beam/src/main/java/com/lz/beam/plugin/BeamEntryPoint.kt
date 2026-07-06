package com.lz.beam.plugin

import com.lz.domain.module.CalculatorDestination
import com.lz.domain.module.ModuleEntryPoint

class BeamEntryPoint : ModuleEntryPoint {

    override val destination =
        CalculatorDestination(
            route = "beam/home"
        )
}