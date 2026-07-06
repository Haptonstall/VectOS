package com.lz.vectos.app.runtime

import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.api.RuntimeEnvironment

/**
 * Convenience helpers.
 */
fun RuntimeEnvironment.destinations():

        List<NavigationDestination> {

    return context
        .navigationRegistry
        .destinations()

}

fun RuntimeEnvironment.modules() =

    context
        .runtimeModuleRegistry
        .modules()