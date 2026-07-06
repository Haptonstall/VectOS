package com.lz.vectos.app.runtime

import android.content.Context
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.vectos.app.platform.RuntimeInitializer


/**
 * Singleton owner of the RuntimeEnvironment.
 *
 * The application creates the Runtime exactly once.
 */
object RuntimeManager {

    @Volatile
    private var runtime: RuntimeEnvironment? = null

    /**
     * Returns the running RuntimeEnvironment.
     */
    fun runtime(): RuntimeEnvironment {

        return checkNotNull(runtime) {

            "RuntimeEnvironment has not been initialized."

        }

    }

    /**
     * Initializes Runtime once.
     */
    fun initialize(

        context: Context

    ) {

        if (runtime != null)
            return

        synchronized(this) {

            if (runtime == null) {

                runtime =

                    RuntimeInitializer.initialize(
                        context
                    )

            }

        }

    }

    /**
     * Stops Runtime.
     */
    fun shutdown() {

        runtime?.stop()

        runtime = null

    }

}