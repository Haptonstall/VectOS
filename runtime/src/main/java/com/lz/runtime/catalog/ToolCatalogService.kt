package com.lz.runtime.catalog

import com.lz.runtime.api.capabilities.CalculatorCapability

class ToolCatalogService(

    private val catalog: RuntimeCapabilityCatalog

) {

    fun projectTools(): List<CalculatorCapability> {

        return catalog
            .calculators()
            .filter {

                it.supportsProjectMode

            }

    }

    fun quickTools(): List<CalculatorCapability> {

        return catalog
            .calculators()
            .filter {

                it.supportsQuickCalcMode

            }

    }

}