package com.lz.runtime.boot

import com.lz.runtime.api.RuntimeConfiguration
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.core.DefaultRuntimeEnvironment
import com.lz.runtime.discovery.DefaultPlatformModuleDiscovery
import com.lz.runtime.loader.DefaultRuntimeModuleLoader
import com.lz.runtime.loader.RuntimeModuleInstaller
import com.lz.runtime.startup.RuntimeStartupPipeline

object RuntimeBootstrapper {

    fun create(

        configuration: RuntimeConfiguration

    ): RuntimeEnvironment {

        val runtime =
            DefaultRuntimeEnvironment(
                configuration
            )

        val discovery =
            DefaultPlatformModuleDiscovery()

        val loader =
            DefaultRuntimeModuleLoader(
                discovery
            )

        val installer =
            RuntimeModuleInstaller(
                runtime.context
            )

        val pipeline =
            RuntimeStartupPipeline(
                runtime.context,
                loader,
                installer
            )

        runtime.attachPipeline(
            pipeline
        )

        return runtime
    }
}