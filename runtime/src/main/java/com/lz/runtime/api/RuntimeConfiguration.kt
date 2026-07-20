package com.lz.runtime.api

import com.lz.common.Version

/**
 * Immutable configuration used when constructing the RuntimeEnvironment.
 */
data class RuntimeConfiguration(

    /**
     * RuntimeEnvironment semantic version.
     */
    val runtimeEnvironmentVersion: Version,

    /**
     * Enables diagnostic logging.
     */
    val debugMode: Boolean,

    /**
     * Automatically initialize installed modules.
     */
    val autoInitializeModules: Boolean = true,

    /**
     * Automatically discover installed modules.
     */
    val autoDiscoverModules: Boolean = true
)