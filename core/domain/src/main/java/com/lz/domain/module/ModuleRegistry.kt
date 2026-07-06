package com.lz.domain.module

interface ModuleRegistry {

    fun register(
        descriptor: ModuleDescriptor
    )

    fun unregister(
        moduleId: String
    )

    fun getModule(
        moduleId: String
    ): ModuleDescriptor?

    fun getModules(): List<ModuleDescriptor>
}