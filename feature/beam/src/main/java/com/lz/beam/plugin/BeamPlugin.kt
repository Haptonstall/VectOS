package com.lz.beam.plugin

import com.lz.domain.plugin.CalculatorPlugin
import com.lz.domain.plugin.ModuleDescriptor
import com.lz.domain.plugin.ModuleType
import com.lz.domain.plugin.PluginEntryPoint

class BeamPlugin : CalculatorPlugin {

    override val descriptor =
        ModuleDescriptor(
            id = "beam",
            displayName = "Beam Design",
            description = "Steel, wood, and aluminum beam design",
            version = "1.0.0",
            moduleType = ModuleType.BEAM,
            dynamicFeatureName = "beam",
            supportsProjectMode = true,
            supportsQuickCalcMode = true
        )

    override val entryPoint: PluginEntryPoint =
        BeamEntryPoint()
}