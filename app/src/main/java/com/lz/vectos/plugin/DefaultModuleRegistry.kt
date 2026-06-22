package com.lz.vectos.plugin

import com.lz.domain.plugin.ModuleDescriptor
import com.lz.domain.plugin.ModuleRegistry

class DefaultModuleRegistry : ModuleRegistry {

    private val modules =
        mutableMapOf<String, ModuleDescriptor>()

    override fun register(
        descriptor: ModuleDescriptor
    ) {
        modules[descriptor.id] = descriptor
    }

    override fun unregister(
        moduleId: String
    ) {
        modules.remove(moduleId)
    }

    override fun getModule(
        moduleId: String
    ): ModuleDescriptor? =
        modules[moduleId]

    override fun getModules(): List<ModuleDescriptor> =
        modules.values.toList()
}