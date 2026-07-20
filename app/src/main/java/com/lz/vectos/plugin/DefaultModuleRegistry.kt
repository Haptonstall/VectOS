package com.lz.vectos.plugin

import com.lz.domain.module.ModuleDescriptor
import com.lz.domain.module.ModuleRegistry
import com.lz.runtime.api.CapabilityType
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.api.capabilities.CalculatorCapability
import com.lz.vectos.app.runtime.toModuleDescriptor

class DefaultModuleRegistry(
    private val runtime: RuntimeEnvironment
) : ModuleRegistry {

    override fun register(descriptor: ModuleDescriptor) {
        // Runtime owns registration
    }

    override fun unregister(moduleId: String) {
        // Runtime owns unregistration
    }

    override fun getModule(moduleId: String): ModuleDescriptor? {
        return getModules()
            .firstOrNull {
                it.id == moduleId
            }
    }

    override fun getModules(): List<ModuleDescriptor> {

        return runtime.context
            .capabilityRegistry
            .capabilities(CapabilityType.CALCULATOR)
            .filterIsInstance<CalculatorCapability>()
            .mapNotNull { capability ->

                runtime.context
                    .runtimeModuleRegistry
                    .get(capability.runtimeModuleId)
                    ?.descriptor
                    ?.toModuleDescriptor(capability)

            }

    }
}
