package com.lz.vectos.plugin

import com.lz.domain.module.ModuleDescriptor
import com.lz.domain.module.ModuleRegistry
import com.lz.domain.module.RegisteredModule
import com.lz.domain.module.RegisteredModuleRepository
import kotlinx.coroutines.runBlocking

class DefaultModuleRegistry(
    private val repository: RegisteredModuleRepository
) : ModuleRegistry {

    override fun register(descriptor: ModuleDescriptor) {
        runBlocking {
            repository.register(
                RegisteredModule(
                    descriptor = descriptor,
                    route = "${descriptor.moduleType.name.lowercase()}/home"
                )
            )
        }
    }

    override fun unregister(moduleId: String) {
        runBlocking { repository.unregister(moduleId) }
    }

    override fun getModule(moduleId: String): ModuleDescriptor? =
        runBlocking { repository.getModule(moduleId)?.descriptor }

    override fun getModules(): List<ModuleDescriptor> =
        runBlocking { repository.getModules().map { it.descriptor } }
}