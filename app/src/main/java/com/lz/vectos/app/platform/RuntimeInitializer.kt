package com.lz.vectos.app.platform

import android.content.Context
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.boot.RuntimeBootstrapper

/**
 * Creates and owns the application's RuntimeEnvironment.
 *
 * This is the only class responsible for bootstrapping the
 * RuntimeEnvironment.
 */
object RuntimeInitializer {

    @Volatile
    private var runtime: RuntimeEnvironment? = null

    /**
     * Creates (if necessary) and starts the RuntimeEnvironment.
     */
    fun initialize(
        context: Context
    ): RuntimeEnvironment {

        runtime?.let { return it }

        synchronized(this) {

            runtime?.let { return it }

            val configuration =
                RuntimeConfigurationFactory.create(context)

            val environment =
                RuntimeBootstrapper.create(configuration)

            environment.start()

            runtime = environment

            return environment
        }
    }

    /**
     * Returns the initialized RuntimeEnvironment.
     */
    fun runtime(): RuntimeEnvironment {

        return checkNotNull(runtime) {
            "RuntimeEnvironment has not been initialized."
        }
    }

    /**
     * Stops the RuntimeEnvironment.
     */
    fun shutdown() {

        runtime?.stop()

        runtime = null
    }
}