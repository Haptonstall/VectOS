package com.lz.runtime.repository

import com.lz.runtime.api.marketplace.InstalledModule

/**
 * Repository describing installed Runtime modules.
 *
 * This is intentionally simple.
 *
 * Later this becomes:
 *
 *      Room
 *      Marketplace
 *      Google Play
 *      Enterprise deployment
 *
 * without changing Runtime.
 */
interface InstalledModuleRepository {

    fun installedModules(): List<InstalledModule>

}
