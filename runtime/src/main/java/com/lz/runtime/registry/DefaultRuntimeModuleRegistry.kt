package com.lz.runtime.registry

import com.lz.runtime.api.RuntimeModuleRegistry
import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.core.AbstractRuntimeService

class DefaultRuntimeModuleRegistry :
    AbstractRuntimeService(
        "runtimeEnvironment.runtimeModuleRegistry"
    ),
    RuntimeModuleRegistry {

    private val modules =
        RegistryStore<RuntimeModule> {
            it.descriptor.id
        }

    override fun onShutdown() =
        modules.clear()

    override fun register(module: RuntimeModule) =
        modules.register(module)

    override fun unregister(moduleId: String) =
        modules.unregister(moduleId)

    override fun get(moduleId: String): RuntimeModule? =
        modules.get(moduleId)

    override fun modules(): List<RuntimeModule> =
        modules.all()

    override fun contains(moduleId: String): Boolean =
        modules.contains(moduleId)

    }