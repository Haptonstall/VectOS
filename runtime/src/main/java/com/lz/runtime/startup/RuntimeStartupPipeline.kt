package com.lz.runtime.startup

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeReadyEvent
import com.lz.runtime.api.RuntimeStartingEvent
import com.lz.runtime.api.RuntimeStoppedEvent
import com.lz.runtime.loader.RuntimeModuleInstaller

/**
 * Coordinates Runtime startup and shutdown.
 */
class RuntimeStartupPipeline(

    private val context: RuntimeContext,

    private val installer: RuntimeModuleInstaller

) {

    fun start() {

        context.eventBus.publish(
            RuntimeStartingEvent()
        )

        installer.initialize(context)

        installer.installAll()

        context.eventBus.publish(
            RuntimeReadyEvent()
        )
    }

    fun stop() {

        installer.uninstallAll()

        installer.shutdown(context)

        context.eventBus.publish(
            RuntimeStoppedEvent()
        )
    }
}