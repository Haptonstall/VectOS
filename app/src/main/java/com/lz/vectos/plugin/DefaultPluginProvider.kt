package com.lz.vectos.plugin

import com.lz.domain.plugin.PluginProvider

class DefaultPluginProvider(
    private val repository:
       InstalledPluginRepository
) : PluginProvider {

    override suspend fun getPluginManifest(
        moduleId: String
    ): PluginManifest? {
        return repository
            .getInstalledPlugins()
            .firstOrNull{
                it.descriptor.id == moduleId
            }
    }

    override suspend fun getInstalledPlugins():
        List<PluginManifest> {
        return repository
            .getInstalledPlugins()
    }
}