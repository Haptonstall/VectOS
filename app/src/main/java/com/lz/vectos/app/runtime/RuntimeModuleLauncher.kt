package com.lz.vectos.app.runtime

import com.lz.domain.module.ModuleLauncher
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.compose.api.ComposeRuntimeModule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuntimeModuleLauncher @Inject constructor(

    private val runtime: RuntimeEnvironment

) : ModuleLauncher {

    override suspend fun open(
        moduleId: String
    ): String {

        val module =
            runtime.context
                .runtimeModuleRegistry
                .get(moduleId)
                ?: error("Module not loaded: $moduleId")

        val composeModule =
            module as? ComposeRuntimeModule
                ?: error("Module does not expose Compose screens.")

        return composeModule
            .screenProviders()
            .first()
            .destinations()
            .first()
            .id
    }
}