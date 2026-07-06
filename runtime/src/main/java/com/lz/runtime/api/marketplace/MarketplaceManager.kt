package com.lz.runtime.api.marketplace

import com.lz.runtime.api.RuntimeService

interface MarketplaceManager : RuntimeService {

    fun installedModules(): List<InstalledModule>

    fun isInstalled(
        moduleId: String
    ): Boolean
}