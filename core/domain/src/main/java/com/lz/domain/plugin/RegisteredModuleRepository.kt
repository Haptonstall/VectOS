package com.lz.domain.plugin

interface RegisteredModuleRepository {

    suspend fun register(
        module: RegisteredModule
    )

    suspend fun unregister(
        moduleId: String
    )

    suspend fun getModule(
        moduleId: String
    ): RegisteredModule?

    suspend fun getModules():
            List<RegisteredModule>
}