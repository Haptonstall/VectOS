package com.lz.vectos.plugin

import com.lz.domain.plugin.ModuleLauncher
import com.lz.domain.plugin.ModuleRegistry
import com.lz.domain.plugin.ModuleType

class DynamicModuleLauncher(
    private val registry: ModuleRegistry
) : ModuleLauncher {

    override suspend fun open(
        moduleId: String
    ): String {

        val descriptor =
            registry.getModule(
                moduleId
            )
                ?: error(
                    "Module not found: $moduleId"
                )

        return when (
            descriptor.moduleType
        ) {

            ModuleType.BEAM ->
                "beam/home"

            ModuleType.COLUMN ->
                "column/home"

            ModuleType.POLE ->
                "pole/home"

            ModuleType.FOUNDATION ->
                "foundation/home"

            else ->
                error(
                    "No route defined."
                )
        }
    }
}