package com.lz.runtime.api.licensing

import com.lz.runtime.api.RuntimeService

/**
 * Determines whether Runtime modules are licensed.
 *
 * The Runtime never communicates directly with Google Play,
 * Stripe, etc.
 */
interface LicenseManager : RuntimeService {

    /**
     * Returns the current license state for the specified module.
     */
    fun licenseState(
        moduleId: String
    ): LicenseState

    /**
     * Convenience helper.
     */
    fun isLicensed(
        moduleId: String
    ): Boolean =
        licenseState(moduleId) == LicenseState.LICENSED
}