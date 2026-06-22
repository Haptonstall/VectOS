package com.lz.domain.plugin

interface ModuleInstaller {

    suspend fun install(
        moduleId: String
    ): InstallResult

    suspend fun uninstall(
        moduleId: String
    ): InstallResult

    suspend fun isInstalled(
        moduleId: String
    ): Boolean
}