package com.lz.runtime.compose.api

import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.compose.registrar.RuntimeComposeRegistrar
import com.lz.runtime.compose.screen.DefaultScreenRegistry
import com.lz.runtime.compose.screen.api.ScreenRegistry

class RuntimeComposeEnvironment(

    val runtime: RuntimeEnvironment

) {

    val screenRegistry: ScreenRegistry =
        DefaultScreenRegistry()

    fun start() {

        RuntimeComposeRegistrar(
            runtime = runtime,
            screenRegistry = screenRegistry
        ).register()

    }

}