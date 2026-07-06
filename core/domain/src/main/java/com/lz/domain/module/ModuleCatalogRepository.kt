package com.lz.domain.module

interface ModuleCatalogRepository {

    suspend fun getAvailableModules():
            List<ModuleDescriptor>
}