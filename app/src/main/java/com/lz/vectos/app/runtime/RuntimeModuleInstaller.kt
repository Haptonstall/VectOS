package com.lz.vectos.app.runtime

import com.lz.domain.module.InstallResult
import com.lz.domain.module.ModuleInstaller
import com.lz.runtime.api.RuntimeEnvironment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuntimeModuleInstaller @Inject constructor(

    private val runtime: RuntimeEnvironment

) : ModuleInstaller {

    override suspend fun install(
        moduleId: String
    ): InstallResult {

        val installed =
            runtime.context
                .runtimeModuleRegistry
                .contains(moduleId)

        return if (installed) {
            InstallResult.Success
        } else {
            InstallResult.Error(
                "Module '$moduleId' is not available."
            )
        }
    }

    override suspend fun uninstall(
        moduleId: String
    ): InstallResult {

        // Marketplace removal will be implemented later.

        return InstallResult.Success
    }

    override suspend fun isInstalled(
        moduleId: String
    ): Boolean {

        return runtime.context
            .runtimeModuleRegistry
            .contains(moduleId)
    }
}