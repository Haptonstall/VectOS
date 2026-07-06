package com.lz.vectos.plugin

import com.lz.domain.module.ModuleLauncher
import com.lz.domain.module.RegisteredModuleRepository

class ProductionModuleLauncher(
    private val repository:
        RegisteredModuleRepository
) : ModuleLauncher {

    override suspend fun open(
        moduleId: String
    ): String {

        val module =
            repository.getModule(
                moduleId
            )
                ?: throw IllegalArgumentException(
                    "Module not installed"
                )

        return module.route
    }
}