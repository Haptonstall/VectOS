package com.lz.runtime.api

import com.lz.runtime.api.capabilities.ModuleCapability

/**
 * Base contract implemented by every engineering module.
 *
 * A RuntimeModule owns its lifecycle and contributes capabilities
 * to the RuntimeEnvironment.
 */
interface RuntimeModule {

    /**
     * Static information describing the module.
     */
    val descriptor: RuntimeModuleDescriptor

    /**
     * Called once when the RuntimeEnvironment loads the module.
     */
    fun initialize(
        runtime: RuntimeEnvironment
    )

    /**
     * Called before the RuntimeEnvironment unloads the module.
     */
    fun shutdown(
        runtime: RuntimeEnvironment
    )

    /**
     * Capabilities contributed by this module.
     */
    fun capabilities(): List<ModuleCapability>

}