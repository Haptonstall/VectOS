package com.lz.runtime.api.licensing

/**
 * Represents the current license state of a Runtime module.
 */
enum class LicenseState {
    LICENSED,
    NOT_LICENSED,
    TRIAL,
    EXPIRED
}