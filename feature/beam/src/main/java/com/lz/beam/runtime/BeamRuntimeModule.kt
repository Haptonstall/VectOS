package com.lz.beam.runtime

import com.lz.runtime.api.*
import com.lz.runtime.api.capabilities.ModuleCapability
import com.lz.runtime.screen.api.ScreenProvider

class BeamRuntimeModule : RuntimeModule {

    override val descriptor =
        BeamDescriptor

    private val capabilities: List<ModuleCapability> =
        listOf(
            BeamCalculatorCapability()
        )

    private val screenProviders =
        listOf(
            BeamScreenProvider()
    )

    override fun initialize(
        runtime: RuntimeEnvironment
    ) {

        // Nothing yet.
    }

    override fun shutdown(
        runtime: RuntimeEnvironment
    ) {

    }

    override fun capabilities(): List<ModuleCapability> =
        capabilities

    override fun screenProviders(): List<ScreenProvider> {
        return listOf(
            BeamScreenProvider()
        )
    }
}