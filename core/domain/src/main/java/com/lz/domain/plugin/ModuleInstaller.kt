package com.lz.domain.plugin

interface ModuleInstaller {

    suspend fun install(
        moduleId: String
    )

    suspend fun uninstall(
        moduleId: String
    )

    suspend fun isInstalled(
        moduleId: String
    ): Boolean
}