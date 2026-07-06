package com.lz.vectos.app.runtime

import android.content.Context
import com.lz.runtime.api.RuntimeConfiguration
import com.lz.runtime.api.model.Version

/**
 * Creates the Runtime configuration used during startup.
 */
object RuntimeConfigurationFactory {

    fun create(

        context: Context

    ): RuntimeConfiguration {

        return RuntimeConfiguration(

            runtimeEnvironmentVersion = Version(
                major = 1,
                minor = 0,
                patch = 0
            ),

            debugMode = false,

            autoInitializeModules = true,

            autoDiscoverModules = true

        )
    }
}