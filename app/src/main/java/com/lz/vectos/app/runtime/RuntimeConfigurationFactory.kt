package com.lz.vectos.app.runtime

import android.content.Context
import com.lz.common.Version
import com.lz.runtime.api.RuntimeConfiguration

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