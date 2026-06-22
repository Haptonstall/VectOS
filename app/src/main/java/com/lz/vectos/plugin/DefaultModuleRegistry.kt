package com.lz.vectos.plugin

import com.lz.domain.plugin.ModuleDescriptor
import com.lz.domain.plugin.ModuleRegistry
import com.lz.domain.plugin.PluginProvider
import kotlinx.coroutines.runBlocking

class DefaultModuleRegistry(
    private val pluginProvider:
        PluginProvider
): ModuleRegistry {
    override fun register(
        descriptor: ModuleDescriptor
    ) {
    }

    override fun unregister(
        moduleId: String
    ) {
    }

    override fun getModule(
        moduleId: String
    ): ModuleDescriptor? {
        return runBlocking {
            pluginProvider
                .getPluginManifest(
                    moduleId
                )
                ?.descriptor
        }
    }

    override fun getModules():
        List<ModuleDescriptor> {

        return  runBlocking {
            pluginProvider
                .getInstalledPlugins()
                .map {
                    it.descriptor
                }
        }
    }
}