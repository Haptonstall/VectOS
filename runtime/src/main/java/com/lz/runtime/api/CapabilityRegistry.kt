package com.lz.runtime.api

import com.lz.runtime.api.capabilities.ModuleCapability

/**
 * Registry of capabilities contributed by all loaded modules.
 */
interface CapabilityRegistry : RuntimeService {

    /**
     * Registers a capability.
     */
    fun register(capability: ModuleCapability)

    /**
     * Removes a capability.
     */
    fun unregister(capabilityId: String)

    /**
     * Returns one capability.
     */
    fun get(capabilityId: String): ModuleCapability?

    /**
     * Returns all capabilities.
     */
    fun capabilities(): List<ModuleCapability>

    /**
     * Returns all capabilities of one type.
     */
    fun capabilities(
        type: CapabilityType
    ): List<ModuleCapability>
}