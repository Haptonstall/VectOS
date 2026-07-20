package com.lz.vectos.app.runtime

import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.api.RuntimeModule

/**
 * Convenience helpers used by the application layer.
 */

fun RuntimeEnvironment.modules(): List<RuntimeModule> =

    context
        .runtimeModuleRegistry
        .modules()