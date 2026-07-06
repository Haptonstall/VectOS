package com.lz.domain.module

interface ModuleProvider {

    suspend fun getPlugin(moduleId: String): RegisteredModule?

    suspend fun getInstalledPlugins(): List<RegisteredModule>
}