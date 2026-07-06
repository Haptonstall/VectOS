package com.lz.beam.runtime

import com.lz.runtime.api.*
import com.lz.runtime.api.capabilities.CalculatorCapability

class BeamCalculatorCapability : CalculatorCapability {

    override val id =
        "beam.calculator"

    override val runtimeModuleId =
        BeamDescriptor.id

    override val displayName =
        "Beam Calculator"

    val description =
        "Analyze and design beams."

    override val capabilityType =
        CapabilityType.CALCULATOR

    override val supportsQuickCalc =
        true

    override val destination =
        BeamNavigationDestination

}