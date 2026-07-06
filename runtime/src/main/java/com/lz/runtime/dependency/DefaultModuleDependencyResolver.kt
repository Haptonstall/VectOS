package com.lz.runtime.dependency

import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.dependency.ModuleDependencyResolver

/**
 * Initial implementation.
 *
 * No dependencies are enforced yet.
 */
class DefaultModuleDependencyResolver :
    ModuleDependencyResolver {

    override fun canLoad(
        module: RuntimeModule
    ): Boolean {

        return true
    }
}