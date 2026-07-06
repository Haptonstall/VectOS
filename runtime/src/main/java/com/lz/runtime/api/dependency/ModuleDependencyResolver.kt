package com.lz.runtime.api.dependency

import com.lz.runtime.api.RuntimeModule

interface ModuleDependencyResolver {

    /**
     * Returns true if all required dependencies for the module
     * are satisfied.
     */
    fun canLoad(
        module: RuntimeModule
    ): Boolean
}