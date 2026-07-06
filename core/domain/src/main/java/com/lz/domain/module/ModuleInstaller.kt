package com.lz.domain.module

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