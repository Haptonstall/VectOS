package com.lz.runtime.boot

import com.lz.runtime.api.RuntimeEnvironment

/**
 * Performs RuntimeEnvironment startup.
 */
object RuntimeInitializer {

    fun initialize(

        runtimeEnvironment: RuntimeEnvironment

    ) {

        runtimeEnvironment.start()

    }
}