package com.lz.vectos.plugin

import com.lz.domain.module.*

class LocalRegisteredModuleRepository :
    RegisteredModuleRepository {

    private val modules =
        mutableMapOf<String, RegisteredModule>()

    override suspend fun register(
        module: RegisteredModule
    ) {

        modules[module.descriptor.id] = module
    }

    override suspend fun unregister(
        moduleId: String
    ) {

        modules.remove(moduleId)
    }

    override suspend fun getModule(
        moduleId: String
    ): RegisteredModule? {

        return modules[moduleId]
    }

    override suspend fun getModules():
            List<RegisteredModule> {

        return modules.values.toList()
    }
}