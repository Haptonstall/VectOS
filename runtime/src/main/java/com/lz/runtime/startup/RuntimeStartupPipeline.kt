package com.lz.runtime.startup

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeModuleLoadFailedEvent
import com.lz.runtime.api.RuntimeModuleProvider
import com.lz.runtime.loader.RuntimeModuleInstaller

/**
 * Coordinates Runtime startup and shutdown.
 *
 * Discovery and installation are delegated entirely to RuntimeModuleInstaller
 */
class RuntimeStartupPipeline(

    private val context: RuntimeContext,

    private val installer: RuntimeModuleInstaller

) {

    fun start() {
        installer.initialize(context)
        installer.installAll()
    }

    fun stop() {
        installer.uninstallAll()
        installer.shutdown(context)
    }
}
