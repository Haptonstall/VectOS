package com.lz.vectos.plugin

import com.lz.common.ModuleType
import com.lz.common.Version
import com.lz.domain.module.ModuleCatalogRepository
import com.lz.domain.module.ModuleDescriptor

class LocalModuleCatalogRepository :
    ModuleCatalogRepository {

    override suspend fun getAvailableModules():
            List<ModuleDescriptor> {

        return listOf(
            ModuleDescriptor(
                id = "beam",
                displayName = "Beam Design",
                description = "Steel and wood beam design",
                version = Version(1, 0, 0),
                moduleType = ModuleType.CALCULATION,
                dynamicFeatureName = "beam",
                supportsProjectMode = true,
                supportsQuickCalcMode = true
            ),
            ModuleDescriptor(
                id = "column",
                displayName = "Column Design",
                description = "Column design module",
                version = Version(1,0,0),
                moduleType = ModuleType.CALCULATION,
                dynamicFeatureName = "column",
                supportsProjectMode = true,
                supportsQuickCalcMode = false
            )
        )
    }
}