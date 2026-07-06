package com.lz.runtime.api

/**
 * Registry of loaded engineering modules.
 */
interface RuntimeModuleRegistry : RuntimeService {

    /**
     * Registers a module.
     */
    fun register(module: RuntimeModule)

    /**
     * Unregisters a module.
     */
    fun unregister(moduleId: String)

    /**
     * Returns a module.
     */
    fun get(moduleId: String): RuntimeModule?

    /**
     * Returns every loaded module.
     */
    fun modules(): List<RuntimeModule>

    /**
     * Returns true if loaded.
     */
    fun contains(moduleId: String): Boolean
}