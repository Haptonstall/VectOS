package com.lz.beam.runtime

import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.capabilities.ModuleCapability
import com.lz.runtime.compose.api.ComposeRuntimeModule
import com.lz.runtime.compose.screen.api.ScreenProvider

class BeamRuntimeModule :
    RuntimeModule,
    ComposeRuntimeModule {

    override val descriptor =
        BeamDescriptor

    private val capabilities: List<ModuleCapability> =
        listOf(
            BeamCalculatorCapability()
        )

    private val screenProviders = listOf<ScreenProvider>(
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

    override fun screenProviders(): List<ScreenProvider> =
            screenProviders

}