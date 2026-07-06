package com.lz.vectos.app.runtime

import com.lz.runtime.api.RuntimeModuleProvider

/**
 * Creates RuntimeModuleProvider instances from class names.
 *
 * Android is responsible for locating provider implementations.
 */
interface RuntimeModuleProviderFactory {

    fun create(

        className: String

    ): RuntimeModuleProvider

}