package com.lz.domain.plugin

interface ModuleCatalogRepository {

    suspend fun getAvailableModules():
            List<ModuleDescriptor>
}