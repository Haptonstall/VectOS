package com.lz.runtime.registry

import com.lz.runtime.api.CapabilityRegistry
import com.lz.runtime.api.CapabilityType
import com.lz.runtime.api.ModuleCapability
import com.lz.runtime.core.AbstractRuntimeService
import java.util.concurrent.ConcurrentHashMap

class DefaultCapabilityRegistry :
    AbstractRuntimeService(
        "runtimeEnvironment.capabilityRegistry"
    ),
    CapabilityRegistry {

    private val capabilities =
        ConcurrentHashMap<String, ModuleCapability>()

    override fun onShutdown() {

        capabilities.clear()
    }

    override fun register(
        capability: ModuleCapability
    ) {

        capabilities[capability.id] = capability
    }

    override fun unregister(
        capabilityId: String
    ) {

        capabilities.remove(capabilityId)
    }

    override fun get(
        capabilityId: String
    ): ModuleCapability? {

        return capabilities[capabilityId]
    }

    override fun capabilities(): List<ModuleCapability> {

        return capabilities.values.toList()
    }

    override fun capabilities(
        type: CapabilityType
    ): List<ModuleCapability> {

        return capabilities.values.filter {
            it.capabilityType == type
        }
    }
}