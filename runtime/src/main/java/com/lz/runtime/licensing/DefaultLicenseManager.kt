package com.lz.runtime.licensing

import com.lz.runtime.api.licensing.*
import com.lz.runtime.core.AbstractRuntimeService

/**
 * Default Runtime implementation.
 *
 * Initially every installed module is considered licensed.
 *
 * The Android application later replaces this with the
 * GooglePlayLicenseManager.
 */
class DefaultLicenseManager :
    AbstractRuntimeService(
        "runtime.licenseManager"
    ),
    LicenseManager {

    override fun licenseState(
        moduleId: String
    ): LicenseState {

        return LicenseState.LICENSED
    }
}