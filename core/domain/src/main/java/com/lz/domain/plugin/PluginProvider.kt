package com.lz.domain.plugin

interface PluginProvider {

    suspend fun getPluginManifest(
        moduleId: String
    ): PluginManifest?

    suspend fun getInstalledPlugins():
        List<PluginManifest>
}