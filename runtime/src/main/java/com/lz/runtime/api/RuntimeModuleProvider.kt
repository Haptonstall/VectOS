package com.lz.runtime.api

/**
 * Factory responsible for creating a RuntimeEnvironment RuntimeModule.
 *
 * Each installable engineering module contributes exactly one
 * RuntimeModuleProvider implementation.
 *
 * The RuntimeEnvironment discovers ModuleProviders dynamically.
 */
interface RuntimeModuleProvider {

    /**
     * Creates the RuntimeModule instance.
     *
     * Called once after the RuntimeEnvironment has verified the module
     * is installed and licensed.
     */
    fun createModule(): RuntimeModule
}