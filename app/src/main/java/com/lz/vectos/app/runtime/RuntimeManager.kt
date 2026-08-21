package com.lz.vectos.app.runtime

import com.lz.runtime.api.RuntimeEnvironment
import com.lz.vectos.app.platform.RuntimeInitializer

/**
 * Thin accessor for the RuntimeEnvironment singleton.
 *
 * Previously maintained its own separate `runtime` field, populated only by
 * an `initialize()` that nothing ever called — the real initialization path
 * is Hilt's ModuleBindings.provideRuntimeEnvironment() -> RuntimeInitializer.
 * That meant runtime() always threw "RuntimeEnvironment has not been
 * initialized." Now delegates directly to RuntimeInitializer's actually-
 * populated singleton instead of duplicating that state.
 */
object RuntimeManager {

    /**
     * Returns the running RuntimeEnvironment.
     */
    fun runtime(): RuntimeEnvironment = RuntimeInitializer.runtime()

    /**
     * Stops Runtime.
     */
    fun shutdown() {
        RuntimeInitializer.shutdown()
    }
}
