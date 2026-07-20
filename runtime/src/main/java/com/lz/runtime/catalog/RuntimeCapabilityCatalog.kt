package com.lz.runtime.catalog

import com.lz.runtime.api.CapabilityRegistry
import com.lz.runtime.api.CapabilityType
import com.lz.runtime.api.capabilities.CalculatorCapability

class RuntimeCapabilityCatalog(

    private val registry: CapabilityRegistry

) {

    fun calculators(): List<CalculatorCapability> {

        return registry
            .capabilities(
                CapabilityType.CALCULATOR
            )
            .filterIsInstance<CalculatorCapability>()

    }

}