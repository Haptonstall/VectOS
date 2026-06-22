package com.lz.vectos.plugin

import com.lz.domain.plugin.InstallResult
import com.lz.domain.plugin.ModuleInstaller
import com.lz.domain.plugin.RegisteredModuleRepository

class DynamicFeatureModuleInstaller(
    private val registeredModules:
        RegisteredModuleRepository
) : ModuleInstaller {

    override suspend fun install(
        moduleId: String
    ): InstallResult {

        return InstallResult.Success
    }

    override suspend fun uninstall(
        moduleId: String
    ): InstallResult {
        registeredModules.unregister(
            moduleId
        )
        return InstallResult.Success
    }

    override suspend fun isInstalled(
        moduleId: String
    ): Boolean {

        return registeredModules
            .getModule(
                moduleId
            )!= null
    }
}