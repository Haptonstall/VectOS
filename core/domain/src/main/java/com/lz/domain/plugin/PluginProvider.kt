package com.lz.domain.plugin

interface PluginProvider {

    suspend fun getPlugin(moduleId: String): RegisteredModule?

    suspend fun getInstalledPlugins(): List<RegisteredModule>
}