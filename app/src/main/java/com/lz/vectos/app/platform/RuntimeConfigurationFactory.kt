package com.lz.vectos.app.platform

import android.content.Context
import com.lz.runtime.api.RuntimeConfiguration
import com.lz.runtime.api.model.Version

/**
 * Creates the RuntimeConfiguration used to initialize
 * the RuntimeEnvironment.
 *
 * This is the application's composition root for Runtime
 * configuration.
 */
object RuntimeConfigurationFactory {

    fun create(
        context: Context
    ): RuntimeConfiguration {

        // Context isn't used yet, but we keep it because future
        // configuration may depend on BuildConfig, preferences,
        // device capabilities, etc.

        return RuntimeConfiguration(

            runtimeEnvironmentVersion = Version(1, 0, 0),

            debugMode = false,

            autoInitializeModules = true,

            autoDiscoverModules = true
        )
    }
}