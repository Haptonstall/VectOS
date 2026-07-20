package com.lz.runtime.api

/**
 * Published when one module provider or module fails during startup.
 *
 * Runtime startup continues after this event.
 */
data class RuntimeModuleLoadFailedEvent(

    val providerName: String,

    val message: String?,

    override val timestamp: Long =
        System.currentTimeMillis()

) : RuntimeEvent {

    override val id: String =
        "runtime.module.load_failed"
}
