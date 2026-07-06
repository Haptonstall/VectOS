package com.lz.runtime.startup

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeModuleLoader
import com.lz.runtime.loader.RuntimeModuleInstaller

/**
 * Executes complete Runtime startup.
 */
class RuntimeStartupPipeline(

    private val context: RuntimeContext,

    private val loader: RuntimeModuleLoader,

    private val installer: RuntimeModuleInstaller

) {

    fun start() {

        loader.initialize(context)

        loader
            .loadModules()
            .forEach {

                installer.install(it)

            }
    }

    fun stop() {

        context
            .runtimeModuleRegistry
            .modules()
            .reversed()
            .forEach {

                installer.uninstall(it)

            }

        loader.shutdown(context)
    }
}