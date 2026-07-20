package com.lz.runtime.marketplace

import com.lz.runtime.api.marketplace.*
import com.lz.runtime.core.AbstractRuntimeService

class DefaultMarketplaceManager :
    AbstractRuntimeService(
        "runtime.marketplace"
    ),
    MarketplaceManager {

    private val installed =
        mutableMapOf<String, InstalledModule>()

    override fun installedModules(): List<InstalledModule> {

        return installed.values.toList()
    }

    override fun isInstalled(
        moduleId: String
    ): Boolean {

        return installed[moduleId]?.installState ==
            InstallState.INSTALLED
    }
}
