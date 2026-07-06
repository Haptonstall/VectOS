package com.lz.beam.plugin

import com.lz.domain.module.CalculatorPlugin
import com.lz.domain.module.ModuleDescriptor
import com.lz.domain.module.ModuleType
import com.lz.domain.module.ModuleEntryPoint

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

    override val entryPoint: ModuleEntryPoint =
        BeamEntryPoint()
}