package com.lz.runtime.boot

import com.lz.runtime.api.RuntimeConfiguration
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.core.DefaultRuntimeEnvironment
import com.lz.runtime.loader.RuntimeModuleInstaller
import com.lz.runtime.startup.RuntimeStartupPipeline

object RuntimeBootstrapper {

    fun create(
        configuration: RuntimeConfiguration,
        installer: RuntimeModuleInstaller
    ): RuntimeEnvironment {

        val runtime =
            DefaultRuntimeEnvironment(configuration)

        runtime.attachPipeline(
            RuntimeStartupPipeline(
                runtime.context,
                installer
            )
        )

        return runtime
    }
}