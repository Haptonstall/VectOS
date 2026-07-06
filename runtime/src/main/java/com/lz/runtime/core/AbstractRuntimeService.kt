package com.lz.runtime.core

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeService

/**
 * Base implementation for RuntimeEnvironment services.
 */
abstract class AbstractRuntimeService(

    final override val id: String

) : RuntimeService {

    protected lateinit var context: RuntimeContext
        private set

    final override fun initialize(
        context: RuntimeContext
    ) {

        this.context = context

        onInitialize()
    }

    final override fun shutdown(
        context: RuntimeContext
    ) {

        onShutdown()
    }

    protected open fun onInitialize() {}

    protected open fun onShutdown() {}
}