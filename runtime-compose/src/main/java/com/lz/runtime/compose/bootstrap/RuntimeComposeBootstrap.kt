package com.lz.runtime.compose.bootstrap

import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.compose.registrar.RuntimeComposeRegistrar
import com.lz.runtime.compose.screen.api.ScreenRegistry

object RuntimeComposeBootstrap {

    fun start(
        runtime: RuntimeEnvironment,
        screenRegistry: ScreenRegistry
    ) {

        RuntimeComposeRegistrar(
            runtime,
            screenRegistry
        ).register()

    }

}