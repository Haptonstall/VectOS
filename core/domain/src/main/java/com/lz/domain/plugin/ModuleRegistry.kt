package com.lz.domain.plugin

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