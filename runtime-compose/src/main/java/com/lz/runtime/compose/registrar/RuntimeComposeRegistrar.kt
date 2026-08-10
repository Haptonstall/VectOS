package com.lz.runtime.compose.registrar

import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.compose.api.ComposeRuntimeModule
import com.lz.runtime.compose.screen.api.ScreenRegistry

class RuntimeComposeRegistrar(

    private val runtime: RuntimeEnvironment,

    private val screenRegistry: ScreenRegistry

) {

    fun register() {

        runtime.context
            .runtimeModuleRegistry
            .modules()
            .filterIsInstance<ComposeRuntimeModule>()
            .forEach { module ->

                module.screenProviders()
                    .forEach(screenRegistry::register)

            }

    }

}