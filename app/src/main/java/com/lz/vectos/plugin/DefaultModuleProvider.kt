package com.lz.vectos.plugin

import com.lz.domain.module.ModuleProvider
import com.lz.domain.module.RegisteredModule
import com.lz.domain.module.RegisteredModuleRepository

class DefaultModuleProvider(
    private val repository: RegisteredModuleRepository
) : ModuleProvider {

    override suspend fun getPlugin(moduleId: String): RegisteredModule? =
        repository.getModule(moduleId)

    override suspend fun getInstalledPlugins(): List<RegisteredModule> =
        repository.getModules()
}