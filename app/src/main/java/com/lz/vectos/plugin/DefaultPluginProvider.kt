package com.lz.vectos.plugin

import com.lz.domain.plugin.PluginProvider
import com.lz.domain.plugin.RegisteredModule
import com.lz.domain.plugin.RegisteredModuleRepository

class DefaultPluginProvider(
    private val repository: RegisteredModuleRepository
) : PluginProvider {

    override suspend fun getPlugin(moduleId: String): RegisteredModule? =
        repository.getModule(moduleId)

    override suspend fun getInstalledPlugins(): List<RegisteredModule> =
        repository.getModules()
}