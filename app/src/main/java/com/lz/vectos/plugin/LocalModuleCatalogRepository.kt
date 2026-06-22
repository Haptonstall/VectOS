package com.lz.vectos.plugin

import com.lz.domain.plugin.*

class LocalModuleCatalogRepository :
    ModuleCatalogRepository {

    override suspend fun getAvailableModules():
            List<ModuleDescriptor> {

        return listOf(
            ModuleDescriptor(
                id = "beam",
                displayName = "Beam Design",
                description = "Steel and wood beam design",
                version = "1.0.0",
                moduleType = ModuleType.BEAM,
                dynamicFeatureName = "beam",
                supportsProjectMode = true,
                supportsQuickCalcMode = true
            ),
            ModuleDescriptor(
                id = "column",
                displayName = "Column Design",
                description = "Column design module",
                version = "1.0.0",
                moduleType = ModuleType.COLUMN,
                dynamicFeatureName = "column",
                supportsProjectMode = true,
                supportsQuickCalcMode = false
            )
        )
    }
}